plugins {
    `lib`
}

dependencies {
    api(project(":server-embedded"))

    implementation(libs.tomcat)
}
