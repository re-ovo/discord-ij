package me.rerere.discordij.service

import com.google.common.cache.CacheBuilder
import com.intellij.analysis.problemsView.ProblemsCollector
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEventMulticasterEx
import com.intellij.openapi.editor.ex.FocusChangeListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitUtil
import me.rerere.discordij.DiscordIJ
import me.rerere.discordij.render.ActivityWrapper
import me.rerere.discordij.render.DiscordRPRender
import me.rerere.discordij.setting.DiscordIJSettingProjectState
import me.rerere.discordij.setting.DisplayMode
import org.jetbrains.concurrency.runAsync
import java.lang.ref.WeakReference

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
        editingProject = ProjectItem.from(project)
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
        editingProject = ProjectItem.from(project)
        editingFile = FileItem.from(file)
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
        editingFile = FileItem.from(file)
        editingProject = ProjectItem.from(project)
        render(
            project = project,
        )
    }

    fun render(project: Project) {
        runAsync {
            runCatching {
                val configState = project.service<DiscordIJSettingProjectState>().state
                val problemsCollector = ProblemsCollector.getInstance(project)
                val repo = editingFile?.file?.get()?.let {
                    GitUtil.getRepositoryManager(project).getRepositoryForFile(it)
                }
                val branchName = repo?.currentBranch?.name ?: "--"
                val repoName = repo?.presentableUrl ?: "--"

                if (editingFile != null && configState.displayMode == DisplayMode.FILE) {
                    val problems = editingFile?.file?.get()?.let { problemsCollector.getFileProblemCount(it) } ?: 0

                    val variables = mapOf(
                        "%projectName%" to (editingProject?.projectName ?: "--"),
                        "%projectPath%" to (editingProject?.projectPath ?: "--"),
                        "%projectProblems%" to problemsCollector.getProblemCount().toString(),
                        "%fileName%" to (editingFile?.fileName ?: "--"),
                        "%filePath%" to (editingFile?.filePath ?: "--"),
                        "%fileProblems%" to problems.toString(),
                        "%branch%" to branchName,
                        "%repository%" to repoName,
                    )

                    service<DiscordRPRender>().updateActivity(
                        ActivityWrapper(
                            state = configState.fileStateFormat.replaceVariables(variables),
                            details = configState.fileDetailFormat.ifBlank { null }?.replaceVariables(variables),
                            startTimestamp = editingFile?.key?.let { timeTracker.getIfPresent(it) },
                        ).applyIDEInfo().applyFileInfo()
                    )

                    DiscordIJ.logger.warn("rendering file: ${configState.fileStateFormat.replaceVariables(variables)}")
                } else if (editingProject != null && configState.displayMode >= DisplayMode.PROJECT) {
                    val variables = mapOf(
                        "%projectName%" to (editingProject?.projectName ?: "--"),
                        "%projectPath%" to (editingProject?.projectPath ?: "--"),
                        "%projectProblems%" to problemsCollector.getProblemCount().toString(),
                        "%branch%" to branchName,
                        "%repository%" to repoName,
                    )
                    service<DiscordRPRender>().updateActivity(
                        ActivityWrapper(
                            state = configState.projectStateFormat.replaceVariables(variables),
                            details = configState.projectDetailFormat.ifBlank { null }?.replaceVariables(variables),
                            startTimestamp = editingProject?.key?.let { timeTracker.getIfPresent(it) },
                        ).applyIDEInfo()
                    )
                } else {
                    service<DiscordRPRender>().updateActivity(
                        ActivityWrapper(
                            state = if (configState.displayMode == DisplayMode.IDE)
                                ApplicationInfoEx.getInstanceEx().fullApplicationName
                            else
                                "Idle",
                            startTimestamp = startTime,
                        ).applyIDEInfo()
                    )
                }
            }.onFailure {
                it.printStackTrace()
                println("Failed to render activity: ${it.message}")
            }
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

private fun String.replaceVariables(variables: Map<String, String>): String {
    var result = this
    variables.forEach { (key, value) ->
        result = result.replace(key, value)
    }
    return result
}

class ProjectItem(
    key: String,
    val projectName: String,
    val projectPath: String?,
) : TimedItem(key) {
    companion object {
        fun from(project: Project): ProjectItem {
            return ProjectItem(
                "project:${project.name}",
                project.name,
                project.basePath
            )
        }
    }
}

class FileItem(
    key: String,
    val file: WeakReference<VirtualFile>,
    val fileName: String,
    val type: String,
    val extension: String?,
    val filePath: String?,
) : TimedItem(key) {
    companion object {
        fun from(file: VirtualFile): FileItem {
            return FileItem(
                "file:${file.name}",
                WeakReference(file),
                file.name,
                file.fileType.name,
                file.extension,
                file.path,
            )
        }
    }
}