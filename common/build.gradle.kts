dependencies {
    shadow(api("commons-logging:commons-logging:1.3.4")!!)
    shadow(api("org.apache.httpcomponents:httpcore:4.4.16")!!)
    shadow(api("org.apache.httpcomponents:httpclient:4.5.13")!!)

    shadow(api("org.java-websocket:Java-WebSocket:${rootProject.property("java_websocket_version")}")!!)
    shadow(api("com.github.jnr:jnr-ffi:${rootProject.property("jnr_version")}")!!)
}