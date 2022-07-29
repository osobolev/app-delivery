plugins {
    `lib`
}

dependencies {
    implementation("org.apache.tomcat.embed:tomcat-embed-core:9.0.65")
    api(project(":server-embedded"))
}
