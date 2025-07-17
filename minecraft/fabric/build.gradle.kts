plugins {
    java
    kotlin("jvm")
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val loader = prop("loom.platform")!!
val mcVersion: String = stonecutter.current.version
val common = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

version = "${mod.version}+$mcVersion"
base {
    archivesName.set("${mod.id}-$loader")
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
    maven("https://maven.fabricmc.net")
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${common.mod.dep("fabric_api")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${mod.dep("fabric_kotlin")}")

    include(modImplementation("me.lucko:fabric-permissions-api:${common.mod.dep("fabric_permissions")}")!!)
    modImplementation("maven.modrinth:modmenu:${common.mod.dep("modmenu")}")

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionFabric")) { isTransitive = false }

    modImplementation("xyz.bluspring.modernnetworking:modernnetworking-fabric:${common.mod.dep("modernnetworking")}+${common.mod.dep("modernnetworking_mc")}")!!
    modImplementation("maven.modrinth:talk-balloons:${common.mod.dep("talk_balloons")}+${mcVersion}-fabric")
    modRuntimeOnly("me.shedaniel.cloth:cloth-config-fabric:${common.mod.dep("cloth_config")}") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
    }
}
