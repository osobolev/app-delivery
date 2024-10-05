plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.56.v20240826")
    api(project(":server-embedded"))
}
