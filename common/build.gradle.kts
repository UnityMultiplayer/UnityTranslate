dependencies {
    shade(api("commons-logging:commons-logging:1.3.4")!!)
    shade(api("org.apache.httpcomponents:httpcore:4.4.16")!!)
    shade(api("org.apache.httpcomponents:httpclient:4.5.13")!!)

    shade(api("org.java-websocket:Java-WebSocket:${rootProject.property("java_websocket_version")}")!!)
    shade(api("com.github.jnr:jnr-ffi:${rootProject.property("jnr_version")}")!!)
    shade(api("io.github.givimad:whisper-jni:${rootProject.property("whisper_version")}")!!)
}


tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}