import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("dev.architectury.loom") version "1.10-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.7.+" apply false
    id("com.gradleup.shadow") version "8.3.5" apply false
    kotlin("jvm") version "2.2.0" apply false
    kotlin("plugin.serialization") version "2.2.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.parchmentmc.org")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        exclusiveContent {
            forRepository {
                maven("https://api.modrinth.com/maven")
            }
            filter {
                includeGroup("maven.modrinth")
            }
        }
        maven("https://repo.clojars.org")
        maven("https://maven.terraformersmc.com/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.shedaniel.me/")
        maven("https://maven.maxhenkel.de/repository/public")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.nucleoid.xyz/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.su5ed.dev/releases")

        maven("https://repo.plo.su")
        maven("https://repo.plasmoverse.com/releases")
        maven("https://repo.plasmoverse.com/snapshots")

        maven("https://mvn.devos.one/releases")
        maven("https://mvn.devos.one/snapshots")

        maven("https://repo.essential.gg/repository/maven-public")
    }
}

subprojects {
    if (project.name == "minecraft" || project.name == "fabric" || project.name == "forge" || project.name == "neoforge" || project.name == "library" || project.parent?.name == "library")
        return@subprojects

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "com.gradleup.shadow")

    val shade by configurations.creating

    dependencies {
        if (project.name != "common") {
            shade("implementation"(project(":common"))!!)
        }

        // Kotlin
        if (!project.name.contains("fabric")) { // Fabric has FLK, but KFF is unreliable and Bukkit doesn't have a commonly-used Kotlin provider
            shade("org.jetbrains.kotlin:kotlin-reflect:${mod.dep("kotlin")}")
            shade("org.jetbrains.kotlin:kotlin-stdlib:${mod.dep("kotlin")}")
            shade("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${mod.dep("kotlin")}")
            shade("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${mod.dep("kotlin")}")
            shade("org.jetbrains.kotlinx:kotlinx-coroutines-core:${mod.dep("kotlin_coroutines")}")
            shade("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${mod.dep("kotlin_coroutines")}")
            shade("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${mod.dep("kotlin_coroutines")}")
            shade("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${mod.dep("kotlin_serialization")}")
            shade("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${mod.dep("kotlin_serialization")}")
        }

        // UnityTranslateLib
        //shade("implementation"("xyz.bluspring.unitytranslate:UnityTranslateLib:${mod.dep("unitytranslatelib")}")!!)
        shade("implementation"(project(":library:library"))!!)
        shade("implementation"("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${mod.dep("unitytranslatelib")}")!!)
        shade("implementation"("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${mod.dep("unitytranslatelib")}")!!)
    }

    tasks {
        named<ShadowJar>("shadowJar") {
            val shadowPkg = "xyz.bluspring.unitytranslate.shaded"

            relocate("org.jetbrains", "$shadowPkg.jetbrains")
            relocate("kotlin", "$shadowPkg.kotlin")
            relocate("kotlinx", "$shadowPkg.kotlinx")
        }

        named<ProcessResources>("processResources") {
            val properties = mutableMapOf<String, String>()

            properties["mod_version"] = rootProject.property("mod.version") as String

            filesMatching("plugin.yml") {
                expand(properties)
            }
        }
    }
}
