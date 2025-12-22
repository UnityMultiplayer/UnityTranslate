plugins {
    id("net.neoforged.moddev.legacyforge")
    id("com.gradleup.shadow")
}

val loader = prop("loom.platform")!!
val minecraftVersion = stonecutter.current.version

val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

legacyForge.version = "$minecraftVersion-${common.mod.dep("forge")}"

fun loaderDep(dep: String): Any {
    return common?.project?.mod?.dep(dep) ?: mod.dep(dep, "[UNSUPPORTED]")
}

legacyForge {
    parchment {
        minecraftVersion.set(loaderDep("parchment_version") as String)
        mappingsVersion.set(loaderDep("parchment_snapshot") as String)
    }

    runs {
        create("client") {
            client()
        }

        create("server") {
            server()
        }
    }

    mods {
        create("unitytranslate") {
            sourceSet(sourceSets.main.get())
        }
    }
}

val commonBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations {
    compileClasspath.get().extendsFrom(commonBundle)
    runtimeClasspath.get().extendsFrom(commonBundle)
//    get("developmentForge").extendsFrom(commonBundle)
}

mixin {
    add(sourceSets.main.get(), "mixins.unitytranslate.refmap.json")
    config("unitytranslate.mixins.json")
}

repositories {
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    modImplementation("maven.modrinth:talk-balloons:${common.mod.dep("talk_balloons")}+${common.mod.dep("talk_balloons_mc")}-forge")
    modImplementation("me.shedaniel.cloth:cloth-config-forge:${common.mod.dep("cloth_config")}")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionForge")) { isTransitive = false }

    modImplementation("dev.nyon:KotlinLangForge:${common.mod.dep("kotlinlangforge")}-k${mod.dep("kotlin")}-${common.mod.dep("kotlinlangforge_loader")}+forge")
    modImplementation("xyz.bluspring.modernnetworking:modernnetworking-forge:${common.mod.dep("modernnetworking")}+${common.mod.dep("modernnetworking_mc")}")!!

    modOptional("maven.modrinth:plasmo-voice", "forge-${mod.commonDep("plasmo_mc", common.mod, minecraftVersion)}-${mod.commonDep("plasmo", common.mod)}", common.mod.prop("proximity_chat") == "plasmo")
    modOptional("maven.modrinth:simple-voice-chat", "forge-$minecraftVersion-${mod.commonDep("voicechat", common.mod)}", common.mod.prop("proximity_chat") == "svc")

    add("additionalRuntimeClasspath", shadowBundle("org.java-websocket:Java-WebSocket:${mod.commonDep("java_websocket", common.mod)}")!!)
    add("additionalRuntimeClasspath", shadowBundle("com.squareup.okhttp3:okhttp:${mod.commonDep("okhttp", common.mod)}")  {
        exclude("org.jetbrains")
        exclude("kotlin")
    })
    add("additionalRuntimeClasspath", shadowBundle("com.github.jnr:jnr-ffi:${mod.commonDep("jnr", common.mod)}")!!)

    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib:${mod.commonDep("unitytranslatelib", common.mod)}")
    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${mod.commonDep("unitytranslatelib", common.mod)}")
    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${mod.commonDep("unitytranslatelib", common.mod)}")
}

//loom {
//    runConfigs.all {
//        isIdeConfigGenerated = true
//        runDir = "../../../run"
//        vmArgs("-Dmixin.debug.export=true", "-XX:+AllowEnhancedClassRedefinition")
//    }
//}

tasks.jar {
    manifest {
        attributes(mapOf(
            "MixinConfigs" to "unitytranslate.mixins.json"
        ))
    }
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
    exclude("fabric.mod.json", "architectury.common.json")
}

obfuscation {
    reobfuscate(tasks.shadowJar, sourceSets.main.get())
}

tasks.processResources {
    properties(listOf("META-INF/neoforge.mods.toml", "META-INF/mods.toml"),
        "mod_id" to mod.id,
        "mod_name" to mod.name,
        "mod_version" to mod.version,
        "mod_description" to mod.prop("description"),
        "mod_authors" to mod.prop("authors"),
        "minecraft_version_range" to common.mod.prop("mc_dep_forgelike"),
        "forge_version" to common.mod.dep("forge"),
        "cloth_config_version" to common.mod.dep("cloth_config"),
        "modernnetworking_version" to common.mod.dep("modernnetworking"),
        "kotlinlangforge_loader" to common.mod.dep("kotlinlangforge_loader"),
        "kotlinlangforge_version" to common.mod.dep("kotlinlangforge")
    )
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

//tasks.register<Copy>("buildAndCollect") {
//    group = "versioned"
//    description = "Must run through 'chiseledBuild'"
//    from(tasks.reobfJar.get().archiveFile, tasks.sourcesJar.get().archiveFile)
//    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
//    dependsOn("build")
//}