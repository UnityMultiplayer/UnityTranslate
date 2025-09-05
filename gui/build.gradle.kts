plugins {
    application
}

dependencies {
    shade(api("org.seleniumhq.selenium:selenium-java:${mod.dep("selenium")}")!!)

    shade(api("gg.essential:elementa:${mod.dep("elementa")}")!!)
    shade(implementation("gg.essential:universalcraft-standalone:${mod.dep("universalcraft")}")!!)
    shade(api("com.google.code.gson:gson:${mod.dep("gson")}")!!)

    implementation("net.minecrell:terminalconsoleappender:1.3.0") // coloured CLI output :D
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("xyz.bluspring.unitytranslate.gui.UnityTranslateGui")
}

tasks {
    processResources {
        val properties = mutableMapOf<String, String>()
        properties["version"] = rootProject.property("mod.version") as String

        filesMatching("version.txt") {
            expand(properties)
        }
    }
}