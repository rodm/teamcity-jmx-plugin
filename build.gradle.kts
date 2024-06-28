
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    alias (libs.plugins.teamcity.server)
    alias (libs.plugins.teamcity.environments)
    alias (libs.plugins.sonarqube)
}

group = "com.github.rodm"
version = "1.3-SNAPSHOT"

base {
    archivesName = "jmx-support"
}

dependencies {
    testImplementation (libs.junit)
    testImplementation (libs.mockito)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks {
    test {
        finalizedBy (jacocoTestReport)
    }

    jacocoTestReport {
        reports {
            xml.required = true
        }
    }
}

val teamcityVersion = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

teamcity {
    version = teamcityVersion

    server {
        descriptor {
            name = "jmx-support"
            displayName = "JMX support"
            version = project.version as String
            description = "Exposes Server, Agent and Project attributes and related build statistics via JMX."
            vendorName = "Rod MacKenzie"
            vendorUrl = "https://github.com/rodm"
            email = "rod.n.mackenzie@gmail.com"
            useSeparateClassloader = true
            minimumBuild = "58245"
        }

        publish {
            token = findProperty("jetbrains.token") as String?
            notes = "Fixes incompatibility with TeamCity 2023.05"
        }
    }

    environments {
        val java11Home = project.findProperty("java11.home") as String? ?: "/opt/jdk-11.0.2"
        val serverDebugOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

        register("teamcity2018.1") {
            version = "2018.1.5"
            serverOptions (serverDebugOptions)
        }

        register("teamcity2024.03") {
            version = "2024.03.3"
            javaHome = java11Home
            serverOptions (serverDebugOptions)
        }
    }
}
