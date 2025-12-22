//import net.fabricmc.loom.api.LoomGradleExtensionAPI
//import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

//fun Project.setupLoom() {
//    val loom = this.extensions.getByName<LoomGradleExtensionAPI>("loom")
//
//    loom.silentMojangMappingsLicense()
//    loom.decompilers {
//        get("vineflower").apply { // Adds names to lambdas - useful for mixins
//            options.put("mark-corresponding-synthetics", "1")
//        }
//    }
//
//    loom.mixin.useLegacyMixinAp = false
//
//    tasks.named<RemapJarTask>("remapJar") {
//        injectAccessWidener = true
//        input = tasks.named<Jar>("shadowJar").get().archiveFile
//        archiveClassifier = null
//        dependsOn(tasks.named<Jar>("shadowJar"))
//    }
//}