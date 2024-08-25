plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

rootProject.name = "app-delivery"

fun add(name: String, path: String) {
    val dir = file(path)
    include(name)
    project(":${name}").projectDir = dir
}

add("unix-zip", "unixzip")

add("apploader-common", "apploader/common")
add("apploader-lib", "apploader/lib")
add("apploader-client", "apploader/client")
add("apploader", "apploader/apploader")

add("server-core", "server/core")
add("server-install", "server/install")
add("server-http", "server/http")
add("server-embedded", "server/embedded")
add("server-jetty", "server/jetty")
add("server-tomcat", "server/tomcat")
add("server-service", "server/service")
add("server-war", "server/war")
add("server-desktop", "server/desktop")
add("server-files", "server/files")
