pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.firstdark.dev/releases")
        maven("https://maven.deftu.dev/releases")
        maven("https://maven.deftu.dev/snapshots")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://server.bbkr.space/artifactory/libs-release/")
        maven("https://maven.kikugie.dev/releases")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version("0.7")
}

val projectName: String = extra["mod.name"]?.toString()!!
rootProject.name = projectName

include("bukkit", "common")
include("library:library")

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true

    create("minecraft") {
        versions("1.18.2", "1.19.2", "1.20.1", "1.20.4", "1.20.6", "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6")
        vcsVersion = "1.20.4"

        branch("fabric")
        branch("forge")
        branch("neoforge") {
            versions("1.20.4", "1.20.6", "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6")
        }
    }
}