version = "1.0.0"

val buildHash = try {
    val command = arrayOf("git", "rev-parse", "HEAD")
    val process = Runtime.getRuntime().exec(command)
    process.inputStream.bufferedReader().readLine().trim()
} catch (e: Throwable) {
    e.printStackTrace()
    "<unknown>"
}

val shadedDep by configurations.getting

dependencies {
    if (System.getenv("GITHUB_RUN_NUMBER") == null)
        runtimeOnly(project(":standalone"))
}

tasks {
    processResources {
        properties(listOf("launcher_meta.json"),
            "version" to project.version,
            "build_hash" to buildHash
        )
    }
}
