package one.skowron.tiltfile

import com.intellij.openapi.vfs.VirtualFile

internal object TiltFiles {
    fun isSupported(file: VirtualFile): Boolean = !file.isDirectory && isSupportedName(file.name)

    fun isSupportedName(name: String): Boolean =
        name.equals("Tiltfile", ignoreCase = true) || name.endsWith(".tiltfile", ignoreCase = true)
}
