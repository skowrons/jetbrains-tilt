package one.skowron.tiltfile

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

internal object TiltExecutable {
    fun resolve(configuredPath: String = TiltSettings.getInstance().executablePath): String? {
        if (configuredPath.isNotBlank()) return resolveConfigured(configuredPath.trim())
        val command = if (SystemInfo.isWindows) "tilt.exe" else "tilt"
        return PathEnvironmentVariableUtil.findInPath(command)?.absolutePath
            ?: listOf("/opt/homebrew/bin/tilt", "/usr/local/bin/tilt", "${System.getProperty("user.home")}/.local/bin/tilt")
                .firstOrNull(::isExecutable)
    }

    private fun resolveConfigured(value: String): String? {
        if (isExecutable(value)) return Path.of(value).toAbsolutePath().toString()
        if ('/' !in value && '\\' !in value) return PathEnvironmentVariableUtil.findInPath(value)?.absolutePath
        return null
    }

    private fun isExecutable(value: String): Boolean = try {
        val path = Path.of(value)
        Files.isRegularFile(path) && Files.isExecutable(path)
    } catch (_: InvalidPathException) {
        false
    }

    fun commandLine(workingDirectory: String?, configuredPath: String = TiltSettings.getInstance().executablePath): GeneralCommandLine {
        val executable = resolve(configuredPath)
            ?: throw ExecutionException("Tilt executable not found. Set its path in Settings > Languages & Frameworks > Tiltfile.")
        return GeneralCommandLine(executable, "lsp", "start")
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workingDirectory)
            .withCharset(Charsets.UTF_8)
    }
}

