package me.rerere.discordij.setting

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import me.rerere.discordij.service.TimeService
import javax.swing.JComponent

class DiscordIJConfigurable(
    private val project: Project
) : Configurable {
    private val panel = panel {
        val state = project.service<DiscordIJSettingProjectState>().state
        group {
            row("Display Mode") {
                comboBox(
                    items = DisplayMode.values().toList(),
                ).bindItem(state::displayMode.toNullableProperty())
            }
        }
    }

    override fun createComponent(): JComponent = panel

    override fun isModified(): Boolean = panel.isModified()

    override fun apply() = panel.apply().also {
        service<TimeService>().render(project)
    }

    override fun reset() = panel.reset()

    override fun getDisplayName(): String = "DiscordIJ"
}