import dev.deftu.gradle.utils.ModData
import dev.deftu.gradle.utils.ProjectData

plugins {
    id("dev.deftu.gradle.multiversion-root")
}

subprojects {
    if (project.name.contains("minecraft")) {
        val projectData = ProjectData.from(rootProject)
        ModData.populateFrom(project, projectData)
    }
}

preprocess {
    val forge_1_21_04 = createNode("1.21.4-forge", 1_21_04, "srg")
    val neoforge_1_21_04 = createNode("1.21.4-neoforge", 1_21_04, "srg")
    val fabric_1_21_04 = createNode("1.21.4-fabric", 1_21_04, "yarn")

    val forge_1_21_03 = createNode("1.21.3-forge", 1_21_03, "srg")
    val neoforge_1_21_03 = createNode("1.21.3-neoforge", 1_21_03, "srg")
    val fabric_1_21_03 = createNode("1.21.3-fabric", 1_21_03, "yarn")

    val forge_1_21_01 = createNode("1.21.1-forge", 1_21_01, "srg")
    val neoforge_1_21_01 = createNode("1.21.1-neoforge", 1_21_01, "srg")
    val fabric_1_21_01 = createNode("1.21.1-fabric", 1_21_01, "yarn")

    val forge_1_20_06 = createNode("1.20.6-forge", 1_20_06, "srg")
    val neoforge_1_20_06 = createNode("1.20.6-neoforge", 1_20_06, "srg")
    val fabric_1_20_06 = createNode("1.20.6-fabric", 1_20_06, "yarn")

    val forge_1_20_04 = createNode("1.20.4-forge", 1_20_04, "srg")
    val neoforge_1_20_04 = createNode("1.20.4-neoforge", 1_20_04, "srg")
    val fabric_1_20_04 = createNode("1.20.4-fabric", 1_20_04, "yarn")

    val forge_1_20_01 = createNode("1.20.1-forge", 1_20_01, "srg")
    val fabric_1_20_01 = createNode("1.20.1-fabric", 1_20_01, "yarn")

    val forge_1_19_02 = createNode("1.19.2-forge", 1_19_02, "srg")
    val fabric_1_19_02 = createNode("1.19.2-fabric", 1_19_02, "yarn")

    val forge_1_18_02 = createNode("1.18.2-forge", 1_18_02, "srg")
    val fabric_1_18_02 = createNode("1.18.2-fabric", 1_18_02, "yarn")

    //val forge_1_16_05 = createNode("1.16.5-forge", 1_16_05, "srg")
    //val fabric_1_16_05 = createNode("1.16.5-fabric", 1_16_05, "yarn")

    forge_1_21_04.link(fabric_1_21_04)
    neoforge_1_21_04.link(fabric_1_21_04)
    fabric_1_21_04.link(fabric_1_21_03)

    forge_1_21_03.link(fabric_1_21_03)
    neoforge_1_21_03.link(fabric_1_21_03)
    fabric_1_21_03.link(fabric_1_21_01)

    forge_1_21_01.link(fabric_1_21_01)
    neoforge_1_21_01.link(fabric_1_21_01)
    fabric_1_21_01.link(fabric_1_20_06)

    forge_1_20_06.link(fabric_1_20_06)
    neoforge_1_20_06.link(fabric_1_20_06)
    fabric_1_20_06.link(fabric_1_20_04)

    forge_1_20_04.link(fabric_1_20_04)
    neoforge_1_20_04.link(fabric_1_20_04)
    fabric_1_20_04.link(fabric_1_20_01)

    forge_1_20_01.link(fabric_1_20_01)
    fabric_1_20_01.link(fabric_1_19_02)

    forge_1_19_02.link(fabric_1_19_02)
    fabric_1_19_02.link(fabric_1_18_02)

    forge_1_18_02.link(fabric_1_18_02)
    //fabric_1_18_02.link(fabric_1_16_05)

    //forge_1_16_05.link(fabric_1_16_05)
}