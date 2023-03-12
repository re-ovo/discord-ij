package me.rerere.discordij.service

import com.google.common.cache.CacheBuilder
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import me.rerere.discordij.render.ActivityWrapper
import me.rerere.discordij.render.DiscordRPRender
import me.rerere.discordij.setting.DiscordIJSettingProjectState
import me.rerere.discordij.setting.DisplayMode

/**
 * TODO: Implement a better tracker?
 *
 * The current tracker seems to incorrectly set the user's status in some cases
 */
@Service
class TimeService {
    private val startTime = System.currentTimeMillis()
    private var timeTracker = CacheBuilder.newBuilder()
        .expireAfterAccess(1, java.util.concurrent.TimeUnit.HOURS)
        .maximumSize(128)
        .build<String, Long>()
    private var editingFile: FileItem? = null
    private var editingProject: ProjectItem? = null

    fun onAppFrameCreated() {
        // TODO("Not yet implemented")
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
        editingFile = FileItem("file:${file.name}", file.name, file.fileType.name)
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
        editingFile = FileItem("file:${file.name}", file.name, file.fileType.name)
        render(
            project = project,
        )
    }

    fun render(project: Project) {
        val state = project.service<DiscordIJSettingProjectState>().state
        if (editingFile != null && state.displayMode == DisplayMode.FILE) {
            service<DiscordRPRender>().updateActivity(
                ActivityWrapper(
                    state = "Editing file ${editingFile?.fileName}",
                    details = "Project: ${editingProject?.projectName}",
                    startTimestamp = editingFile?.key?.let { timeTracker.getIfPresent(it) },
                ).applyIDEInfo().applyFileInfo()
            )
        } else if (editingProject != null && state.displayMode >= DisplayMode.PROJECT) {
            service<DiscordRPRender>().updateActivity(
                ActivityWrapper(
                    state = "Editing project ${editingProject?.projectName}",
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
            val type = getFileTypeByName(it.type)
            smallImageKey = largeImageKey // swap
            smallImageText = largeImageText // swap
            largeImageKey = type.icon
            largeImageText = type.name
        }
        return this
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
    val type: String
) : TimedItem(key)