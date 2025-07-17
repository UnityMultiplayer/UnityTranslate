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
    forge()
}

loom {
    forge {
        mixinConfigs("${mod.id}.mixins.json")
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
    get("developmentForge").extendsFrom(commonBundle)
}

repositories {
    maven("https://maven.minecraftforge.net")
}

dependencies {
    "forge"("net.minecraftforge:forge:$mcVersion-${common.mod.dep("forge")}")
    "io.github.llamalad7:mixinextras-forge:${mod.dep("mixin_extras")}".let {
        implementation(it)
        include(it)
    }

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionForge")) { isTransitive = false }

    modImplementation("xyz.bluspring.modernnetworking:modernnetworking-forge:${common.mod.dep("modernnetworking")}+${common.mod.dep("modernnetworking_mc")}")!!
    modImplementation("maven.modrinth:talk-balloons:${common.mod.dep("talk_balloons")}+${mcVersion}-forge")

    if (common.prop("cloth_forge") != "unsupported")
        modRuntimeOnly("me.shedaniel.cloth:cloth-config-forge:${common.mod.dep("cloth_config")}") {
            isTransitive = false
        }
}