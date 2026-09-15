package one.skowron.tiltfile

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(name = "TiltfileSettings", storages = [Storage("tiltfile.xml")])
internal class TiltSettings : PersistentStateComponent<TiltSettings.Options> {
    data class Options(var executablePath: String = "")

    private var options = Options()
    override fun getState(): Options = options
    override fun loadState(state: Options) { options = state }

    var executablePath: String
        get() = options.executablePath
        set(value) { options.executablePath = value.trim() }

    companion object {
        fun getInstance(): TiltSettings = service()
    }
}

