package me.rerere.discordij.setting

data class SettingState(
    var displayMode: DisplayMode = DisplayMode.FILE,
    var projectStateFormat: String = "Working on Project: %projectName%",
    var projectDetailFormat: String = "%projectProblems% Problems in Project",
    var fileStateFormat: String = "Editing file: %fileName%",
    var fileDetailFormat: String = "Project: %projectName% - %fileProblems% Problems",
)

enum class DisplayMode {
    IDE,
    PROJECT,
    FILE
}