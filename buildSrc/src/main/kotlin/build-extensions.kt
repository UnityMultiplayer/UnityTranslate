import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven
import org.gradle.language.jvm.tasks.ProcessResources

val Project.mod: ModData get() = ModData(this)
fun Project.prop(key: String): String? = findProperty(key)?.toString()
fun String.upperCaseFirst() = replaceFirstChar { if (it.isLowerCase()) it.uppercaseChar() else it }

fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
    forRepository { maven(url) { name = alias } }
    filter { groups.forEach(::includeGroup) }
}

fun DependencyHandler.modOptional(notation: String, version: String, enabled: Boolean = true) {
    if (version != "[UNSUPPORTED]") {
        if (enabled) {
            add("modImplementation", "$notation:$version")
        } else {
            add("modCompileOnly", "$notation:$version")
        }
    }
}

fun DependencyHandler.optional(notation: String, version: String, enabled: Boolean = true) {
    if (version != "[UNSUPPORTED]") {
        if (enabled) {
            add("api", "$notation:$version")
        } else {
            add("compileOnly", "$notation:$version")
        }
    }
}

fun DependencyHandler.optional(notation: String, version: String, enabled: Boolean = true, builder: (String) -> Any) {
    if (version != "[UNSUPPORTED]") {
        if (enabled) {
            add("api", builder.invoke("$notation:$version"))
        } else {
            add("compileOnly", builder.invoke("$notation:$version"))
        }
    }
}

fun ProcessResources.properties(files: Iterable<String>, vararg properties: Pair<String, Any>) {
    for ((name, value) in properties) inputs.property(name, value)
    filesMatching(files) {
        expand(properties.toMap())
    }
}

@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = requireNotNull(project.prop("mod.id")) { "Missing 'mod.id'" }
    val name: String get() = requireNotNull(project.prop("mod.name")) { "Missing 'mod.name'" }
    val version: String get() = requireNotNull(project.prop("mod.version")) { "Missing 'mod.version'" }
    val group: String get() = requireNotNull(project.prop("mod.group")) { "Missing 'mod.group'" }

    fun prop(key: String) = requireNotNull(project.prop("mod.$key")) { "Missing 'mod.$key'" }
    fun prop(key: String, orElse: Any) = project.prop("mod.$key") ?: orElse
    fun dep(key: String) = requireNotNull(project.prop("deps.$key")) { "Missing 'deps.$key'" }
    fun dep(key: String, orElse: Any) = project.prop("deps.$key") ?: orElse

    fun commonDep(key: String, common: ModData? = null, default: String = "[UNSUPPORTED]"): Any {
        val commonDep = common?.dep(key, default) ?: "[UNSUPPORTED]"
        val loaderDep = this.dep(key, commonDep)

        if (loaderDep == "[VERSIONED]" || loaderDep == "[DEFAULT]") {
            if (commonDep == "[DEFAULT]") {
                return default
            }

            return commonDep
        }

        return loaderDep
    }
}