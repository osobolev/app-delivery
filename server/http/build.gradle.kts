plugins {
    `lib`
}

dependencies {
    api("javax.servlet:javax.servlet-api:3.1.0")
    api("io.github.osobolev.txrpc:txrpc-remote-server:1.0")
    api(project(":apploader-common"))

    implementation(project(":server-install"))
}
