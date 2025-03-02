val minecraftVersion = project.property("core_mc_version")

// versioned dependency
fun verdep(name: String): String {
    return rootProject.property("${minecraftVersion}.$name") as String
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraftVersion:${verdep("parchment_release")}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("loader_version")}")
}