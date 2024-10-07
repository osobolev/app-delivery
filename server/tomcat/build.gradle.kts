plugins {
    `lib`
}

dependencies {
    api(project(":server-embedded"))

    implementation("org.apache.tomcat.embed:tomcat-embed-core:9.0.95")
}
