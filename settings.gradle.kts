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
        kotlin("jvm") version("2.1.0")
        kotlin("plugin.serialization") version("2.1.0")
        id("dev.deftu.gradle.multiversion-root") version("2.26.0")
    }
}

val projectName: String = extra["mod.name"]?.toString()!!
rootProject.name = projectName
rootProject.buildFileName = "root.gradle.kts"

listOf("bukkit", "common", "vanilla").forEach {
    include(it)
    project(":$it").apply {
        buildFileName = "build.gradle.kts"
    }
}

include()

val neoforgeVersion = "1.20.4"
var hasNeoForge = false

listOf(
    "1.16.5", "1.18.2", "1.19.2",
    "1.20.1", "1.20.4", "1.20.6",
    "1.21.1", "1.21.3", "1.21.4"
).forEach { version ->
    if (version == neoforgeVersion)
        hasNeoForge = true

    include("$version-fabric")
    project(":$version-fabric").apply {
        projectDir = file("versions/$version/fabric")
        buildFileName = "../../../minecraft.gradle.kts"
    }

    include("$version-forge")
    project(":$version-forge").apply {
        projectDir = file("versions/$version/forge")
        buildFileName = "../../../minecraft.gradle.kts"
    }

    if (hasNeoForge) {
        include("$version-neoforge")
        project(":$version-neoforge").apply {
            projectDir = file("versions/$version/neoforge")
            buildFileName = "../../../minecraft.gradle.kts"
        }
    }
}