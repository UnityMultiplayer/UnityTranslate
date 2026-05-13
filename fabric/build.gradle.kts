import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    alias(libs.plugins.fletching.table)
    alias(libs.plugins.shadow)
    `maven-publish`
}

if (shouldRemap()) {
    apply(plugin = "net.fabricmc.fabric-loom-remap")
} else {
    apply(plugin = "net.fabricmc.fabric-loom")
}

setupCommon("fabric")
setupCommonLoom("fabric")

val loom = extensions.getByType<LoomGradleExtensionAPI>()

val shadedDep by configurations.named("shadedDep")
val common = stonecutter.node.sibling("")?.project

dependencies {
    runtimeOnly(project(":api"))
    runtimeOnly(project(":common:${stonecutter.current.version}"))

    moddedImplementation(libs.fabric.loader)
    moddedApi(libs.fabric.kotlin)
    api(libs.mixinextras.fabric)
    annotationProcessor(libs.mixinextras.fabric)
    moddedApi("net.fabricmc.fabric-api:fabric-api:${common?.mod?.dep("fabric_api") ?: mod.dep("fabric_api")}")
    api(libs.bundles.elementa)
}
