plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.50.v20221201")
    api(project(":server-embedded"))
}
