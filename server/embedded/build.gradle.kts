plugins {
    `lib`
}

dependencies {
    api(project(":server-core"))
    api(project(":server-http"))
    implementation(project(":apploader-common"))
}
