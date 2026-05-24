import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.gson.JsonParser
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import egt.RelocationTransform
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.NodeChildren
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import java.net.URI

fun Project.setupCommon(module: String, isMod: Boolean = true) {
    val stonecutter = project.extensions.getByType<StonecutterBuildExtension>()

    version = "${mod.version}+${stonecutter.current.version}"

    stonecutter.constants.match(module, "fabric", "forge", "neoforge",
        "common" // do not use lmao
    )

    if (module != "common" && module != "elementa") {
        val common = stonecutter.node.sibling("") ?: return
        val commonProj = project.project(":common:${stonecutter.current.version}")

        dependencies {
            "compileOnly"(commonProj.extensions.getByName<SourceSetContainer>("sourceSets").named("main").get().output)
        }

        val properLoaderName = when (module) {
            "fabric" -> "Fabric"
            "forge" -> "Forge"
            "neoforge" -> "NeoForge"
            else -> ""
        }

        val base = project.extensions.getByType<BasePluginExtension>()
        base.archivesName.set("${common.project.mod.name}-${module}")

        // setup publishing to Modrinth and such
        apply(plugin = "me.modmuss50.mod-publish-plugin")
        project.extensions.configure<ModPublishExtension>("publishMods") {
            displayName = "${common.project.mod.version}+${stonecutter.current.version} ($properLoaderName)"
            version = "${common.project.mod.version}+${stonecutter.current.version}-$module"
            changelog = ""
            type = ReleaseType.STABLE
            modLoaders.add(module)

            dryRun = providers.environmentVariable("MODRINTH_TOKEN")
                .getOrNull() == null || providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

            modrinth {
                projectId = rootProject.property("publishing.modrinth").toString()
                accessToken = providers.environmentVariable("MODRINTH_TOKEN")

                minecraftVersions.addAll(common.project.mod.prop("supported_versions").split(","))

                if (project.path.contains("fabric")) {
                    requires {
                        slug = "fabric-api"
                    }
                    requires {
                        slug = "fabric-language-kotlin"
                    }
                } else if (project.path.contains("forge")) {
                    requires {
                        slug = "kotlin-lang-forge"
                    }
                }
            }

            curseforge {
                projectId = rootProject.property("publishing.curseforge").toString()
                accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")

                minecraftVersions.addAll(common.project.mod.prop("supported_versions").split(","))

                if (module == "fabric") {
                    requires {
                        slug = "fabric-api"
                    }
                    requires {
                        slug = "fabric-language-kotlin"
                    }
                } else if (module.contains("forge")) {
                    requires {
                        slug = "kotlinlangforge"
                    }
                }
            }
        }

        setupCommonUnmodded(module, commonProj)
    }

    val commonProject = stonecutter.node.sibling("")?.project ?: this

    tasks.named<ProcessResources>("processResources") {
        properties(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml"),
            "mod_id" to commonProject.mod.id,
            "mod_name" to commonProject.mod.name,
            "mod_description" to commonProject.mod.description,
            "mod_version" to commonProject.mod.version,
            "mod_sources" to commonProject.mod.sources,

            "minecraft_version_range_fabric" to commonProject.mod.prop("minecraft_version_range_fabric"),
            "minecraft_version_range_forge" to commonProject.mod.prop("minecraft_version_range_forge"),
        )
    }
}

fun Project.setupCommonUnmodded(module: String, commonProj: Project? = null, javaVersion: Int = project.minimumJavaVersion) {
    project.extensions.configure<BasePluginExtension>("base") {
        archivesName.set("${mod.name}-$module")
    }

    val shadedDep by configurations.creating

    project.extensions.configure<JavaPluginExtension>("java") {
        withSourcesJar()
        withJavadocJar()

        val java = JavaVersion.toVersion(javaVersion)
        targetCompatibility = java
        sourceCompatibility = java
    }

    project.extensions.configure<KotlinProjectExtension>("kotlin") {
        jvmToolchain(javaVersion)
    }

    tasks.named<Jar>("jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        if (commonProj != null)
            from(zipTree(commonProj.tasks.named<Jar>("jar").get().archiveFile))
        archiveClassifier = "dev"
    }

    tasks.named<Jar>("sourcesJar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        if (commonProj != null)
            from(zipTree(commonProj.tasks.named<Jar>("sourcesJar").get().archiveFile))
    }

    tasks.named<ShadowJar>("shadowJar") {
        configurations = listOf(shadedDep)

        if (!shouldRemap() || module == "neoforge") {
            archiveClassifier = null
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        if (commonProj != null)
            from(zipTree(commonProj.tasks.named<Jar>("jar").get().archiveFile))

        relocate("gg.essential", "xyz.bluspring.unitytranslate.shaded.essential")
    }

    tasks.named<ProcessResources>("processResources") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        if (commonProj != null)
            from(commonProj.extensions.getByName<SourceSetContainer>("sourceSets").named("main").get().resources)
    }

    tasks.register<Copy>("buildAndCollect") {
        group = "versioned"
        description = "Must run through 'chiseledBuild'"

        if (shouldRemap() && module == "fabric")
            from(tasks.named<Jar>("remapJar").get().archiveFile)
        else if (module == "forge")
            from(tasks.named<Jar>("reobfJar").get().archiveFile)
        else
            from(tasks.named<Jar>("shadowJar").get().archiveFile)
        into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$module"))
        dependsOn("build")
    }
}

val cachedAvailableParchment = mutableMapOf<String, String?>()
val cachedNeoForm = mutableMapOf<String, String>()

fun tryFindParchmentSnapshot(version: String): String? {
    if (cachedAvailableParchment.contains(version))
        return cachedAvailableParchment[version]

    return try {
        val metadata = URI("https://maven.parchmentmc.org/org/parchmentmc/data/parchment-$version/maven-metadata.xml").toURL().openStream()
            .use { XmlSlurper().parse(it) }

        (metadata.getProperty("versioning") as NodeChildren).getProperty("release").toString()
    } catch (_: Throwable) {
        null
    }.apply {
        cachedAvailableParchment[version] = this
    }
}

fun tryFindNeoFormVersion(version: String): String? {
    if (cachedNeoForm.contains(version))
        return cachedNeoForm[version]

    return try {
        val json = URI("https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoform?sorted=true").toURL()
            .run { JsonParser.parseString(this.readText()) }.asJsonObject

        val versions = json.getAsJsonArray("versions").toList().reversed().map { it.asString }
        for (neoFormVersion in versions) {
            if (neoFormVersion.startsWith("$version-")) {
                return neoFormVersion.apply {
                    cachedNeoForm[version] = this
                }
            }
        }

        println(versions)
        null
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

fun Project.shouldRemap(): Boolean {
    val stonecutter = this.extensions.findByType<StonecutterBuildExtension>() ?: return false
    return stonecutter.eval(stonecutter.current.version, "<=1.21.11")
}

val Project.minimumJavaVersion: Int
    get() {
        val stonecutter = this.extensions.getByType<StonecutterBuildExtension>()
        return if (stonecutter.eval(stonecutter.current.version, ">1.21.11"))
            25
        else if (stonecutter.eval(stonecutter.current.version, ">=1.20.5"))
            21
        else if (stonecutter.eval(stonecutter.current.version, ">=1.18"))
            17
        else if (stonecutter.eval(stonecutter.current.version, ">=1.17")) // jail :(
            16
        else
            8
    }

fun Project.shadedDep(notation: Any): Dependency? {
    return this.dependencies.add("shadedDep", notation)
}
