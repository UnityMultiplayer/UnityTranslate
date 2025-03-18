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
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }

    plugins {
        val dgtVersion = "2.26.0"

        kotlin("jvm") version("2.0.10")
        kotlin("plugin.serialization") version("2.0.10")
        id("dev.deftu.gradle.multiversion-root") version(dgtVersion)
        id("dev.deftu.gradle.tools.shadow") version(dgtVersion)
    }
}

val projectName: String = extra["mod.name"]?.toString()!!
rootProject.name = projectName

include("bukkit", "common")

include(":minecraft")
project(":minecraft").buildFileName = "root.gradle.kts"

val neoforgeVersion = "1.20.4"
var hasNeoForge = false

listOf(
    //"1.16.5",
    "1.18.2", "1.19.2",
    "1.20.1", "1.20.4", "1.20.6",
    "1.21.1", "1.21.3", "1.21.4"
).forEach { version ->
    if (version == neoforgeVersion)
        hasNeoForge = true

    include(":minecraft:$version-fabric")
    project(":minecraft:$version-fabric").apply {
        projectDir = file("minecraft/versions/$version/fabric")
        buildFileName = "../../../build.gradle.kts"
    }

    include(":minecraft:$version-forge")
    project(":minecraft:$version-forge").apply {
        projectDir = file("minecraft/versions/$version/forge")
        buildFileName = "../../../build.gradle.kts"
    }

    if (hasNeoForge) {
        include(":minecraft:$version-neoforge")
        project(":minecraft:$version-neoforge").apply {
            projectDir = file("minecraft/versions/$version/neoforge")
            buildFileName = "../../../build.gradle.kts"
        }
    }
}