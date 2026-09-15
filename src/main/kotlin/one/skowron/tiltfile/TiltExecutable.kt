package one.skowron.tiltfile

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.EnvironmentUtil
import java.io.File
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

internal object TiltExecutable {
    fun resolve(configuredPath: String = TiltSettings.getInstance().executablePath): String? {
        if (configuredPath.isNotBlank()) return resolveConfigured(configuredPath.trim())
        val command = if (SystemInfo.isWindows) "tilt.exe" else "tilt"
        return findOnPath(command)
            ?: listOf("/opt/homebrew/bin/tilt", "/usr/local/bin/tilt", "${System.getProperty("user.home")}/.local/bin/tilt")
                .firstOrNull(::isExecutable)
    }

    private fun resolveConfigured(value: String): String? {
        if (isExecutable(value)) return Path.of(value).toAbsolutePath().toString()
        if ('/' !in value && '\\' !in value) return findOnPath(value)
        return null
    }

    // PathEnvironmentVariableUtil.findFirst is only available from IntelliJ Platform 2026.3.
    internal fun findOnPath(command: String, pathValue: String? = EnvironmentUtil.getValue("PATH")): String? {
        if (pathValue == null) return null
        for (directory in pathValue.split(File.pathSeparatorChar)) {
            try {
                val path = Path.of(directory)
                if (!path.isAbsolute) continue
                val candidate = path.resolve(command)
                if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) return candidate.toString()
            } catch (_: InvalidPathException) {
                // Ignore malformed PATH entries and keep searching.
            }
        }
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
