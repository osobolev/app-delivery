plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.48.v20220622")
    api(project(":server-embedded"))
}
