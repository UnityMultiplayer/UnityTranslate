plugins {
    java
    kotlin("jvm")
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val loader = prop("loom.platform")!!
val mcVersion: String = stonecutter.current.version
val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

version = "${mod.version}+$mcVersion"
base {
    archivesName.set("${mod.id}-$loader")
}
architectury {
    platformSetupLoomIde()
    neoForge()
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
    get("developmentNeoForge").extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    "neoForge"("net.neoforged:neoforge:${common.mod.dep("neoforge")}")
    "io.github.llamalad7:mixinextras-neoforge:${mod.dep("mixin_extras")}".let {
        implementation(it)
        include(it)
    }

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionNeoForge")) { isTransitive = false }

    modImplementation("xyz.bluspring.modernnetworking:modernnetworking-neoforge:${common.mod.dep("modernnetworking")}+${common.mod.dep("modernnetworking_mc")}")!!
    modImplementation("maven.modrinth:talk-balloons:${common.mod.dep("talk_balloons")}+${mcVersion}-neoforge")
    modRuntimeOnly("me.shedaniel.cloth:cloth-config-neoforge:${common.mod.dep("cloth_config")}") {
        isTransitive = false
    }
}
