import dev.deftu.gradle.utils.version.MinecraftVersions

plugins {
    java
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.resources")
    id("dev.deftu.gradle.tools.bloom")
    id("dev.deftu.gradle.tools.shadow")
    id("dev.deftu.gradle.tools.minecraft.loom")
    id("dev.deftu.gradle.tools.minecraft.releases")
}

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

toolkitLoomHelper {
    if (!mcData.isNeoForge) {
        useMixinRefMap("unitytranslate")
    }

    if (mcData.isForge) {
        useTweaker("org.spongepowered.asm.launch.MixinTweaker")
        useForgeMixin("unitytranslate.mixins.json", true)
    }
}

// Enables SVC for use in the dev environment
val USE_SIMPLE_VOICE_CHAT = true

val modLoaderName = mcData.loader.friendlyString

version = "${rootProject.property("mod.version")}+mc${mcData.version}-${modLoaderName.lowercase()}"

// versioned dependency
fun verdep(name: String): String {
    return rootProject.property("${mcData.version}.$name") as String
}

dependencies {
    // Fabric setup
    if (mcData.isFabric) {
        modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
        modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")

        "include"("modImplementation"("me.lucko:fabric-permissions-api:${verdep("fabric_permissions_version")}")!!)
        "modImplementation"("com.terraformersmc:modmenu:${verdep("modmenu_version")}")
    }

    "include"(implementation("com.moulberry:mixinconstraints:${rootProject.property("mixinconstraints_version")}")!!)

    // SVC doesn't have releases for versions between this range on its current version, so
    // use the deprecated version
    val voicechatVersion = if (mcData.version >= MinecraftVersions.VERSION_1_20_4 && mcData.version <= MinecraftVersions.VERSION_1_20_6)
        rootProject.property("voicechat_deprecated_version")
    else
        rootProject.property("voicechat_version")

    // Add Simple Voice Chat and other common dependencies
    val architecturyVersion = verdep("architectury_version")

    if (modLoaderName != "") {
        "modCompileOnly"("maven.modrinth:simple-voice-chat:${modLoaderName.lowercase()}-${mcData.version}-${voicechatVersion}")

        if (USE_SIMPLE_VOICE_CHAT) {
            "modRuntimeOnly"("maven.modrinth:simple-voice-chat:${modLoaderName.lowercase()}-${mcData.version}-${voicechatVersion}")
        }

        if (mcData.version <= MinecraftVersions.VERSION_1_16_5)
            "modImplementation"("me.shedaniel:architectury-${modLoaderName.lowercase()}:${architecturyVersion}")
        else if (mcData.version < MinecraftVersions.VERSION_1_20_4 || !mcData.isForge) // Architectury is not available for LexForge 1.20.4 and above.
            "modImplementation"("dev.architectury:architectury-${modLoaderName.lowercase()}:${architecturyVersion}")
    }

    val playwrightVersion = rootProject.property("playwright_version")
    "shade"("implementation"("com.microsoft.playwright:playwright:${playwrightVersion}")!!)
}

tasks {
    processResources {
        val properties = mutableMapOf<String, String>()
        properties["mod_version"] = rootProject.property("mod.version") as String
        properties["mc_version"] = mcData.version.toString()

        if (mcData.isFabric) {
            properties["loader_version"] = mcData.dependencies.fabric.fabricLoaderVersion
            properties["fabric_version"] = mcData.dependencies.fabric.fabricApiVersion
            properties["fabric_kotlin_version"] = mcData.dependencies.fabric.fabricLanguageKotlinVersion
        }

        properties["architectury_version"] = verdep("architectury_version")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        if (mcData.version >= MinecraftVersions.VERSION_1_20_6)
            rename("unitytranslate.mixins.1.20.6.json", "unitytranslate.mixins.json")
        else
            rename("unitytranslate.mixins.1.18.2.json", "unitytranslate.mixins.json")

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