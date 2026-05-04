pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        gradlePluginPortal()
    }
}

// can't use libs.versions.toml for this - https://github.com/gradle/gradle/issues/36437
// make sure to update it there too tho.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" // https://plugins.gradle.org/plugin/org.gradle.toolchains.foojay-resolver-convention
    id("dev.kikugie.stonecutter") version "0.9.+" // https://stonecutter.kikugie.dev/
}

val versions = listOf("1.20.1", "1.21.1", "1.21.4", "1.21.8", "26.1")

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    create(rootProject) {
        versions(versions)
        vcsVersion = "1.20.1"

        branch("common")
        branch("fabric")
        branch("neoforge") {
            // NeoForge doesn't exist for <=1.20.1
            versions(versions.filter { stonecutter.eval(it, ">1.20.1") })
        }
        branch("forge") {
            // KLF doesn't exist for >=1.20.5, don't bother
            versions(versions.filter { stonecutter.eval(it, "<1.20.5") })
        }
    }
}

include(":api")
include(":standalone")
include(":transcribers", ":transcribers:google", ":transcribers:whisper")
include(":relay")

rootProject.name = "UnityTranslate"