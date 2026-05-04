import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import me.modmuss50.mpp.ModPublishExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

fun Project.setupCommonLoom(module: String) {
    val stonecutter = this.extensions.getByType<StonecutterBuildExtension>()
    val loom = this.extensions.getByType<LoomGradleExtensionAPI>()

    dependencies {
        "minecraft"("com.mojang:minecraft:${stonecutter.current.version}")

        if (shouldRemap()) {
            "mappings"(loom.layered {
                officialMojangMappings()

                val parchmentVersion = tryFindParchmentSnapshot(stonecutter.current.version)
                if (parchmentVersion != null) {
                    parchment("org.parchmentmc.data:parchment-${stonecutter.current.version}:$parchmentVersion@zip")
                }
            })
        }
    }

    if (module != "common") {
        if (shouldRemap()) {
            tasks.named<RemapJarTask>("remapJar") {
                dependsOn("shadowJar")

                inputFile.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
                archiveClassifier.set(null)
            }
        }

        project.extensions.configure<ModPublishExtension>("publishMods") {
            if (shouldRemap()) {
                file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)
            } else {
                file.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
            }
        }
    }
}

fun Project.moddedImplementation(notation: Any): Dependency? {
    return if (this.shouldRemap()) {
        this.dependencies.add("modImplementation", notation)
    } else {
        this.dependencies.add("implementation", notation)
    }
}

fun Project.moddedCompileOnly(notation: Any): Dependency? {
    return if (this.shouldRemap()) {
        this.dependencies.add("modCompileOnly", notation)
    } else {
        this.dependencies.add("compileOnly", notation)
    }
}

fun Project.moddedRuntimeOnly(notation: Any): Dependency? {
    return if (this.shouldRemap()) {
        this.dependencies.add("modRuntimeOnly", notation)
    } else {
        this.dependencies.add("runtimeOnly", notation)
    }
}

fun Project.moddedApi(notation: Any): Dependency? {
    return if (this.shouldRemap()) {
        this.dependencies.add("modApi", notation)
    } else {
        this.dependencies.add("api", notation)
    }
}