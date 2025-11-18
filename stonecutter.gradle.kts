import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("dev.kikugie.stonecutter")

    id("dev.architectury.loom") version "1.13-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false

    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false

    id("com.gradleup.shadow") version "8.3.5" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.7.+" apply false
}

stonecutter active "1.20.1"

allprojects {
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
        maven("https://maven.maxhenkel.de/repository/public")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.nucleoid.xyz/")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.fabricmc.net")

        maven("https://repo.plo.su")
        maven("https://repo.plasmoverse.com/releases")
        maven("https://repo.plasmoverse.com/snapshots")
        maven("https://repo.nyon.dev/releases")
        maven("https://mvn.devos.one/releases")
        maven("https://mvn.devos.one/snapshots")
    }

}

subprojects {
    if (project.extensions.findByName("stonecutter") == null)
        return@subprojects

    if (parent == rootProject)
        return@subprojects

    val sc = project.extensions.getByType<StonecutterBuildExtension>()
    val common = sc.node.sibling("")

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "architectury-plugin")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "com.gradleup.shadow")

    val minecraftVersion = sc.current.version
    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

    loom.silentMojangMappingsLicense()
    loom.decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    loom.mixin.useLegacyMixinAp = false

    dependencies {
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
        "mappings"(loom.officialMojangMappings())
    }

    project.extensions.configure<JavaPluginExtension>("java") {
        val java = if (sc.eval(minecraftVersion, ">=1.20.5"))
            JavaVersion.VERSION_21 else JavaVersion.VERSION_17
        targetCompatibility = java
        sourceCompatibility = java
    }

    tasks.named<Jar>("jar") {
        archiveClassifier = "dev"
    }

    tasks.named<RemapJarTask>("remapJar") {
        injectAccessWidener = true
        input = tasks.named<Jar>("shadowJar").get().archiveFile
        archiveClassifier = null
        dependsOn(tasks.named<Jar>("shadowJar"))
    }
}

// Runs active versions for each loader
for (it in stonecutter.tree.nodes) {
    if (it.metadata != stonecutter.current || it.branch.id.isEmpty()) continue
    val types = listOf("Client", "Server")
    val loader = it.branch.id.upperCaseFirst()
    for (type in types) it.project.tasks.register("runActive$type$loader") {
        group = "project"
        dependsOn("run$type")
    }
}