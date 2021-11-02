plugins {
    `lib`
}

dependencies {
    implementation("org.eclipse.jetty:jetty-servlet:9.4.43.v20210629")
    api(project(":server-core"))
    api(project(":server-http"))
    implementation(project(":apploader-common"))
}
