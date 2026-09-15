package one.skowron.tiltfile

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import org.junit.Assume.assumeTrue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Talks to an actual Tilt process using the same command and initialization as GoLand. */
class TiltLspSmokeTest : BasePlatformTestCase() {
    fun testRealTiltCompletionDocumentationSignaturesAndDiagnostics() {
        assumeTrue("Tilt must be installed to run the LSP smoke test", TiltExecutable.resolve() != null)
        val descriptor = TiltLspClientDescriptor(project)
        val process = descriptor.createCommandLine().createProcess()
        val diagnostics = LinkedBlockingQueue<PublishDiagnosticsParams>()
        val client = object : LanguageClient {
            override fun telemetryEvent(value: Any?) {}
            override fun publishDiagnostics(params: PublishDiagnosticsParams) { diagnostics.offer(params) }
            override fun showMessage(params: MessageParams) {}
            override fun showMessageRequest(params: ShowMessageRequestParams): CompletableFuture<MessageActionItem> =
                CompletableFuture.completedFuture(null)
            override fun logMessage(params: MessageParams) {}
        }
        // Tilt logs to stderr; drain it separately so it cannot corrupt or block the protocol.
        val stderr = StringBuilder()
        val stderrThread = Thread { process.errorStream.bufferedReader().useLines { lines -> lines.forEach { stderr.appendLine(it) } } }
        stderrThread.isDaemon = true
        stderrThread.start()
        val launcher = LSPLauncher.createClientLauncher(client, process.inputStream, process.outputStream)
        val listening = launcher.startListening()
        val server = launcher.remoteProxy
        try {
            val initialized = server.initialize(descriptor.createInitializeParams()).get(30, TimeUnit.SECONDS)
            assertNotNull(initialized.capabilities.completionProvider)
            assertNotNull(initialized.capabilities.signatureHelpProvider)
            server.initialized(InitializedParams())

            val text = "# Editor smoke test\nk8s_yaml('app.yaml')\nk8s_\n"
            val file = myFixture.addFileToProject("Tiltfile", text).virtualFile
            val uri = descriptor.getFileUri(file)
            val identifier = TextDocumentIdentifier(uri)
            server.textDocumentService.didOpen(DidOpenTextDocumentParams(TextDocumentItem(uri, descriptor.getLanguageId(file), 1, text)))

            val completion = server.textDocumentService.completion(CompletionParams(identifier, Position(2, 4))).get(20, TimeUnit.SECONDS)
            val items = if (completion.isLeft) completion.left else completion.right.items
            assertTrue("Tilt completion should include k8s_yaml: ${items.map { it.label }}", items.any { it.label.startsWith("k8s_yaml") })

            val hover = server.textDocumentService.hover(HoverParams(identifier, Position(1, 3))).get(20, TimeUnit.SECONDS)
            assertNotNull("Expected Tilt documentation", hover)
            assertTrue(hover.contents.toString().contains("k8s_yaml"))

            val signature = server.textDocumentService.signatureHelp(SignatureHelpParams(identifier, Position(1, 10))).get(20, TimeUnit.SECONDS)
            assertFalse("Expected parameter information", signature.signatures.isEmpty())

            diagnostics.clear()
            server.textDocumentService.didChange(DidChangeTextDocumentParams(
                VersionedTextDocumentIdentifier(uri, 2), listOf(TextDocumentContentChangeEvent("load(123, 'name')\n"))
            ))
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20)
            var foundError = false
            while (!foundError && System.nanoTime() < deadline) {
                val message = diagnostics.poll(1, TimeUnit.SECONDS) ?: continue
                foundError = message.uri == uri && message.version == 2 && message.diagnostics.any { it.severity == DiagnosticSeverity.Error }
            }
            assertTrue("Expected a diagnostic for the invalid load() argument; stderr: $stderr", foundError)

            server.textDocumentService.didChange(DidChangeTextDocumentParams(
                VersionedTextDocumentIdentifier(uri, 3), listOf(TextDocumentContentChangeEvent("k8s_yaml('app.yaml')\n"))
            ))
            val cleared = diagnostics.poll(20, TimeUnit.SECONDS)
            assertNotNull("Expected diagnostics to clear after fixing the load() call", cleared)
            assertEquals(uri, cleared!!.uri)
            assertEquals(3, cleared.version)
            assertTrue(cleared.diagnostics.isEmpty())
            server.textDocumentService.didClose(DidCloseTextDocumentParams(identifier))
            server.shutdown().get(10, TimeUnit.SECONDS)
            server.exit()
        } finally {
            listening.cancel(true)
            process.destroy()
            if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly()
            stderrThread.join(1000)
        }
    }
}
