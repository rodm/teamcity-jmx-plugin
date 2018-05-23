package TeamCityJmxPlugin

import jetbrains.buildServer.configs.kotlin.v2017_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2017_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2017_2.Template
import jetbrains.buildServer.configs.kotlin.v2017_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2017_2.project
import jetbrains.buildServer.configs.kotlin.v2017_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2017_2.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.ScheduleTrigger
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2017_2.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.v2017_2.version

version = "2017.2"
project {
    uuid = "f0b75571-3aa3-414d-aafe-b39df547fb10"
    id = "TeamCityJmxPlugin"
    parentId = "TeamCityPlugins"
    name = "JMX Support"

    val vcsId = "TeamCityJmxPlugin_JmxPlugin"
    val vcsRoot = GitVcsRoot({
        uuid = "3a482ecb-7e55-4537-b473-340658b5680a"
        id = vcsId
        name = "jmx plugin"
        pollInterval = 3600
        url = "https://github.com/rodm/teamcity-jmx-plugin"
        useMirrors = false
    })
    vcsRoot(vcsRoot)

    features {
        versionedSettings {
            id = "PROJECT_EXT_8"
            mode = VersionedSettings.Mode.ENABLED
            rootExtId = vcsId
            showChanges = true
            settingsFormat = VersionedSettings.Format.KOTLIN
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
        }
    }

    val buildTemplate = Template({
        uuid = "6b487b15-714d-41de-8ea4-312183a1a2ea"
        id = "btTemplate3"
        name = "build plugin"

        params {
            param("gradle.opts", "")
            param("gradle.tasks", "clean build")
            param("java.home", "%java8.home%")
            param("version", "%teamcity80.version%")
        }

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
            feature {
                id = "jvm-monitor-plugin"
                type = "jvm-monitor-plugin"
                enabled = false
            }
        }
    })
    template(buildTemplate)

    val build80 = BuildType({
        template(buildTemplate)
        uuid = "7b13548f-b973-4b45-80c0-91b76d44dc98"
        id = "TeamCityJmxPlugin_BuildTeamCity80"
        name = "Build - TeamCity 8.0"

        artifactRules = "build/distributions/*.zip"

        params {
            param("version", "7.1.5")
        }
    })
    buildType(build80)

    val build81 = BuildType({
        template(buildTemplate)
        uuid = "57c8decb-afc5-40a6-890b-e938b93606a7"
        id = "TeamCityJmxPlugin_BuildTeamCity81"
        name = "Build - TeamCity 8.1"

        params {
            param("gradle.opts", "-Dteamcity.version=%version%")
            param("version", "%teamcity81.version%")
        }
    })
    buildType(build81)

    val build90 =  BuildType({
        template(buildTemplate)
        uuid = "162abe89-c678-4a4d-a29b-719e1f165564"
        id = "TeamCityJmxPlugin_BuildTeamCity90"
        name = "Build - TeamCity 9.0"

        params {
            param("gradle.opts", "-Dteamcity.version=%version%")
            param("version", "%teamcity90.version%")
        }
    })
    buildType(build90)

    val build91 = BuildType({
        template(buildTemplate)
        uuid = "f241df8c-3530-4d6a-aa64-595643d08f62"
        id = "TeamCityJmxPlugin_BuildTeamCity91"
        name = "Build - TeamCity 9.1"

        params {
            param("gradle.opts", "-Dteamcity.version=%version%")
            param("version", "%teamcity91.version%")
        }
    })
    buildType(build91)

    val build100 = BuildType({
        template(buildTemplate)
        uuid = "e41a12c8-b4d7-476b-bb5f-edaeaba23bcb"
        id = "TeamCityJmxPlugin_BuildTeamCity100"
        name = "Build - TeamCity 10.0"

        params {
            param("gradle.opts", "-Dteamcity.version=%version%")
            param("version", "%teamcity100.version%")
        }
    })
    buildType(build100)

    val reportCodeQuality = BuildType({
        template(buildTemplate)
        uuid = "28454d8c-3494-428e-ac2f-bcafab96e47c"
        id = "TeamCityJmxPlugin_ReportCodeQuality"
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts% -Dteamcity.version=%version%")
            param("gradle.tasks", "clean build sonarqube")
            param("version", "%teamcity81.version%")
        }

        triggers {
            schedule {
                id = "TRIGGER_3"
                schedulingPolicy = weekly {
                    dayOfWeek = ScheduleTrigger.DAY.Saturday
                    hour = 11
                    minute = 15
                }
                branchFilter = ""
                triggerBuild = always()
            }
        }

        disableSettings("vcsTrigger")
    })
    buildType(reportCodeQuality)
}
