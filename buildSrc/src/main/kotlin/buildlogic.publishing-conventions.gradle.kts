plugins {
    id("buildlogic.java-library-conventions")
    id("maven-publish")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String

            pom {
                name.set("Particle")
                description.set("Particle HTML UI for Java")
                url.set("https://github.com/bitfist/markdown-editor-fx")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("bitfist")
                        name.set("bitfist")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/bitfist/markdown-editor-fx.git")
                    developerConnection.set("scm:git:ssh://github.com:bitfist/markdown-editor-fx.git")
                    url.set("https://github.com/bitfist/markdown-editor-fx")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/bitfist/markdown-editor-fx")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
