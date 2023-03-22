
plugins {
    id ("org.gradle.java")
    id ("org.gradle.jacoco")
    id ("io.github.rodm.teamcity-server") version "1.5"
    id ("io.github.rodm.teamcity-environments") version "1.5"
    id ("org.sonarqube") version "3.4.0.2513"
}

group = "com.github.rodm"
version = "1.3-SNAPSHOT"

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"
extra["downloadsDir"] = project.findProperty("downloads.dir") as String? ?: "$rootDir/downloads"
extra["serversDir"] = project.findProperty("servers.dir") as String? ?: "$rootDir/servers"
extra["java8Home"] = project.findProperty("java8.home") ?: "/opt/jdk1.8.0_152"

base {
    archivesName.set("jmx-support")
}

dependencies {
    testImplementation ("junit:junit:4.13")
    testImplementation ("org.mockito:mockito-core:3.3.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    test {
        finalizedBy (jacocoTestReport)
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
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
            token = findProperty("jetbrains.token") as String
            notes = "Fixes incompatibility with TeamCity 2020.1"
        }
    }

    environments {
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String

        register("teamcity2018.1") {
            version = "2018.1.5"
            serverOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
        }

        register("teamcity2020.2") {
            version = "2020.2.1"
            serverOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
        }
    }
}
