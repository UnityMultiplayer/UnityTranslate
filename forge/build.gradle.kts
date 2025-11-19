plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val loader = prop("loom.platform")!!
val minecraftVersion = stonecutter.current.version

val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

architectury {
    platformSetupLoomIde()
    forge()
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
    get("developmentForge").extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    "forge"("net.minecraftforge:forge:$minecraftVersion-${common.mod.dep("forge")}")

    modImplementation("maven.modrinth:talk-balloons:${common.mod.dep("talk_balloons")}+${common.mod.dep("talk_balloons_mc")}-forge")
    modImplementation("me.shedaniel.cloth:cloth-config-forge:${common.mod.dep("cloth_config")}")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionForge")) { isTransitive = false }

    modImplementation("dev.nyon:KotlinLangForge:${common.mod.dep("kotlinlangforge")}-${common.mod.dep("kotlinlangforge_loader")}+forge")
    modImplementation("xyz.bluspring.modernnetworking:modernnetworking-forge:${common.mod.dep("modernnetworking")}+${common.mod.dep("modernnetworking_mc")}")!!

    modOptional("maven.modrinth:plasmo-voice", "forge-${mod.commonDep("plasmo_mc", common.mod, minecraftVersion)}-${mod.commonDep("plasmo", common.mod)}", common.mod.prop("proximity_chat") == "plasmo")
    modOptional("maven.modrinth:simple-voice-chat", "forge-$minecraftVersion-${mod.commonDep("voicechat", common.mod)}", common.mod.prop("proximity_chat") == "svc")

    minecraftRuntimeLibraries(shadowBundle("org.java-websocket:Java-WebSocket:${mod.commonDep("java_websocket", common.mod)}")!!)
    minecraftRuntimeLibraries(shadowBundle("com.squareup.okhttp3:okhttp:${mod.commonDep("okhttp", common.mod)}")  {
        exclude("org.jetbrains")
        exclude("kotlin")
    })
    minecraftRuntimeLibraries(shadowBundle("com.github.jnr:jnr-ffi:${mod.commonDep("jnr", common.mod)}")!!)

    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib:${mod.commonDep("unitytranslatelib", common.mod)}")
    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${mod.commonDep("unitytranslatelib", common.mod)}")
    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${mod.commonDep("unitytranslatelib", common.mod)}")
}

loom {
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../../run"
        vmArgs("-Dmixin.debug.export=true", "-XX:+AllowEnhancedClassRedefinition")
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

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}