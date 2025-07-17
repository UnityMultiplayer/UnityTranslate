plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("dev.kikugie.j52j") version "2.0"
}

val modLoaderName = prop("loom.platform") ?: "fabric"
val mcVersion = stonecutter.current.version
val common = stonecutter.node.sibling("")

version = "${rootProject.property("mod.version")}+mc${mcVersion}-${modLoaderName.lowercase()}"

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$mcVersion:${common?.project?.mod?.prop("parchment_snapshot") ?: mod.prop("parchment_snapshot")}")
    })

    modImplementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")

    modCompileOnly("maven.modrinth:simple-voice-chat:$modLoaderName-$mcVersion-${common?.project?.mod?.dep("voicechat") ?: mod.dep("voicechat")}")
    modImplementation("xyz.bluspring.modernnetworking:modernnetworking-fabric:${mod.dep("modernnetworking")}+${common?.project?.mod?.dep("modernnetworking_mc") ?: mod.dep("modernnetworking_mc")}")!!

    modCompileOnly("maven.modrinth:talk-balloons:${mod.dep("talk_balloons")}+${mcVersion}-fabric")
}
