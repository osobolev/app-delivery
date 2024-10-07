plugins {
    `lib`
}

dependencies {
    api(project(":server-embedded"))

    implementation("org.eclipse.jetty:jetty-servlet:9.4.56.v20240826")
}
