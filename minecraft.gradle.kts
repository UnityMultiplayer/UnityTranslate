import net.fabricmc.loom.bootstrap.LoomGradlePluginBootstrap
import net.fabricmc.loom.configuration.FabricApiExtension

plugins {
    id("dev.architectury.loom")
}

// Enables SVC for use in the dev environment
val USE_SIMPLE_VOICE_CHAT = true

val supportsVanilla = project.name != "bukkit" && project.name != "common" && project.name != "vanilla"
val modLoaderName = if (project.name.endsWith("fabric"))
    "Fabric"
else if (project.name.endsWith("neoforge"))
    "NeoForge"
else if (project.name.endsWith("forge"))
    "Forge"
else ""

val minecraftVersion = if (project.name == "fabric")
    "1.16.5"
else
    project.name.replaceAfter("-", "").removeSuffix("-")

val mcVersionSplit = if (minecraftVersion.contains(".")) minecraftVersion.split(".").map { it.toInt() } else emptyList()
val mcVersionComp = mcVersionSplit.joinToString("") { it.toString().padStart(2, '0') }.toIntOrNull() ?: -1

extra.set("loom.platform", modLoaderName.lowercase())
apply<LoomGradlePluginBootstrap>()

version = "${rootProject.property("mod.version")}+mc${minecraftVersion}-${modLoaderName.lowercase()}"

val common = project.configurations.getByName("common")
val shadowCommon = project.configurations.getByName("shadowCommon")

// versioned dependency
fun verdep(name: String): String {
    return rootProject.property("${minecraftVersion}.$name") as String
}

// Setup modloader stuff
if (modLoaderName != "") {
    if (mcVersionSplit.isNotEmpty()) {
        architectury.platformSetupLoomIde()
    }

    if (modLoaderName == "Fabric") {
        architectury.fabric()
    } else if (modLoaderName == "Forge") {
        architectury.forge()
    } else {
        architectury.neoForge()
    }

    configurations.getByName("development$modLoaderName").extendsFrom(common)
}

loom {
    if (modLoaderName == "Forge") {
        forge {
            mixinConfigs("unitytranslate.vanilla.mixins.json", "unitytranslate.forge.mixins.json")
        }
    }
}

dependencies {
    common(project(path = ":vanilla", configuration = "namedElements")) {
        isTransitive = false
    }

    shadowCommon(project(path = ":vanilla", configuration = "transformProduction$modLoaderName")) {
        isTransitive = false
    }

    // Package Fabric API's base for the sake of events.
    val fabricApi = project.extensions.getByName<FabricApiExtension>("fabricApi")
    if (!minecraftVersion.contains(".")) { // Use the oldest version of Fabric API
        "modImplementation"(fabricApi.module("fabric-api-base", "0.42.0+1.16"))
    } else if (modLoaderName != "Fabric" && modLoaderName != "") {
        // Use Forgified Fabric API for this, hopefully it works fine?
        if (mcVersionSplit[1] >= 21) {
            "include"("modImplementation"("dev.su5ed.sinytra.fabric-api:fabric-api-base:0.4.31+ef105b4977")!!)
        } else {
            "include"("modImplementation"("dev.su5ed.sinytra.fabric-api:fabric-api-base:0.4.27+3f71c344f4")!!)
        }
    }

    // Add Fabric Loader
    if (project.name.endsWith("fabric") || project.name.endsWith("-common")) {
        "modImplementation"("net.fabricmc:fabric-loader:${rootProject.property("loader_version")}")
    }

    // Fabric setup
    if (project.name.endsWith("-fabric")) {
        "modImplementation"("net.fabricmc.fabric-api:fabric-api:${verdep("fabric_version")}")
        "modImplementation"("net.fabricmc:fabric-language-kotlin:${rootProject.property("fabric_kotlin_version")}")

        "include"("modImplementation"("me.lucko:fabric-permissions-api:${verdep("fabric_permissions_version")}")!!)
        "modImplementation"("com.terraformersmc:modmenu:${verdep("modmenu_version")}")
    }

    // Forge-like setup
    if (supportsVanilla && minecraftVersion.contains(".")) {
        if (modLoaderName == "Forge") { // Forge setup
            "forge"("net.minecraftforge:forge:${minecraftVersion}-${verdep("forge_version")}")
        } else if (modLoaderName == "NeoForge") { // Fabric setup
            "neoForge"("net.neoforged:neoforge:${verdep("neoforge_version")}")
        }
    }

    // SVC doesn't have releases for versions between this range on its current version, so
    // use the deprecated version
    val voicechatVersion = if (mcVersionComp >= 1_20_04 && mcVersionComp <= 1_20_06)
        rootProject.property("voicechat_deprecated_version")
    else
        rootProject.property("voicechat_version")

    // Add Simple Voice Chat and other common dependencies
    if (supportsVanilla && minecraftVersion.contains(".")) {
        val architecturyVersion = verdep("architectury_version")

        if (modLoaderName != "") {
            "modCompileOnly"("maven.modrinth:simple-voice-chat:${modLoaderName.lowercase()}-${minecraftVersion}-${voicechatVersion}")

            if (USE_SIMPLE_VOICE_CHAT) {
                "modRuntimeOnly"("maven.modrinth:simple-voice-chat:${modLoaderName.lowercase()}-${minecraftVersion}-${voicechatVersion}")
            }

            if (mcVersionSplit[1] == 16)
                "modImplementation"("me.shedaniel:architectury-${modLoaderName.lowercase()}:${architecturyVersion}")
            else if (mcVersionComp < 1_20_04 || modLoaderName != "Forge") // Architectury is not available for LexForge 1.20.4 and above.
                "modImplementation"("dev.architectury:architectury-${modLoaderName.lowercase()}:${architecturyVersion}")
        } else {
            // Add common Architectury
            if (mcVersionSplit[1] == 16)
                "modImplementation"("me.shedaniel:architectury:${architecturyVersion}")
            else
                "modImplementation"("dev.architectury:architectury:${architecturyVersion}")
        }
    }
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}