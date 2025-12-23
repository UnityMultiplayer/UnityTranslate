plugins {
    id("net.neoforged.moddev")
    id("com.gradleup.shadow")
}

val loader = prop("loom.platform")!!
val minecraftVersion = stonecutter.current.version

val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

val commonBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

fun loaderDep(dep: String): Any {
    return common?.project?.mod?.dep(dep) ?: mod.dep(dep, "[UNSUPPORTED]")
}

neoForge {
    version = common.mod.dep("neoforge")

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

configurations {
    compileClasspath.get().extendsFrom(commonBundle)
    runtimeClasspath.get().extendsFrom(commonBundle)
//    get("developmentNeoForge").extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    api("maven.modrinth:talk-balloons:${common.mod.dep("talk_balloons")}+${common.mod.dep("talk_balloons_mc")}-neoforge")
    api("me.shedaniel.cloth:cloth-config-neoforge:${common.mod.dep("cloth_config")}")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "namedElements")) { isTransitive = false }

    api("dev.nyon:KotlinLangForge:${common.mod.dep("kotlinlangforge")}-k${mod.dep("kotlin")}-${common.mod.dep("kotlinlangforge_loader")}+neoforge")
    api("xyz.bluspring.modernnetworking:modernnetworking-neoforge:${common.mod.dep("modernnetworking")}+${common.mod.dep("modernnetworking_mc")}")!!

    optional("maven.modrinth:plasmo-voice", "neoforge-${mod.commonDep("plasmo_mc", common.mod, minecraftVersion)}-${mod.commonDep("plasmo", common.mod)}", common.mod.prop("proximity_chat") == "plasmo")
    optional("maven.modrinth:simple-voice-chat", "neoforge-$minecraftVersion-${mod.commonDep("voicechat", common.mod)}", common.mod.prop("proximity_chat") == "svc")

    shadowBundle("org.java-websocket:Java-WebSocket:${mod.commonDep("java_websocket", common.mod)}") {
        exclude("org.slf4j", "slf4j-api")
    }.also {
        if (stonecutter.eval(minecraftVersion, "<=1.21.8")) {
            add("additionalRuntimeClasspath", it!!)
        }
    }

    shadowBundle("com.squareup.okhttp3:okhttp:${mod.commonDep("okhttp", common.mod)}") {
        exclude("org.jetbrains")
        exclude("kotlin")
    }.also {
        if (stonecutter.eval(minecraftVersion, "<=1.21.8")) {
            add("additionalRuntimeClasspath", it!!)
        }
    }

    shadowBundle("com.github.jnr:jnr-ffi:${mod.commonDep("jnr", common.mod)}").also {
        if (stonecutter.eval(minecraftVersion, "<=1.21.8")) {
            add("additionalRuntimeClasspath", it!!)
        }
    }

    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib:${mod.commonDep("unitytranslatelib", common.mod)}")
    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${mod.commonDep("unitytranslatelib", common.mod)}")
    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${mod.commonDep("unitytranslatelib", common.mod)}")
}

/*loom {
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../../run"
        vmArgs("-Dmixin.debug.export=true", "-XX:+AllowEnhancedClassRedefinition")
    }
}*/

configurations.all {
    resolutionStrategy {
        // Force SLF4J version so we don't encounter a "failed to import" exception with this
        force("org.slf4j:slf4j-api:2.0.17")
    }
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
    exclude("fabric.mod.json", "architectury.common.json")
}

tasks.processResources {
    properties(listOf("META-INF/neoforge.mods.toml", "META-INF/mods.toml"),
        "mod_id" to mod.id,
        "mod_name" to mod.name,
        "mod_version" to mod.version,
        "mod_description" to mod.prop("description"),
        "mod_authors" to mod.prop("authors"),
        "minecraft_version_range" to common.mod.prop("mc_dep_forgelike"),
        "neoforge_version" to common.mod.dep("neoforge"),
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
//    from(tasks.jar.get().archiveFile, tasks.sourcesJar.get().archiveFile)
//    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
//    dependsOn("build")
//}