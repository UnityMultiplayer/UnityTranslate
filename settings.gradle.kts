pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.kikugie.dev/releases")

        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }

    plugins {
        kotlin("jvm") version("2.3.0")
        kotlin("plugin.serialization") version("2.3.0")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version("0.7.+")
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    val versions = listOf("1.20.1", "1.21.1", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11")

    create(rootProject) {
        versions(versions)
        vcsVersion = "1.20.1"

        branch("fabric")
        branch("forge") {
            versions(versions.filterIndexed { i, _ -> i <= versions.indexOf("1.20.1") })
        }
        branch("neoforge") {
            versions(versions.filterIndexed { i, _ -> i >= versions.indexOf("1.21.1") })
        }
    }
}

val projectName: String = extra["mod.name"]?.toString()!!
rootProject.name = projectName