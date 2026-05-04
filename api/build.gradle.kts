plugins {
    java
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}

setupCommonUnmodded("api", javaVersion = 17)

dependencies {
    api(libs.bundles.kotlin)
    api(libs.slf4j.api.get())
    api(libs.datafixerupper.get())
}
