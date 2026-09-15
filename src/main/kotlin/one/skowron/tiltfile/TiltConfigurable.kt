package one.skowron.tiltfile

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

internal class TiltConfigurable : Configurable {
    private var pathField: TextFieldWithBrowseButton? = null

    override fun getDisplayName(): String = "Tiltfile"

    override fun createComponent(): JComponent {
        val field = TextFieldWithBrowseButton()
        field.addBrowseFolderListener(null, FileChooserDescriptorFactory.singleFile().withTitle("Select Tilt Executable"))
        pathField = field
        return panel {
            row("Tilt executable:") { cell(field).align(AlignX.FILL) }
            row { comment("Leave empty to detect Tilt automatically. Changes restart the language server in open projects.") }
        }.also { reset() }
    }

    override fun isModified(): Boolean = pathField?.text?.trim()?.let { it != TiltSettings.getInstance().executablePath } ?: false

    override fun apply() {
        val value = pathField?.text?.trim() ?: return
        if (value.isNotEmpty() && TiltExecutable.resolve(value) == null) {
            throw ConfigurationException("Select an existing Tilt executable, or leave the field empty for automatic detection.")
        }
        TiltSettings.getInstance().executablePath = value
        ProjectManager.getInstance().openProjects.forEach { project ->
            project.service<TiltMissingExecutableNotification>().reset()
            LspClientManager.getInstance(project).stopAndRestartClientsIfNeeded(TiltLspIntegrationProvider::class.java)
        }
    }

    override fun reset() { pathField?.text = TiltSettings.getInstance().executablePath }
    override fun disposeUIResources() { pathField?.dispose(); pathField = null }
}
