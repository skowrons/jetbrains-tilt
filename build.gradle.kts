import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    kotlin("jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "one.skowron.tiltfile"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    intellijPlatform {
        val localPath = providers.gradleProperty("platformLocalPath")
        if (localPath.isPresent) local(localPath.get()) else goland("2026.2.2.1")
        bundledPlugin("org.jetbrains.plugins.textmate")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
    }
}

kotlin {
    jvmToolchain(25)
    compilerOptions { jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY) }
}

intellijPlatform {
    pluginConfiguration {
        name = "Tiltfile"
        ideaVersion {
            sinceBuild = "262"
        }
    }
    pluginVerification {
        ides {
            val localPath = providers.gradleProperty("platformLocalPath")
            if (localPath.isPresent) local(localPath.get()) else create(IntelliJPlatformType.GoLand, "2026.2.2.1")
        }
    }
}

tasks {
    withType<PrepareSandboxTask>().configureEach {
        from("textmate") { into("${project.name}/textmate") }
        from("LICENSE") { into(project.name) }
    }
}
