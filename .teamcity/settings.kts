
import jetbrains.buildServer.configs.kotlin.v2018_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2018_2.version

version = "2018.2"

project {

    val vcsId = "JmxSupport"
    val vcsRoot = GitVcsRoot {
        id(vcsId)
        name = "jmx-support"
        url = "https://github.com/rodm/teamcity-jmx-plugin"
        useMirrors = false
    }
    vcsRoot(vcsRoot)

    val buildTemplate = template {
        uuid = "6b487b15-714d-41de-8ea4-312183a1a2ea"
        id("Build")
        name = "build plugin"

        vcs {
            root(vcsRoot)
            checkoutMode = CheckoutMode.ON_SERVER
        }

        steps {
            gradle {
                id = "RUNNER_19"
                tasks = "%gradle.tasks%"
                gradleParams = "%gradle.opts%"
                useGradleWrapper = true
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        triggers {
            vcs {
                id = "vcsTrigger"
                branchFilter = ""
            }
        }

        failureConditions {
            executionTimeoutMin = 15
        }

        features {
            feature {
                id = "perfmon"
                type = "perfmon"
            }
        }

        params {
            param("gradle.opts", "")
            param("gradle.tasks", "clean build")
            param("java.home", "%java8.home%")
        }
    }

    val build1 = buildType {
        templates(buildTemplate)
        uuid = "7b13548f-b973-4b45-80c0-91b76d44dc98"
        id("Build1")
        name = "Build - TeamCity 2018.1"

        artifactRules = "build/distributions/*.zip"
    }

    val build2 = buildType {
        templates(buildTemplate)
        uuid = "57c8decb-afc5-40a6-890b-e938b93606a7"
        id("Build2")
        name = "Build - TeamCity 2018.2"

        params {
            param("gradle.opts", "-Dteamcity.version=2018.2")
        }
    }

    val build3 =  buildType {
        templates(buildTemplate)
        uuid = "162abe89-c678-4a4d-a29b-719e1f165564"
        id("Build3")
        name = "Build - TeamCity 2019.1-SNAPSHOT"

        params {
            param("gradle.opts", "-Dteamcity.version=2019.1-SNAPSHOT")
        }
    }

    val reportCodeQuality = buildType {
        templates(buildTemplate)
        uuid = "28454d8c-3494-428e-ac2f-bcafab96e47c"
        id("ReportCodeQuality")
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts%")
            param("gradle.tasks", "clean build sonarqube")
        }
    }

    buildTypesOrder = arrayListOf(build1, build2, build3, reportCodeQuality)
}
