dependencies {
    shade(api("commons-logging:commons-logging:1.3.4")!!)
    shade(api("org.apache.httpcomponents:httpcore:4.4.16")!!)
    shade(api("org.apache.httpcomponents:httpclient:4.5.13")!!)

    api("xyz.bluspring.modernnetworking:modernnetworking-api:${mod.dep("modernnetworking")}")
    api("io.netty:netty-buffer:4.1.97.Final")
    api("io.netty:netty-codec:4.1.97.Final")

    shade(api("org.java-websocket:Java-WebSocket:${mod.dep("java_websocket")}")!!)
    shade(api("com.github.jnr:jnr-ffi:${mod.dep("jnr")}")!!)
    shade(api("io.github.givimad:whisper-jni:${mod.dep("whisper")}")!!)
}


kotlin {
    compilerOptions {
        jvmToolchain(17)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}