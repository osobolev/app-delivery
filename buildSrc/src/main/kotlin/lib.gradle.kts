plugins {
    id("base-lib")
}

group = "io.github.osobolev.app-delivery"
version = "7.1"

if (project.name == "unix-unzip") {
    description = "Library for reading/restoring UNIX permissions of ZIP file entries";
}

(publishing.publications["mavenJava"] as MavenPublication).pom {
    name.set("${project.group}:${project.name}")
    description.set(project.description ?: "Framework for delivering desktop application updates")
    url.set("https://github.com/osobolev/app-delivery")
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    developers {
        developer {
            name.set("Oleg Sobolev")
            organizationUrl.set("https://github.com/osobolev")
        }
    }
    scm {
        connection.set("scm:git:https://github.com/osobolev/app-delivery.git")
        developerConnection.set("scm:git:https://github.com/osobolev/app-delivery.git")
        url.set("https://github.com/osobolev/app-delivery")
    }
}
