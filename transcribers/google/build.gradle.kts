repositories {
    maven("https://maven.maxhenkel.de/repository/public")
}

dependencies {
    shade(api("com.google.code.gson:gson:${mod.dep("gson")}")!!)
    shade(api("net.sourceforge.javaflacencoder:java-flac-encoder:${mod.dep("java_flac_encoder")}")!!)
}