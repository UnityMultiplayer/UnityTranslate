import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.util.Version
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    java
    kotlin("jvm") version("2.1.10")
    kotlin("plugin.serialization") version("2.1.10")
    id("architectury-plugin") version("3.4-SNAPSHOT") apply false
    id("dev.architectury.loom") version("1.9-SNAPSHOT") apply false
    id("com.gradleup.shadow") version("8.3.3") apply false
}

val versions = project.subprojects
    .filter { it.name.endsWith("-fabric") }
    .map { it.name.replace("-fabric", "") }
    .sortedBy { Version.parse(it) }

fun getAllPriorVersions(current: String): List<String> {
    val versionList = mutableListOf<String>()

    for (version in versions) {
        if (version == current)
            break

        versionList.add(version)
    }

    return versionList
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")

    repositories {
        mavenCentral()
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

        maven("https://mvn.devos.one/snapshots")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "com.gradleup.shadow")

    val modLoaderName = if (project.name.endsWith("fabric"))
        "Fabric"
    else if (project.name.endsWith("neoforge"))
        "NeoForge"
    else if (project.name.endsWith("forge"))
        "Forge"
    else ""

    val supportsVanilla = project.name != "bukkit" && project.name != "common" && project.name != "vanilla"
    val shadow = configurations.getByName("shadow")

    val minecraftVersion = if (project.name == "fabric" || project.name == "vanilla")
        "1.16.5"
    else
        project.name.replaceAfter("-", "").removeSuffix("-")

    val common = configurations.create("common")
    val shadowCommon = configurations.create("shadowCommon")

    configurations.compileClasspath.extendsFrom(configurations.named("common"))
    configurations.runtimeClasspath.extendsFrom(configurations.named("common"))

    // versioned dependency
    fun verdep(name: String): String {
        return rootProject.property("${minecraftVersion}.$name") as String
    }

    /*if (supportsVanilla) {
        val architectury = project.extensions.getByName<ArchitectPluginExtension>("architectury")

        if (modLoaderName == "" && mcVersionComp >= 1_20_04) // Assume common
            architectury.common("fabric", "forge", "neoforge")
        else if (modLoaderName == "")
            architectury.common("fabric", "forge")
    }*/

    if (supportsVanilla || project.name == "vanilla") {
        apply(plugin = "dev.architectury.loom")
    }

    dependencies {
        if (supportsVanilla) {
            val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

            "minecraft"("com.mojang:minecraft:$minecraftVersion")
            "mappings"(loom.layered() {
                officialMojangMappings()
                parchment("org.parchmentmc.data:parchment-$minecraftVersion:${rootProject.property("${minecraftVersion}.parchment_release")}@zip")
            })
        }

        if (project.name != "common") {
            shadow(implementation(project(":common"))!!)
        }

        if (supportsVanilla) {
            for (priorVersion in getAllPriorVersions(minecraftVersion)) {
                common(project(path = ":$priorVersion-fabric", configuration = "namedElements")) {
                    isTransitive = false
                }

                shadowCommon(project(path = ":$priorVersion-fabric", configuration = "transformProduction$modLoaderName")) {
                    isTransitive = false
                }

                if (modLoaderName != "Fabric") {
                    val lowerModLoader = modLoaderName.lowercase()

                    if (rootProject.subprojects.any { it.name == "$priorVersion-$lowerModLoader" }) {
                        common(project(path = ":$priorVersion-$lowerModLoader", configuration = "namedElements")) {
                            isTransitive = false
                        }

                        shadowCommon(project(path = ":$priorVersion-$lowerModLoader", configuration = "transformProduction$modLoaderName")) {
                            isTransitive = false
                        }
                    }
                }
            }
        }

        if (supportsVanilla || project.name == "vanilla") {
            "include"(implementation("com.moulberry:mixinconstraints:${rootProject.property("mixinconstraints_version")}")!!)
        }

        // Voice Chat APIs
        implementation("de.maxhenkel.voicechat:voicechat-api:${rootProject.property("voicechat_api_version")}")
        compileOnly("su.plo.voice.api:server:${rootProject.property("plasmo_api_version")}")
        compileOnly("su.plo.voice.api:client:${rootProject.property("plasmo_api_version")}")

        // Kotlin
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${rootProject.property("kotlin_serialization_version")}")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${rootProject.property("kotlin_serialization_version")}")
        implementation("org.jetbrains.kotlin:kotlin-reflect:${rootProject.property("kotlin_version")}")

        if (modLoaderName != "Fabric") { // Fabric has FLK, but KFF is unreliable and Bukkit doesn't have a commonly-used Kotlin provider
            shadow("org.jetbrains.kotlin:kotlin-reflect:${rootProject.property("kotlin_version")}")
            shadow("org.jetbrains.kotlin:kotlin-stdlib:${rootProject.property("kotlin_version")}")
            shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${rootProject.property("kotlin_version")}")
            shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${rootProject.property("kotlin_version")}")
            shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.property("kotlin_coroutines_version")}")
            shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${rootProject.property("kotlin_coroutines_version")}")
            shadow("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${rootProject.property("kotlin_coroutines_version")}")
            shadow("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${rootProject.property("kotlin_serialization_version")}")
            shadow("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${rootProject.property("kotlin_serialization_version")}")
        }

        // UnityTranslateLib
        shadow(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib:${rootProject.property("unitytranslatelib_version")}")!!)
        shadow(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${rootProject.property("unitytranslatelib_version")}")!!)
        shadow(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${rootProject.property("unitytranslatelib_version")}")!!)

        shadow(implementation("org.reflections:reflections:0.10.2")!!)
    }

    tasks {
        named<ShadowJar>("shadowJar") {
            configurations = listOf(shadow, shadowCommon)
            archiveClassifier.set("dev-shadow")

            val shadowPkg = "xyz.bluspring.unitytranslate.shaded"

            relocate("org.jetbrains", "$shadowPkg.jetbrains")
            relocate("kotlin", "$shadowPkg.kotlin")
            relocate("kotlinx", "$shadowPkg.kotlinx")
            relocate("org.reflections", "$shadowPkg.reflections")
        }

        processResources {
            val properties = mutableMapOf<String, String>()

            properties["mod_version"] = rootProject.property("mod.version") as String
            if (supportsVanilla) {
                properties["mc_version"] = minecraftVersion
                properties["architectury_version"] = verdep("architectury_version")
                properties["fabric_kotlin_version"] = rootProject.property("fabric_kotlin_version") as String
                properties["loader_version"] = rootProject.property("loader_version") as String
            }

            filesMatching("plugin.yml") {
                expand(properties)
            }

            filesMatching("fabric.mod.json") {
                expand(properties)
            }

            filesMatching("META-INF/mods.toml") {
                expand(properties)
            }

            filesMatching("META-INF/neoforge.mods.toml") {
                expand(properties)
            }
        }
    }
}