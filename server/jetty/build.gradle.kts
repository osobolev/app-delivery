plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.51.v20230217")
    api(project(":server-embedded"))
}
