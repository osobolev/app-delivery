plugins {
    `lib`
}

dependencies {
    api(libs.servlet.api)
    api(libs.txrpc.server)
    api(project(":apploader-common"))
    api(project(":server-install"))
}
