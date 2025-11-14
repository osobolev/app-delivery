plugins {
    `lib`
}

dependencies {
    compileOnly(project(":apploader-client"))
    compileOnly(project(":apploader-ssl"))
}

tasks.jar {
    dependsOn(configurations.compileClasspath)
    from(configurations.compileClasspath.map { conf -> conf.files.map { if (it.isDirectory()) it else zipTree(it) } })
    manifest.attributes["Main-Class"] = "apploader.AppLoader"
}
