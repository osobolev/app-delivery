plugins {
    `lib`
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("org.tukaani:xz:1.9")
    implementation(project(":apploader-common"))
}
