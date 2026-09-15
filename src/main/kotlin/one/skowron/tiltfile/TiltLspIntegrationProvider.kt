package one.skowron.tiltfile

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.ProjectWideLspClientDescriptor
import java.util.concurrent.atomic.AtomicBoolean

internal class TiltLspIntegrationProvider : LspIntegrationProvider {
    override fun fileOpened(project: Project, file: VirtualFile, clientStarter: LspIntegrationProvider.LspClientStarter) {
        if (!TiltFiles.isSupported(file)) return
        if (TiltExecutable.resolve() == null) {
            project.service<TiltMissingExecutableNotification>().show()
            return
        }
        clientStarter.ensureClientStarted(TiltLspClientDescriptor(project))
    }
}

internal class TiltLspClientDescriptor(project: Project) : ProjectWideLspClientDescriptor(project, "Tiltfile") {
    override fun isSupportedFile(file: VirtualFile): Boolean = TiltFiles.isSupported(file)
    override fun getLanguageId(file: VirtualFile): String = "tiltfile"
    override fun createCommandLine(): GeneralCommandLine = TiltExecutable.commandLine(project.basePath)
}

@Service(Service.Level.PROJECT)
internal class TiltMissingExecutableNotification(private val project: Project) {
    private val shown = AtomicBoolean()

    fun show() {
        if (!shown.compareAndSet(false, true)) return
        NotificationGroupManager.getInstance().getNotificationGroup("Tiltfile")
            .createNotification("Tilt executable not found", "Install Tilt or select its executable to enable code completion and documentation.", NotificationType.WARNING)
            .addAction(NotificationAction.createSimpleExpiring("Configure Tilt") {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, TiltConfigurable::class.java)
            })
            .notify(project)
    }

    fun reset() { shown.set(false) }
}

