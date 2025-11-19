@file:Suppress("UnstableApiUsage")


plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val loader = prop("loom.platform")!!
val minecraftVersion: String = stonecutter.current.version
val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

architectury {
    platformSetupLoomIde()
    fabric()
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
    get("developmentFabric").extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.terraformersmc.com")
    maven("https://maven.nucleoid.xyz/") // Not sure why but we need this
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${common.mod.dep("fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${common.mod.dep("fabric_api")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${common.mod.dep("fabric_kotlin")}")

    modImplementation("com.terraformersmc:modmenu:${common.mod.dep("modmenu")}")

    include(modImplementation("me.lucko:fabric-permissions-api:${common.mod.dep("fabric_permissions")}")!!)

    modImplementation("maven.modrinth:talk-balloons:${common.mod.dep("talk_balloons")}+${common.mod.dep("talk_balloons_mc")}-fabric")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${common.mod.dep("cloth_config")}")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionFabric")) { isTransitive = false }

    modImplementation("xyz.bluspring.modernnetworking:modernnetworking-fabric:${common.mod.dep("modernnetworking")}+${common.mod.dep("modernnetworking_mc")}")!!
    modImplementation("net.fabricmc:fabric-language-kotlin:${common.mod.dep("fabric_kotlin")}")

    modOptional("maven.modrinth:plasmo-voice", "fabric-${mod.commonDep("plasmo_mc", common.mod, minecraftVersion)}-${mod.commonDep("plasmo", common.mod)}", common.mod.prop("proximity_chat") == "plasmo")
    modOptional("maven.modrinth:simple-voice-chat", "fabric-$minecraftVersion-${mod.commonDep("voicechat", common.mod)}", common.mod.prop("proximity_chat") == "svc")

    include("org.java-websocket:Java-WebSocket:${mod.commonDep("java_websocket", common.mod)}")
    include("com.squareup.okhttp3:okhttp:${mod.commonDep("okhttp", common.mod)}")
    include("com.github.jnr:jnr-ffi:${mod.commonDep("jnr", common.mod)}")

    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib:${mod.commonDep("unitytranslatelib", common.mod)}")
    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-windows-amd64:${mod.commonDep("unitytranslatelib", common.mod)}")
    shadowBundle("xyz.bluspring.unitytranslate:UnityTranslateLib-natives-linux-amd64:${mod.commonDep("unitytranslatelib", common.mod)}")
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
}

tasks.jar {
    archiveClassifier = "dev"
}

tasks.processResources {
    properties(listOf("fabric.mod.json"),
        "mod_id" to mod.id,
        "mod_name" to mod.name,
        "mod_version" to mod.version,
        "mod_description" to mod.prop("description"),
        "mod_authors" to mod.prop("authors"),
        "minecraft_version_range" to common.mod.prop("mc_dep_fabric"),
        "fabric_loader_version" to common.mod.dep("fabric_loader"),
        "cloth_config_version" to common.mod.dep("cloth_config"),
        "modernnetworking_version" to common.mod.dep("modernnetworking")
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