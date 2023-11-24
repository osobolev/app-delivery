plugins {
    `lib`
}

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:9.0.83")
    api(project(":server-embedded"))
}
