plugins {
    id("com.github.ben-manes.versions") version "0.54.0"
}

fun requiredMajor(mod: ModuleComponentIdentifier): String {
    if (mod.group == "javax.servlet") return "3." // версия >= 4 использует jakarta namespace
    if (mod.group == "org.apache.tomcat.embed") return "9." // версия >= 10 использует jakarta namespace
    if (mod.group == "org.eclipse.jetty") return "9." // версия >= 10 требует Java 11
    return ""
}

tasks.withType(com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class).configureEach {
    rejectVersionIf {
        !candidate.version.startsWith(requiredMajor(candidate))
    }
}
