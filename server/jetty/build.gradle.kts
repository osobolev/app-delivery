plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.53.v20231009")
    api(project(":server-embedded"))
}
