plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.44.v20210927")
    api(project(":server-embedded"))
}
