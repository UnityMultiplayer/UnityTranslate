plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val minecraftVersion = stonecutter.current.version
val loader = try { project.property("loom.platform") as? String? } catch (_: Throwable) { null } ?: "unknown"

version = "${mod.version}+$minecraftVersion-$loader"
group = mod.group

base {
    archivesName.set(mod.name)
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
}

stonecutter {
    constants.match(
        loader,
        "fabric", "forge", "neoforge", "unknown"
    )

    constants["forge_like"] = loader == "forge" || loader == "neoforge"
}

/*
version = "${project.property("mod.version")}+mc${mcData.version}-${mcData.loader.friendlyString}"

val leastCommonMcVersion = when (mcData.version) {
    MinecraftVersions.VERSION_1_21_10 -> "1.21.9"
    MinecraftVersions.VERSION_1_21_8 -> "1.21.6"
    MinecraftVersions.VERSION_1_21_4 -> "1.21.3"
    MinecraftVersions.VERSION_1_21_1 -> "1.21"
    else -> mcData.version.toString()
}

val architecturyVersion = when (mcData.version) {
    MinecraftVersions.VERSION_1_20_1 -> "9.2.14"
    MinecraftVersions.VERSION_1_20_4 -> "11.1.17"
    MinecraftVersions.VERSION_1_20_6 -> "12.1.4"
    MinecraftVersions.VERSION_1_21_1 -> "13.0.6"
    MinecraftVersions.VERSION_1_21_4 -> "15.0.3"
    MinecraftVersions.VERSION_1_21_5 -> "16.1.4"
    MinecraftVersions.VERSION_1_21_8 -> "17.0.8"
    MinecraftVersions.VERSION_1_21_10 -> "18.0.6"

    else -> throw IllegalStateException()
}

val klfVersion = when (mcData.version) {
    MinecraftVersions.VERSION_1_20_1, MinecraftVersions.VERSION_1_20_4 -> "2.0"
    MinecraftVersions.VERSION_1_21_1, MinecraftVersions.VERSION_1_21_4, MinecraftVersions.VERSION_1_21_5, MinecraftVersions.VERSION_1_21_8 -> "3.0"
    MinecraftVersions.VERSION_1_21_10 -> "3.1"

    else -> throw IllegalStateException()
}

val fabricPermissionsApiVersion = when (mcData.version) {
    MinecraftVersions.VERSION_1_20_1 -> "0.3.1"
    MinecraftVersions.VERSION_1_21_1, MinecraftVersions.VERSION_1_21_4, MinecraftVersions.VERSION_1_21_5 -> "0.3.3"
    MinecraftVersions.VERSION_1_21_8 -> "0.4.1"
    MinecraftVersions.VERSION_1_21_10 -> "0.5.0"

    else -> throw IllegalStateException()
}

dependencies {
    implementation("de.maxhenkel.voicechat:voicechat-api:${project.property("voicechat_api_version")}")
    compileOnly("su.plo.voice.api:server:${project.property("plasmo_api_version")}")
    compileOnly("su.plo.voice.api:client:${project.property("plasmo_api_version")}")

    modApi("dev.architectury:architectury-${mcData.loader.friendlyString}:$architecturyVersion")

    if (mcData.isFabric) {
        val modMenuVersion = when (mcData.version) {
            MinecraftVersions.VERSION_1_20_1 -> "7.2.2"
            MinecraftVersions.VERSION_1_21_1 -> "11.0.2"
            MinecraftVersions.VERSION_1_21_4 -> "13.0.3"
            MinecraftVersions.VERSION_1_21_5 -> "14.0.0"
            MinecraftVersions.VERSION_1_21_8 -> "15.0.0"
            MinecraftVersions.VERSION_1_21_10 -> "16.0.0-rc.1"

            else -> throw IllegalStateException()
        }

        modImplementation("com.terraformersmc:modmenu:$modMenuVersion")
    }

    if (mcData.isFabric) {
        includeOrShade(modImplementation("me.lucko:fabric-permissions-api:$fabricPermissionsApiVersion")!!)
    }

    val useSVC = true

    if (useSVC)
        modRuntimeOnly("maven.modrinth:simple-voice-chat:${mcData.loader.friendlyString}-${mcData.version}-${project.property("voicechat_version")}")
    else if (!mcData.isNeoForge) {
        modRuntimeOnly("maven.modrinth:plasmo-voice:${mcData.loader.friendlyString}-${leastCommonMcVersion}-${project.property("plasmo_version")}")
        runtimeOnly("su.plo.voice.api:server:${project.property("plasmo_api_version")}")
        runtimeOnly("su.plo.voice.api:client:${project.property("plasmo_api_version")}")
    }

    val clothConfigVersion = when(mcData.version) {
        MinecraftVersions.VERSION_1_20_1 -> "11.1.118"
        MinecraftVersions.VERSION_1_21_1 -> "15.0.128"
        MinecraftVersions.VERSION_1_21_4 -> "17.0.144"
        MinecraftVersions.VERSION_1_21_5 -> "18.0.145"
        MinecraftVersions.VERSION_1_21_8 -> "19.0.147"
        MinecraftVersions.VERSION_1_21_10 -> "20.0.149"

        else -> throw IllegalStateException()
    }

    modImplementation("maven.modrinth:cloth-config:${clothConfigVersion}+${mcData.loader.friendlyString}")
    modImplementation("maven.modrinth:talk-balloons:${project.property("talk_balloons_version")}+${if (mcData.version == MinecraftVersions.VERSION_1_21_1) "1.21.1" else leastCommonMcVersion}-${mcData.loader.friendlyString}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:${project.property("kotlin_serialization_version")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${project.property("kotlin_serialization_version")}")

    if (mcData.isFabric) {
        modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
        modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")
    } else {
        modImplementation("dev.nyon:KotlinLangForge:${property("kotlinlangforge_version")}-k${property("kotlin_version")}-${klfVersion}+${mcData.loader.friendlyString}")
    }

    val jws = includeOrShade("org.java-websocket:Java-WebSocket:1.5.7")!!

    implementation(jws)
    if (mcData.isForgeLike) {
        minecraftRuntimeLibraries(jws)
    }

    val okhttp = shade(implementation("com.squareup.okhttp3:okhttp:${project.property("okhttp_version")}") {
        exclude("kotlin")
        exclude("org.jetbrains")
    })
    if (mcData.isForgeLike) {
        minecraftRuntimeLibraries(okhttp!!)
    }

    // UnityTranslateLib
    shade(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib:${rootProject.property("unitytranslatelib_version")}")!!)
    shade(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${rootProject.property("unitytranslatelib_version")}")!!)
    shade(implementation("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${rootProject.property("unitytranslatelib_version")}")!!)

    includeOrShade(api("com.github.jnr:jnr-ffi:${rootProject.property("jnr_version")}")!!)
}

toolkitReleases {
    detectVersionType.set(true)
    releaseName.set("[${mcData.version}] UnityTranslate ${modData.version} (${mcData.loader.friendlyName})")

    modrinth {
        projectId.set("yA7uge1H")

        if (mcData.loader == ModLoader.FABRIC) {
            dependencies.add(ModDependency("Ha28R6CL", DependencyType.REQUIRED)) // Fabric Language Kotlin
        } else if (mcData.isForgeLike) {
            dependencies.add(ModDependency("1vrSzlao", DependencyType.REQUIRED)) // KotlinLangForge (it's so much better oh my GOD)
        }

        dependencies.addAll(listOf(
            ModDependency("lhGA9TYQ", DependencyType.REQUIRED), // Architectury API
            ModDependency("l3tS9WUS", DependencyType.OPTIONAL), // Talk Balloons
            ModDependency("9eGKb6K1", DependencyType.OPTIONAL), // Simple Voice Chat
            ModDependency("1bZhdhsH", DependencyType.OPTIONAL), // Plasmo Voice
        ))
    }

    curseforge {
        projectId.set("1093604")

        if (mcData.loader == ModLoader.FABRIC) {
            relations.add(CurseRelation("fabric-language-kotlin", CurseRelationType.REQUIRED)) // Fabric Language Kotlin
        } else if (mcData.isForgeLike) {
            relations.add(CurseRelation("kotlinlangforge", CurseRelationType.REQUIRED)) // KotlinLangForge
        }

        relations.addAll(listOf(
            CurseRelation("architectury-api", CurseRelationType.REQUIRED), // Architectury API
            CurseRelation("talk-balloons", CurseRelationType.OPTIONAL), // Talk Balloons
            CurseRelation("simple-voice-chat", CurseRelationType.OPTIONAL), // Simple Voice Chat
            CurseRelation("plasmo-voice", CurseRelationType.OPTIONAL), // Plasmo Voice
        ))
    }

    changelogFile.set(File(project.rootDir, "CHANGELOG.md"))
}

tasks {
    processResources {
        val properties = mutableMapOf<String, String>()

        properties.putAll(mapOf(
            "mod_version" to modData.version,
            "mc_version" to mcData.version.toString(),
            "architectury_version" to architecturyVersion,
        ))

        val forgeLoaderVersion: String? = run {
            if (!mcData.isPresent) {
                return@run null
            }

            if (!mcData.isForgeLike) {
                return@run null
            }

            if (mcData.isLegacyForge) {
                return@run null
            }

            "[${klfVersion.split(".")[0]},)"
        }

        if (mcData.isForgeLike) {
            properties["forge_kotlin_version"] = klfVersion
            properties["forge_loader_version"] = forgeLoaderVersion!!
            properties["mod_loader_name"] = mcData.loader.friendlyString

            properties["forge_loader"] = "klf"

            if (mcData.isForge) {
                properties["FUCKING_REQUIRED"] = "mandatory=true"
            } else {
                properties["FUCKING_REQUIRED"] = "type=\"required\""
            }
        }

        if (mcData.isFabric) {
            properties["fabric_kotlin_version"] = mcData.dependencies.fabric.fabricLanguageKotlinVersion
            properties["loader_version"] = mcData.dependencies.fabric.fabricLoaderVersion

            exclude("META-INF/mods.toml")
            exclude("META-INF/neoforge.mods.toml")
        }

        for ((key, value) in properties) {
            inputs.property(key, value)
        }

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(properties)
        }

        filesMatching("META-INF/mods.toml") {
            expand(properties)
        }

        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    fatJar {
        relocate("okhttp3", "xyz.bluspring.unitytranslate.shaded.okhttp3")
        relocate("okio", "xyz.bluspring.unitytranslate.shaded.okio")
        exclude("kotlin/**/*", "org/**/*")
    }
}
*/