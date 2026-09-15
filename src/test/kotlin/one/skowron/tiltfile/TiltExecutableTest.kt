package one.skowron.tiltfile

import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files

class TiltExecutableTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun findsFirstExecutableInPathOrder() {
        val first = temporaryFolder.newFolder("first")
        val second = temporaryFolder.newFolder("second")
        val firstExecutable = executable(first)
        val secondExecutable = executable(second)

        TestCase.assertEquals(firstExecutable.absolutePath, TiltExecutable.findOnPath("tilt.exe", path(first, second)))
        TestCase.assertEquals(secondExecutable.absolutePath, TiltExecutable.findOnPath("tilt.exe", path(second, first)))
    }

    @Test
    fun skipsMissingFilesAndDirectoriesNamedLikeTheExecutable() {
        val missing = temporaryFolder.newFolder("missing")
        val directory = temporaryFolder.newFolder("directory")
        Files.createDirectory(directory.toPath().resolve("tilt.exe"))
        val valid = temporaryFolder.newFolder("valid")
        val executable = executable(valid)

        TestCase.assertEquals(executable.absolutePath, TiltExecutable.findOnPath("tilt.exe", path(missing, directory, valid)))
    }

    @Test
    fun skipsFilesWithoutExecutePermission() {
        val blocked = temporaryFolder.newFolder("blocked")
        val file = executable(blocked)
        org.junit.Assume.assumeTrue(file.setExecutable(false, false) && !Files.isExecutable(file.toPath()))
        val valid = temporaryFolder.newFolder("valid")
        val executable = executable(valid)

        TestCase.assertEquals(executable.absolutePath, TiltExecutable.findOnPath("tilt.exe", path(blocked, valid)))
    }

    @Test
    fun handlesSpacesAndSkipsInvalidPathEntries() {
        val directory = temporaryFolder.newFolder("custom tools")
        val executable = executable(directory)
        val pathValue = listOf("", "invalid\u0000path", directory.path).joinToString(File.pathSeparator)

        TestCase.assertEquals(executable.absolutePath, TiltExecutable.findOnPath("tilt.exe", pathValue))
    }

    @Test
    fun ignoresRelativePathEntriesEvenWhenTheyContainAnExecutable() {
        val directory = temporaryFolder.newFolder("relative")
        executable(directory)
        val relativePath = File("").absoluteFile.toPath().relativize(directory.toPath())

        TestCase.assertNull(TiltExecutable.findOnPath("tilt.exe", relativePath.toString()))
    }

    @Test
    fun returnsNullWhenNoExecutableCanBeFound() {
        TestCase.assertNull(TiltExecutable.findOnPath("tilt.exe", null))
        TestCase.assertNull(TiltExecutable.findOnPath("tilt.exe", ""))
        TestCase.assertNull(TiltExecutable.findOnPath("tilt.exe", temporaryFolder.root.path))
        TestCase.assertNull(TiltExecutable.findOnPath("invalid\u0000name", temporaryFolder.root.path))
    }

    private fun executable(directory: File): File = File(directory, "tilt.exe").also {
        it.writeText("#!/bin/sh\nexit 0\n")
        TestCase.assertTrue(it.setExecutable(true))
    }

    private fun path(vararg directories: File): String = directories.joinToString(File.pathSeparator) { it.path }
}
