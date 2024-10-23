plugins {
    `lib`
}

dependencies {
    api(project(":server-core"))
    api(project(":server-http"))

    implementation(libs.txrpc.body.server)
}
