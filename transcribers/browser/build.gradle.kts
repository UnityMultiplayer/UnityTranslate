dependencies {
    api(project(":transcribers:api"))
    shade(api("org.seleniumhq.selenium:selenium-java:${mod.dep("selenium")}")!!)
    shade(api("org.java-websocket:Java-WebSocket:${mod.dep("java_websocket")}")!!)
    shade(api("com.google.code.gson:gson:${mod.dep("gson")}")!!)
    shadow(api("net.java.dev.jna:jna:${mod.dep("jna")}")!!)
    shadow(api("net.java.dev.jna:jna-platform:${mod.dep("jna")}")!!)
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

tasks {
    processResources {
        val properties = mutableMapOf<String, String>()

        properties["version"] = project.property("module.version") as String

        filesMatching("unitytranslate.module.json") {
            expand(properties)
        }
    }
}