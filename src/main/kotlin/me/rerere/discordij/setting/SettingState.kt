package me.rerere.discordij.setting

data class SettingState(
    var displayMode: DisplayMode = DisplayMode.FILE
)

enum class DisplayMode {
    IDE,
    PROJECT,
    FILE
}