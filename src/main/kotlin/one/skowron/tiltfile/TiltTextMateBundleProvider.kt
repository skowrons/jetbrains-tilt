package one.skowron.tiltfile

import com.intellij.openapi.extensions.PluginAware
import com.intellij.openapi.extensions.PluginDescriptor
import org.jetbrains.plugins.textmate.api.TextMateBundleProvider

internal class TiltTextMateBundleProvider : TextMateBundleProvider, PluginAware {
    private lateinit var plugin: PluginDescriptor

    override fun setPluginDescriptor(pluginDescriptor: PluginDescriptor) {
        plugin = pluginDescriptor
    }

    override fun getBundles(): List<TextMateBundleProvider.PluginBundle> {
        return listOf(TextMateBundleProvider.PluginBundle("Tiltfile", plugin.pluginPath.resolve("textmate/tiltfile.tmbundle")))
    }
}
