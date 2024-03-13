plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.54.v20240208")
    api(project(":server-embedded"))
}
