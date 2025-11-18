plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    val neoforge_1_21_10 = createNode("1.21.10-neoforge", 1_21_10, "srg")
    val fabric_1_21_10 = createNode("1.21.10-fabric", 1_21_10, "yarn")

    val neoforge_1_21_08 = createNode("1.21.8-neoforge", 1_21_08, "srg")
    val fabric_1_21_08 = createNode("1.21.8-fabric", 1_21_08, "yarn")

    val neoforge_1_21_05 = createNode("1.21.5-neoforge", 1_21_05, "srg")
    val fabric_1_21_05 = createNode("1.21.5-fabric", 1_21_05, "yarn")

    val neoforge_1_21_04 = createNode("1.21.4-neoforge", 1_21_04, "srg")
    val fabric_1_21_04 = createNode("1.21.4-fabric", 1_21_04, "yarn")

    //val forge_1_21_01 = createNode("1.21.1-forge", 1_21_01, "srg")
    val neoforge_1_21_01 = createNode("1.21.1-neoforge", 1_21_01, "srg")
    val fabric_1_21_01 = createNode("1.21.1-fabric", 1_21_01, "yarn")

    val forge_1_20_01 = createNode("1.20.1-forge", 1_20_01, "srg")
    val fabric_1_20_01 = createNode("1.20.1-fabric", 1_20_01, "yarn")

    neoforge_1_21_10.link(fabric_1_21_10)
    fabric_1_21_10.link(fabric_1_21_08)

    neoforge_1_21_08.link(fabric_1_21_08)
    fabric_1_21_08.link(fabric_1_21_05)

    neoforge_1_21_05.link(fabric_1_21_05)
    fabric_1_21_05.link(fabric_1_21_04)

    neoforge_1_21_04.link(fabric_1_21_04)
    fabric_1_21_04.link(fabric_1_21_01)

    //forge_1_21_01.link(fabric_1_21_01)
    neoforge_1_21_01.link(fabric_1_21_01)
    fabric_1_21_01.link(fabric_1_20_01)

    forge_1_20_01.link(fabric_1_20_01)
}