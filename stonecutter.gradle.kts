import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

plugins {
    id("idea")
    id("dev.kikugie.stonecutter")

    // Architectury Loom was becoming annoying.
    id("fabric-loom") apply false
//    id("net.minecraftforge.gradle") version "[6.0,6.2)" apply false
    id("net.neoforged.moddev") version "2.0.134" apply false

    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false

    id("com.gradleup.shadow") version "9.3.0" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.7.+" apply false
}

stonecutter active "1.20.1"

allprojects {
    group = mod.group

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
        maven("https://maven.shedaniel.me/")
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

    if (project.extensions.findByType<StonecutterBuildExtension>() == null)
        return@allprojects

    val sc = project.extensions.getByType<StonecutterBuildExtension>()
    val common = sc.node.sibling("")

    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.gradleup.shadow")

    val minecraftVersion = sc.current.version

    val loader = try { project.property("loom.platform") as? String? } catch (_: Throwable) { null } ?: "unknown"

    project.extensions.configure<BasePluginExtension>("base") {
        archivesName.set(mod.name)
    }

    version = "${mod.version}+$minecraftVersion-$loader"

    sc.constants.match(
        loader,
        "fabric", "forge", "neoforge", "unknown"
    )

    sc.constants["forge_like"] = loader == "forge" || loader == "neoforge"

    fun loaderDep(dep: String): Any {
        return common?.project?.mod?.dep(dep) ?: mod.dep(dep, "[UNSUPPORTED]")
    }

    dependencies {
        "implementation"("org.java-websocket:Java-WebSocket:${loaderDep("java_websocket")}") {
            exclude("org.slf4j", "slf4j-api")
        }
        "implementation"("com.squareup.okhttp3:okhttp:${loaderDep("okhttp")}") {
            exclude("kotlin")
            exclude("org.jetbrains")
        }

        "implementation"("xyz.bluspring.unitytranslate:UnityTranslateLib:${loaderDep("unitytranslatelib")}")
        "implementation"("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${loaderDep("unitytranslatelib")}")
        "implementation"("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${loaderDep("unitytranslatelib")}")

        "implementation"("com.github.jnr:jnr-ffi:${loaderDep("jnr")}")

        "implementation"("xyz.bluspring.modernnetworking:modernnetworking-api:${loaderDep("modernnetworking")}")!!
    }

    project.extensions.configure<KotlinBaseExtension>("kotlin") {
        jvmToolchain(
            if (sc.eval(minecraftVersion, ">=1.20.5"))
                21
            else 17
        )
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

    tasks.named<ShadowJar>("shadowJar") {
        mergeServiceFiles()

        val shadowPackage = "${mod.group}.${mod.id}.shaded"

        exclude("org/jetbrains/**/*", "org/intellij/**/*", "org/slf4j/**/*", "kotlin/**/*", "kotlinx/**/*")
        relocate("com.mayakapps.kache", "$shadowPackage.kache")

        if (loader.contains("forge")) {
            relocate("org.java_websocket", "$shadowPackage.java_websocket")
            relocate("okhttp3", "$shadowPackage.okhttp3")
            relocate("jnr", "$shadowPackage.jnr")
        }
    }

    // IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
    idea {
        module {
            isDownloadSources = true
            isDownloadJavadoc = true
        }
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