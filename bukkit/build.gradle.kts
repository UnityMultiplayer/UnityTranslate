plugins {
    java
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}

setupCommonUnmodded("shared", javaVersion = 17)

val shadedDep by configurations.getting

dependencies {
    shadedDep(api(project(":api"))!!)
    shadedDep(api(project(":")))
    api(libs.bundles.kotlin)
    shadedDep(libs.bundles.kotlin)
    api(libs.datafixerupper.get())
}
