plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
    alias(libs.plugins.fabric.loom)
}

val buildHash = try {
    val command = arrayOf("git", "rev-parse", "HEAD")
    val process = Runtime.getRuntime().exec(command)
    process.inputReader().readLine().trim()
} catch (e: Throwable) {
    e.printStackTrace()
    "<unknown>"
}

setupCommonUnmodded("standalone", javaVersion = 25)
val shadedDep by configurations.getting

val launch by sourceSets.creating

afterEvaluate {
    sourceSets {
        launch.compileClasspath += configurations.getByName("minecraftNamedCompile")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${libs.versions.minecraft.standalone.get()}")

    shadedDep("launchImplementation"(implementation(project(":api"))!!)!!)
    shadedDep("launchImplementation"(implementation(project(":common:26.1")) {
        isTransitive = false
    })!!)
    shadedDep(implementation(project(":transcribers:google"))!!)
    shadedDep(implementation(project(":transcribers:whisper"))!!)

    "launchImplementation"(sourceSets.main.get().output)

    implementation(libs.bundles.kotlin)
    "launchImplementation"(libs.bundles.kotlin)
    shadedDep(libs.bundles.kotlin)

    shadedDep(libs.datafixerupper.get())
    shadedDep(libs.slf4j.api.get())
    "launchImplementation"(libs.bundles.logging)
    shadedDep(libs.bundles.logging)
}

tasks {
    processResources {
        properties(listOf("metadata.json"),
            "version" to mod.version,
            "minecraft_version" to libs.versions.minecraft.standalone.get(),
            "build_time" to System.currentTimeMillis(),
            "build_hash" to buildHash
        )
    }
}
