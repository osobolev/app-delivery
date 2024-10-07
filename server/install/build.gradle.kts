plugins {
    `lib`
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation(project(":apploader-common"))

    runtimeOnly("org.tukaani:xz:1.10")
}
