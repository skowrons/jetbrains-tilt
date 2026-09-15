package one.skowron.tiltfile

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.extensions.PluginId
import com.intellij.platform.lsp.api.LspIntegrationProvider
import com.intellij.platform.lsp.api.LspClientDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.xmlb.XmlSerializer
import org.jetbrains.plugins.textmate.TextMateFileType
import org.jetbrains.plugins.textmate.TextMateService
import org.jetbrains.plugins.textmate.api.TextMateBundleProvider
import java.nio.file.Files

class TiltfileIntegrationTest : BasePlatformTestCase() {
    fun testInstalledPluginProvidesItsBundle() {
        val provider = TextMateBundleProvider.EP_NAME.extensionList.filterIsInstance<TiltTextMateBundleProvider>().single()
        val bundle = provider.getBundles().single()
        val plugin = requireNotNull(PluginManagerCore.getPlugin(PluginId.getId("one.skowron.tiltfile")))
        assertTrue(bundle.path.startsWith(plugin.pluginPath))
        assertTrue(Files.isRegularFile(bundle.path.resolve("Syntaxes/Tiltfile.tmLanguage")))
        assertTrue(Files.isRegularFile(bundle.path.resolve("Preferences/General.tmPreferences")))
        assertTrue(Files.isRegularFile(bundle.path.resolve("LICENSE")))
    }

    fun testTiltfilesAutomaticallyReceiveTextMateHighlighting() {
        for (name in listOf("Tiltfile", "tiltfile", "backend.Tiltfile", "local.tiltfile")) {
            assertNotNull(name, TextMateService.getInstance().getLanguageDescriptorByFileName(name))
            val file = myFixture.configureByText(name, "# local services\nk8s_yaml('app.yaml')\n")
            assertEquals(name, TextMateFileType.INSTANCE, file.fileType)
            val highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, file.virtualFile)
            highlighter.setText(file.text)
            val iterator = highlighter.createIterator(0)
            val tokenTypes = mutableSetOf<String>()
            while (!iterator.atEnd()) {
                tokenTypes += iterator.tokenType.toString()
                iterator.advance()
            }
            assertTrue("Expected comment and string tokens: $tokenTypes", tokenTypes.any { it.contains("comment") } && tokenTypes.any { it.contains("string") })
        }
    }

    fun testLspOnlyClaimsTiltfilesAndSendsTiltfileLanguageId() {
        val descriptor = TiltLspClientDescriptor(project)
        for (name in listOf("Tiltfile", "tiltfile", "backend.Tiltfile", "local.tiltfile")) {
            val file = myFixture.configureByText(name, "print('hello')\n").virtualFile
            assertTrue(name, descriptor.isSupportedFile(file))
            assertEquals("tiltfile", descriptor.getLanguageId(file))
        }
        for (name in listOf("main.py", "BUILD", "helpers.bzl", "Tiltfile.yaml", "myTiltfile")) {
            assertFalse(name, descriptor.isSupportedFile(myFixture.configureByText(name, "").virtualFile))
        }
        val directory = myFixture.tempDirFixture.findOrCreateDir("nested/Tiltfile")
        assertTrue(directory.isDirectory)
        assertFalse(descriptor.isSupportedFile(directory))
    }

    fun testRegisteredLspProviderStartsTiltWhenAFileIsOpened() {
        val provider = LspIntegrationProvider.EP_NAME.extensionList.filterIsInstance<TiltLspIntegrationProvider>().single()
        val settings = TiltSettings.getInstance()
        val previousPath = settings.executablePath
        val executable = Files.createTempFile("tilt-provider-test", "")
        try {
            executable.toFile().setExecutable(true)
            settings.executablePath = executable.toString()
            var started: LspClientDescriptor? = null
            val starter = object : LspIntegrationProvider.LspClientStarter {
                override fun ensureClientStarted(descriptor: LspClientDescriptor) { started = descriptor }
            }
            provider.fileOpened(project, myFixture.configureByText("main.py", "").virtualFile, starter)
            assertNull(started)
            provider.fileOpened(project, myFixture.configureByText("Tiltfile", "").virtualFile, starter)
            assertTrue(started is TiltLspClientDescriptor)
            assertEquals(executable.toString(), started!!.createCommandLine().exePath)
        } finally {
            settings.executablePath = previousPath
            Files.deleteIfExists(executable)
        }
    }

    fun testExecutablePathWithSpacesIsOneArgumentAndUsesStdio() {
        val directory = Files.createTempDirectory("tilt executable ")
        val executable = directory.resolve("fake tilt")
        try {
            Files.writeString(executable, "#!/bin/sh\nexit 0\n")
            assertTrue(executable.toFile().setExecutable(true))
            val command = TiltExecutable.commandLine(directory.toString(), executable.toString())
            assertEquals(executable.toString(), command.exePath)
            assertEquals(listOf("lsp", "start"), command.parametersList.list)
            assertEquals(directory.toFile(), command.workDirectory)
            assertFalse(command.isRedirectErrorStream)
            assertEquals(Charsets.UTF_8, command.charset)
        } finally {
            Files.deleteIfExists(executable)
            Files.deleteIfExists(directory)
        }
    }

    fun testInvalidExplicitExecutableDoesNotFallBackToAnotherTilt() {
        assertNull(TiltExecutable.resolve("/nonexistent/tiltfile-test/tilt"))
        assertNull(TiltExecutable.resolve("invalid\u0000path"))
    }

    fun testSettingsRoundTrip() {
        val first = TiltSettings()
        first.executablePath = "  /opt/custom tools/tilt  "
        val restored = TiltSettings()
        restored.loadState(XmlSerializer.deserialize(XmlSerializer.serialize(first.state), TiltSettings.Options::class.java))
        assertEquals("/opt/custom tools/tilt", restored.executablePath)
    }
}
