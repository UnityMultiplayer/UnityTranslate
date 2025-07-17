import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.18.2" /* [SC] DO NOT EDIT */

// Enables SVC for use in the dev environment
val USE_SIMPLE_VOICE_CHAT = true

subprojects {
    if (project.extensions.findByName("stonecutter") == null)
        return@subprojects

    project.extensions.configure(StonecutterBuildExtension::class.java) {
        constants.match(project.findProperty("loom.platform") as? String ?: "fabric", "fabric", "neoforge", "forge")
    }

    val sc = (project.extensions.getByName("stonecutter") as StonecutterBuildExtension)
    val common = sc.node.sibling("")

    apply(plugin = "architectury-plugin")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "com.gradleup.shadow")

    val mcVersion = sc.current.version
    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

    loom.silentMojangMappingsLicense()
    loom.decompilers {
        get("vineflower").apply { // Adds names to lambdas - useful for mixins
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    loom.mixin.useLegacyMixinAp = false

    val modLoaderName = prop("loom.platform") ?: "fabric"

    dependencies {
        "minecraft"("com.mojang:minecraft:$mcVersion")
        "mappings"(loom.layered() {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$mcVersion:${common?.project?.mod?.prop("parchment_snapshot") ?: mod.prop("parchment_snapshot")}")
        })

        "shadow"("implementation"("org.seleniumhq.selenium:selenium-java:${mod.dep("selenium")}")!!)
        "runtimeOnly"("dev.architectury:architectury-transformer:5.2.9999")

        "include"("implementation"("com.moulberry:mixinconstraints:${mod.dep("mixinconstraints")}")!!)

        if (USE_SIMPLE_VOICE_CHAT) {
            "modRuntimeOnly"("maven.modrinth:simple-voice-chat:$modLoaderName-$mcVersion-${common?.project?.mod?.dep("voicechat") ?: mod.dep("voicechat")}")
        }

        "shadow"("implementation"("gg.essential:elementa:${mod.dep("elementa")}")!!)
        "shadow"("modImplementation"("gg.essential:universalcraft-${if (common?.project?.prop("deps.universalcraft_mc") != null) 
            common.project.mod.dep("universalcraft_mc") 
        else if (prop("deps.universalcraft_mc") != null)
            mod.dep("universalcraft_mc")
        else 
            mcVersion
        }-${modLoaderName}:${mod.dep("universalcraft")}")!!)
    }

    project.extensions.configure(KotlinProjectExtension::class.java) {
        jvmToolchain(if (sc.eval(mcVersion, ">=1.20.5")) 21 else 17)
    }

    project.extensions.configure<JavaPluginExtension>("java") {
        val java = if (sc.eval(mcVersion, ">=1.20.5"))
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

    tasks {
        named<ProcessResources>("processResources") {
            val properties = mutableMapOf<String, String>()
            properties["mod_version"] = rootProject.property("mod.version") as String
            properties["mc_version"] = mcVersion

            if (modLoaderName == "fabric") {
                properties["loader_version"] = mod.dep("fabric_loader")
                properties["fabric_version"] = mod.dep("fabric_api")
                properties["fabric_kotlin_version"] = mod.dep("fabric_kotlin")
            }

            properties["modernnetworking_version"] = mod.dep("modernnetworking")

            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

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

        named<ShadowJar>("shadowJar") {
            val shadowPkg = "xyz.bluspring.unitytranslate.minecraft.shaded"

            relocate("gg.essential.elementa", "$shadowPkg.elementa")
            relocate("gg.essential.universalcraft", "$shadowPkg.elementa")
        }
    }

    val properLoaderName = when (project.prop("loom.platform")) {
        "fabric" -> "Fabric"
        "forge" -> "Forge"
        "neoforge" -> "NeoForge"
        else -> ""
    }
}