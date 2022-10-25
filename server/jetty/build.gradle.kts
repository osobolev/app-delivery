plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.49.v20220914")
    api(project(":server-embedded"))
}
