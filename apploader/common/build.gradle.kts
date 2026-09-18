plugins {
    id("lib")
}

dependencies {
    // Do not change to "implementation" - "api" required by apploader.jar:
    api(project(":unix-unzip"))
}
