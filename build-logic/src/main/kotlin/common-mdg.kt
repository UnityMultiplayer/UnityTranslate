import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import me.modmuss50.mpp.ModPublishExtension
import net.neoforged.moddevgradle.dsl.ModDevExtension
import net.neoforged.moddevgradle.legacyforge.tasks.RemapJar
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

fun Project.configureModDev(extension: ModDevExtension, module: String) {
    val stonecutter = project.extensions.getByType<StonecutterBuildExtension>()
    val mcVersion = stonecutter.current.version
    val parchmentVersion = tryFindParchmentSnapshot(mcVersion)
    if (parchmentVersion != null) {
        extension.parchment {
            minecraftVersion.set(mcVersion)
            mappingsVersion.set(parchmentVersion)
        }
    }

    if (module != "common") {
        extension.runs {
            create("client") {
                client()
            }

            create("server") {
                server()
            }
        }

        val sourceSets = this.extensions.getByName<SourceSetContainer>("sourceSets")

        extension.mods {
            create(mod.id) {
                sourceSet(sourceSets.named("main").get())
            }
        }
    }
}

fun Project.setupCommonModDev(module: String) {
    if (module != "common") {
        project.extensions.configure<ModPublishExtension>("publishMods") {
            if (shouldRemap() && module == "forge") {
                file.set(tasks.named<RemapJar>("reobfJar").get().archiveFile)
            } else {
                file.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
            }
        }

        if (shouldRemap() && module == "forge") {
            tasks.named<RemapJar>("reobfJar") {
                input.set(tasks.named<ShadowJar>("shadowJar").get().archiveFile)
            }

            tasks.named<ShadowJar>("shadowJar") {
                finalizedBy("reobfJar")
            }
        }
    }
}

val Project.klfLangVersion: String
    get() {
        val stonecutter = this.extensions.getByType<StonecutterBuildExtension>()
        val mcVersion = stonecutter.current.version

        return if (stonecutter.eval(mcVersion, "<1.17.1"))
            "1.0"
        else if (stonecutter.eval(mcVersion, "<=1.20.4"))
            "2.0"
        else if (stonecutter.eval(mcVersion, "<=1.21.8"))
            "3.0"
        else
            "3.1"
    }