import dev.kikugie.fletching_table.extension.FletchingTableExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    id("dev.kikugie.stonecutter")
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.mod.publish) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.fletching.table) apply false
    id("idea")
}

stonecutter active "26.1"


allprojects {
    apply(plugin = "idea")

    repositories {
        mavenCentral()
        maven("https://maven.parchmentmc.org")
        maven("https://repo.nyon.dev/releases")
        maven("https://maven.fabricmc.net")
        maven("https://libraries.minecraft.net")
        maven("https://mvn.devos.one/releases")
        maven("https://repo.essential.gg/repository/maven-public")
    }

    group = mod.group
    version = mod.version

    // IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
    idea {
        module {
            isDownloadSources = true
            isDownloadJavadoc = true
        }
    }
}

subprojects {
    if (project.extensions.findByName("stonecutter") == null)
        return@subprojects

    //if (parent == rootProject)
        //return@subprojects

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "dev.kikugie.fletching-table")

    project.extensions.configure<FletchingTableExtension>("fletchingTable") {
        j52j.register("main") {
            extension("json", "*.mixins.json5")
        }
    }

    project.extensions.configure<JavaPluginExtension>("java") {
        withSourcesJar()
        withJavadocJar()

        val java = JavaVersion.toVersion(project.minimumJavaVersion)
        targetCompatibility = java
        sourceCompatibility = java
    }

    project.extensions.configure<KotlinProjectExtension>("kotlin") {
        jvmToolchain(project.minimumJavaVersion)
    }

    tasks.named<Jar>("jar") {
        archiveClassifier = "dev"
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
