plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    checkstyle
    `maven-publish`
    signing
    `java-library`
    `version-catalog`
    id("org.gradle.crypto.checksum") version "1.4.0"
    id("com.gradle.plugin-publish") version "1.1.0" apply false
}

val groupId: String by project
val defaultVersion: String by project
val jupiterVersion: String by project
val assertj: String by project
val mockitoVersion: String by project

val gitHubUser: String? by project
val gitHubToken: String? by project

var actualVersion: String = (project.findProperty("version") ?: defaultVersion) as String
if (actualVersion == "unspecified") {
    actualVersion = defaultVersion
}

allprojects {
    apply(plugin = "checkstyle")
    apply(plugin = "maven-publish")
    version = actualVersion
    group = groupId

    // for all gradle plugins:
    pluginManager.withPlugin("java-gradle-plugin") {
        apply(plugin = "com.gradle.plugin-publish")
    }

    publishing {
        repositories {
            maven {
                name = "GitHub"
                setUrl("https://maven.pkg.github.com/ids-basecamp/gradle-plugins-fork")
                credentials {
                    username = gitHubUser
                    password = gitHubToken
                }
            }
        }
    }

    // for all java libs:
    pluginManager.withPlugin("java-library") {
        java {
            val javaVersion = 11
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(javaVersion))
            }
            tasks.withType(JavaCompile::class.java) {
                // making sure the code does not use any APIs from a more recent version.
                // Ref: https://docs.gradle.org/current/userguide/building_java_projects.html#sec:java_cross_compilation
                options.release.set(javaVersion)
            }
            withJavadocJar()
            withSourcesJar()
        }

        dependencies {
            // Use JUnit test framework for unit tests
            testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
            testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
            testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
            testImplementation("org.assertj:assertj-core:${assertj}")
            testImplementation("org.mockito:mockito-core:${mockitoVersion}")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }

    // configure checkstyle version
    checkstyle {
        toolVersion = "10.0"
        maxErrors = 0 // does not tolerate errors
    }

    repositories {
        mavenCentral()
    }

    // let's not generate any reports because that is done from within the Github Actions workflow
    tasks.withType<Checkstyle> {
        reports {
            html.required.set(false)
            xml.required.set(true)
        }
    }

    afterEvaluate {
        // values needed for publishing
        publishing {
            publications.forEach { i ->
                val mp = (i as MavenPublication)
                mp.pom {
                    name.set(project.name)
                    description.set("edc :: ${project.name}")
                    url.set("https://github.com/ids-basecamp/gradle-plugins-fork.git")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                        developers {
                            developer {
                                id.set("dhommen")
                                name.set("Daniel Hommen")
                                email.set("dhommen@orbiter.de")
                            }
                            developer {
                                id.set("schoenenberg")
                                name.set("Maximilian Schönenberg")
                                email.set("ms@emprium.at")
                            }
                            developer {
                                id.set("paulocabrita-ionos")
                                name.set("Paulo Cabrita")
                                email.set("paulo.cabrita@ionos.com")
                            }
                            developer {
                                id.set("jannotti-glaucio")
                                name.set("Glaucio Jannotti")
                                email.set("glaucio.jannotti@dengun.com")
                            }
                            developer {
                                id.set("augustocmleal")
                                name.set("Augusto Leal")
                                email.set("augusto.leal.ext@dengun.com")
                            }
                        }
                        scm {
                            connection.set("git@github.com:ids-basecamp/gradle-plugins-fork.git")
                            url.set("https://github.com/ids-basecamp/gradle-plugins-fork.git")
                        }
                    }
                }
            }
        }
    }

    tasks.withType<Jar> {
        metaInf {
            from("${rootProject.projectDir.path}/NOTICE.md")
            from("${rootProject.projectDir.path}/LICENSE")
        }
    }
}
