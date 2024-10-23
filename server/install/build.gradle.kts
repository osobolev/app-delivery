plugins {
    `lib`
}

dependencies {
    implementation(libs.commons.compress)
    implementation(project(":apploader-common"))

    runtimeOnly(libs.xz)
}
