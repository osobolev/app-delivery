plugins {
    `lib`
}

dependencies {
    api(libs.servlet.api)
    api(libs.txrpc.server)
    api(project(":apploader-common"))

    implementation(project(":server-install"))
}
