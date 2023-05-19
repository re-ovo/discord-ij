package me.rerere.discordij.service

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEventMulticasterEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import me.rerere.discordij.DiscordIJ
import me.rerere.discordij.render.ActivityWrapper
import me.rerere.discordij.render.DiscordRPRender
import me.rerere.discordij.setting.DiscordIJSettingProjectState
import me.rerere.discordij.setting.DisplayMode
import org.apache.xerces.dom.DocumentImpl
import java.util.UUID

/**
 * TODO: Implement a better tracker?
 *
 * The current tracker seems to incorrectly set the user's status in some cases
 */
@Service
class TimeService : Disposable {
    private val startTime = System.currentTimeMillis()
    private var timeTracker = CacheBuilder.newBuilder()
        .expireAfterAccess(1, java.util.concurrent.TimeUnit.HOURS)
        .maximumSize(128)
        .build<String, Long>()
    private var editingFile: FileItem? = null
    private var editingProject: ProjectItem? = null

    init {
        val multicaster: Any = EditorFactory.getInstance().eventMulticaster
        if (multicaster is EditorEventMulticasterEx) {
            multicaster.addFocusChangeListener(object : FocusChangeListener {
                override fun focusGained(editor: Editor) {
                    val project = editor.project ?: return
                    val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
                    onFileChanged(project, file)
                }
            }, this)
        }
    }

    fun onProjectOpened(project: Project) {
        timeTracker.put("project:${project.name}", System.currentTimeMillis())
        editingProject = ProjectItem("project:${project.name}", project.name)
        render(
            project = project
        )
    }

    fun onProjectClosed(project: Project) {
        timeTracker.invalidate("project:${project.name}")
        editingProject = null
        render(
            project = project
        )
    }

    fun onFileOpened(project: Project, file: VirtualFile) {
        timeTracker.put("file:${file.name}", System.currentTimeMillis())
        editingFile = FileItem("file:${file.name}", file.name, file.fileType.name, file.extension)
        render(
            project = project,
        )
    }

    fun onFileClosed(project: Project, file: VirtualFile) {
        timeTracker.invalidate("file:${file.name}")
        editingFile = null
        render(
            project = project,
        )
    }

    fun onFileChanged(project: Project, file: VirtualFile) {
        editingFile = FileItem("file:${file.name}", file.name, file.fileType.name, file.extension)
        render(
            project = project,
        )
    }

    fun render(project: Project) {
        val state = project.service<DiscordIJSettingProjectState>().state
        if (editingFile != null && state.displayMode == DisplayMode.FILE) {
            service<DiscordRPRender>().updateActivity(
                ActivityWrapper(
                    state = "Editing File: ${editingFile?.fileName}",
                    details = "Project: ${editingProject?.projectName ?: project.name}",
                    startTimestamp = editingFile?.key?.let { timeTracker.getIfPresent(it) },
                ).applyIDEInfo().applyFileInfo()
            )
        } else if (editingProject != null && state.displayMode >= DisplayMode.PROJECT) {
            service<DiscordRPRender>().updateActivity(
                ActivityWrapper(
                    state = "Editing Project ${editingProject?.projectName ?: project.name}",
                    startTimestamp = editingProject?.key?.let { timeTracker.getIfPresent(it) },
                ).applyIDEInfo()
            )
        } else {
            service<DiscordRPRender>().updateActivity(
                ActivityWrapper(
                    state = if (state.displayMode == DisplayMode.IDE)
                        ApplicationInfoEx.getInstanceEx().fullApplicationName
                    else
                        "Idle",
                    startTimestamp = startTime,
                ).applyIDEInfo()
            )
        }
    }

    private fun ActivityWrapper.applyIDEInfo(): ActivityWrapper {
        val ideType = currentIDEType
        largeImageKey = ideType.icon
        largeImageText = ideType.name
        return this
    }

    private fun ActivityWrapper.applyFileInfo(): ActivityWrapper {
        editingFile?.let {
            val type = getFileTypeByName(it.type, it.extension)
            smallImageKey = largeImageKey // swap
            smallImageText = largeImageText // swap
            largeImageKey = type.icon
            largeImageText = type.name
        }
        return this
    }

    override fun dispose() {
    }
}

sealed class TimedItem(
    val key: String
)

class ProjectItem(
    key: String,
    val projectName: String,
) : TimedItem(key)

class FileItem(
    key: String,
    val fileName: String,
    val type: String,
    val extension: String?,
) : TimedItem(key)