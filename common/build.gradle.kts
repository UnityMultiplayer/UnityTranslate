dependencies {
    shade(api("commons-logging:commons-logging:1.3.4")!!)
    shade(api("org.apache.httpcomponents:httpcore:4.4.16")!!)
    shade(api("org.apache.httpcomponents:httpclient:4.5.13")!!)

    api("xyz.bluspring.modernnetworking:modernnetworking-api:${mod.dep("modernnetworking")}")
    api("io.netty:netty-buffer:4.1.97.Final")
    api("io.netty:netty-codec:4.1.97.Final")

    shade(api("com.github.jnr:jnr-ffi:${mod.dep("jnr")}")!!)
    shade(api("io.github.givimad:whisper-jni:${mod.dep("whisper")}")!!)

    shade(api("gg.essential:vigilance:${mod.dep("vigilance")}")!!)

    // Voice Chat APIs
    implementation("de.maxhenkel.voicechat:voicechat-api:${mod.dep("voicechat_api")}")
    compileOnly("su.plo.voice.api:server:${mod.dep("plasmo_api")}")
    compileOnly("su.plo.voice.api:client:${mod.dep("plasmo_api")}")
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