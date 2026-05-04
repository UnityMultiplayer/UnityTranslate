plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin) version libs.versions.kotlin.get()
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.kikugie.dev/releases")
    maven("https://maven.kikugie.dev/snapshots")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("net.fabricmc:fabric-loom:${libs.versions.fabric.loom.get()}")
    implementation("net.neoforged:moddev-gradle:${libs.versions.moddevgradle.get()}")
    implementation("dev.kikugie:stonecutter:${libs.versions.stonecutter.get()}")
    implementation("me.modmuss50:mod-publish-plugin:${libs.versions.mod.publish.get()}")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:${libs.versions.shadow.get()}")
}