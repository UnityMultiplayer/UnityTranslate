import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("dev.deftu.gradle.tools.shadow")
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
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
    }
}

subprojects {
    if (project.name == "minecraft")
        return@subprojects

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "dev.deftu.gradle.tools.shadow")

    dependencies {
        if (project.name != "common") {
            shade(implementation(project(":common"))!!)
        }

        // Voice Chat APIs
        implementation("de.maxhenkel.voicechat:voicechat-api:${rootProject.property("voicechat_api_version")}")
        compileOnly("su.plo.voice.api:server:${rootProject.property("plasmo_api_version")}")
        compileOnly("su.plo.voice.api:client:${rootProject.property("plasmo_api_version")}")

        // Kotlin
        if (!project.name.contains("fabric")) { // Fabric has FLK, but KFF is unreliable and Bukkit doesn't have a commonly-used Kotlin provider
            shade("org.jetbrains.kotlin:kotlin-reflect:${rootProject.property("kotlin_version")}")
            shade("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.property("kotlin_version")}")
            shade("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${rootProject.property("kotlin_version")}")
            shade("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${rootProject.property("kotlin_version")}")
            shade("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.property("kotlin_coroutines_version")}")
            shade("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${rootProject.property("kotlin_coroutines_version")}")
            shade("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${rootProject.property("kotlin_coroutines_version")}")
            shade("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${rootProject.property("kotlin_serialization_version")}")
            shade("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${rootProject.property("kotlin_serialization_version")}")
        }

        // UnityTranslateLib
        shade(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib:${rootProject.property("unitytranslatelib_version")}")!!)
        shade(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${rootProject.property("unitytranslatelib_version")}")!!)
        shade(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${rootProject.property("unitytranslatelib_version")}")!!)
    }

    tasks {
        named<ShadowJar>("fatJar") {
            val shadowPkg = "xyz.bluspring.unitytranslate.shaded"

            relocate("org.jetbrains", "$shadowPkg.jetbrains")
            relocate("kotlin", "$shadowPkg.kotlin")
            relocate("kotlinx", "$shadowPkg.kotlinx")
        }

        processResources {
            val properties = mutableMapOf<String, String>()

            properties["mod_version"] = rootProject.property("mod.version") as String

            filesMatching("plugin.yml") {
                expand(properties)
            }
        }
    }
}
