
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

    params {
        param("teamcity.ui.settings.readOnly", "true")
    }

    val buildTemplate = template {
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
        id("Build1")
        name = "Build - TeamCity 2018.1"

        artifactRules = "build/distributions/*.zip"
    }

    val build2 = buildType {
        templates(buildTemplate)
        id("Build2")
        name = "Build - TeamCity 2018.2"

        params {
            param("gradle.opts", "-Dteamcity.version=2018.2")
        }
    }

    val build3 =  buildType {
        templates(buildTemplate)
        id("Build3")
        name = "Build - TeamCity 2019.1"

        params {
            param("gradle.opts", "-Dteamcity.version=2019.1")
        }
        paused = true
    }

    val reportCodeQuality = buildType {
        templates(buildTemplate)
        id("ReportCodeQuality")
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts%")
            param("gradle.tasks", "clean build sonarqube")
        }
    }

    buildTypesOrder = arrayListOf(build1, build2, build3, reportCodeQuality)
}
