plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val minecraftVersion = stonecutter.current.version

dependencies {
    modCompileOnly("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")
    modCompileOnly("net.fabricmc:fabric-language-kotlin:${mod.dep("fabric_kotlin")}")

    implementation("de.maxhenkel.voicechat:voicechat-api:${mod.dep("voicechat_api")}")
    compileOnly("su.plo.voice.api:server:${mod.dep("plasmo_api")}")
    compileOnly("su.plo.voice.api:client:${mod.dep("plasmo_api")}")

    modImplementation("xyz.bluspring.modernnetworking:modernnetworking-common:${mod.dep("modernnetworking")}+${mod.dep("modernnetworking_mc")}")!!

    modCompileOnly("maven.modrinth:talk-balloons:${mod.dep("talk_balloons")}+${mod.dep("talk_balloons_mc")}-fabric")
}

/*
dependencies {
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