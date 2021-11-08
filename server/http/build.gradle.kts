plugins {
    `lib`
}

dependencies {
    api("javax.servlet:javax.servlet-api:3.1.0")
    implementation("io.github.osobolev.sqlg3:sqlg3-remote-server:2.1")
    implementation(project(":apploader-common"))
    implementation(project(":server-install"))
}
