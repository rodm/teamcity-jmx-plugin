
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("io.github.rodm.teamcity-server")
    id ("io.github.rodm.teamcity-environments")
    id ("org.sonarqube")
}

group = "com.github.rodm"
version = "1.3-SNAPSHOT"

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"
extra["downloadsDir"] = project.findProperty("downloads.dir") as String? ?: "$rootDir/downloads"
extra["serversDir"] = project.findProperty("servers.dir") as String? ?: "$rootDir/servers"
extra["java11Home"] = project.findProperty("java11.home") ?: "/opt/jdk-11.0.2"

base {
    archivesName.set("jmx-support")
}

dependencies {
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:4.11.0")
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

teamcity {
    version = extra["teamcityVersion"] as String

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
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String

        register("teamcity2018.1") {
            version = "2018.1.5"
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
        }

        register("teamcity2024.03") {
            version = "2024.03.1"
            javaHome = extra["java11Home"] as String
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
        }
    }
}
