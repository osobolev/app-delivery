plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.55.v20240627")
    api(project(":server-embedded"))
}
