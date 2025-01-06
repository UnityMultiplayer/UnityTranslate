pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.minecraftforge.net")

        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        kotlin("jvm") version("2.1.0")
        kotlin("plugin.serialization") version("2.1.0")
        id("architectury-plugin") version("3.4-SNAPSHOT")
        id("dev.architectury.loom") version("1.7-SNAPSHOT")
    }
}

val projectName: String = extra["mod.name"]?.toString()!!
rootProject.name = projectName
rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.20.1-fabric",
    "1.20.1-forge",

    "1.20.4-fabric",
    "1.20.4-neoforge",
    //"1.20.4-forge",

    "1.20.6-fabric",
    "1.20.6-neoforge",
    //"1.20.6-forge",

    "1.21.1-fabric",
    "1.21.1-neoforge",
    //"1.21.1-forge",

    "1.21.3-fabric",
    "1.21.3-neoforge",
    //"1.21.3-forge"
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}