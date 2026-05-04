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

dependencies {
    moddedImplementation(libs.fabric.loader)
    moddedApi(libs.fabric.kotlin)
    api(libs.mixinextras.fabric)
    annotationProcessor(libs.mixinextras.fabric)
}
