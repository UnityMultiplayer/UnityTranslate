import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        "implementation"(project(":api"))
    }

    project.extensions.configure<JavaPluginExtension>("java") {
        withSourcesJar()
        withJavadocJar()

        val java = JavaVersion.toVersion(17)
        targetCompatibility = java
        sourceCompatibility = java
    }

    project.extensions.configure<KotlinProjectExtension>("kotlin") {
        jvmToolchain(17)
    }

    tasks {
        getByName<ProcessResources>("processResources") {
            properties(listOf("unitytranslate.plugin.json"), "version" to project.version)
        }
    }
}
