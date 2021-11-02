rootProject.name = "app-delivery"

fun add(name: String, path: String) {
    val dir = file(path)
    include(name)
    project(":${name}").projectDir = dir
}

add("apploader-common", "apploader/common")
add("apploader-lib", "apploader/lib")
add("apploader-client", "apploader/client")
add("apploader", "apploader/apploader")

add("server-core", "server/core")
add("server-install", "server/install")
add("server-http", "server/http")
add("server-jetty", "server/jetty")
add("server-service", "server/service")
add("server-war", "server/war")
add("server-desktop", "server/desktop")
add("server-files", "server/files")
