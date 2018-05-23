package TeamCityJmxPlugin

import jetbrains.buildServer.configs.kotlin.v2017_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2017_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2017_2.FailureAction
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
            param("system.teamcity.version", "%version%")
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
            param("gradle.opts", "-Dteamcity.version=%version% -Dteamcity.home=%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%")
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
            param("env.JAVA_HOME", "%java.home%")
            param("gradle.opts", "-Dteamcity.version=%version% -Dteamcity.home=%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%")
            param("system.teamcity.home", "%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%")
            param("version", "%teamcity90.version%")
        }

        disableSettings("RUNNER_5")
    })
    buildType(build90)

    val build91 = BuildType({
        template(buildTemplate)
        uuid = "f241df8c-3530-4d6a-aa64-595643d08f62"
        id = "TeamCityJmxPlugin_BuildTeamCity91"
        name = "Build - TeamCity 9.1"

        params {
            param("env.JAVA_HOME", "%java.home%")
            param("gradle.opts", "-Dteamcity.version=%version% -Dteamcity.home=%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%")
            param("system.teamcity.home", "%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%")
            param("version", "%teamcity91.version%")
        }

        disableSettings("RUNNER_5")
    })
    buildType(build91)

    val build100 = BuildType({
        template(buildTemplate)
        uuid = "e41a12c8-b4d7-476b-bb5f-edaeaba23bcb"
        id = "TeamCityJmxPlugin_BuildTeamCity100"
        name = "Build - TeamCity 10.0"

        params {
            param("env.JAVA_HOME", "%java.home%")
            param("gradle.opts", "-Dteamcity.version=%version% -Dteamcity.home=%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%")
            param("system.teamcity.home", "%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%")
            param("version", "%teamcity100.version%")
        }

        disableSettings("RUNNER_5")
    })
    buildType(build100)

    val publishTemplate = Template({
        uuid = "4158df22-d9b9-432a-89a5-40623805b716"
        id = "TeamCityJmxPlugin_PublishPlugin"
        name = "publish plugin"

        params {
            param("gradle.opts", "-x build -x jar -x serverPlugin -PrepositoryUrl=%repository.url% -PrepositoryUsername=%repository.user% -PrepositoryPassword=%repository.password%")
            param("java.home", "%java8.home%")
            param("repository.password", "")
            param("repository.url", "")
            param("repository.user", "")
            param("system.teamcity.home", "%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%")
            param("version", "%teamcity80.version%")
        }

        vcs {
            root(vcsRoot)
            checkoutMode = CheckoutMode.ON_SERVER
        }

        steps {
            gradle {
                id = "RUNNER_20"
                tasks = "publish"
                gradleParams = "%gradle.opts%"
                useGradleWrapper = true
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
        }

        failureConditions {
            executionTimeoutMin = 5
        }

        features {
            feature {
                id = "perfmon"
                type = "perfmon"
            }
        }

        dependencies {
            dependency(build80) {
                snapshot {
                    onDependencyFailure = FailureAction.FAIL_TO_START
                }

                artifacts {
                    id = "ARTIFACT_DEPENDENCY_1"
                    cleanDestination = true
                    artifactRules = "jmx-plugin-1.1-SNAPSHOT.zip => build/distributions"
                }
            }
        }
    })
    template(publishTemplate)

    val publishToBintray = BuildType({
        template(publishTemplate)
        uuid = "c06de9c8-5763-4f3f-ac85-923edf152e47"
        id = "TeamCityJmxPlugin_PublishToBintray"
        name = "Publish to Bintray"

        params {
            param("gradle.opts", """
            -x build -x jar -x serverPlugin
            -Dversion=%system.version% -PrepositoryUrl=%repository.url% -PrepositoryUsername=%repository.user% -PrepositoryPassword=%repository.password%
        """.trimIndent())
            param("repository.password", "%bintray.repository.password%")
            param("repository.url", "%bintray.repository.url%/teamcity-jmx-plugin")
            param("repository.user", "%bintray.repository.user%")
            param("system.teamcity.version", "%version%")
            param("system.version", "1.1-b%dep.TeamCityJmxPlugin_BuildTeamCity80.build.number%")
        }
    })
    buildType(publishToBintray)

    val publishToNexus = BuildType({
        template(publishTemplate)
        uuid = "1f64bac8-8bf1-4488-bd74-75e462492e41"
        id = "TeamCityJmxPlugin_PublishToNexus"
        name = "Publish to Nexus"

        params {
            param("repository.password", "%nexus.repository.password%")
            param("repository.url", "%nexus.repository.snapshots.url%")
            param("repository.user", "%nexus.repository.user%")
        }
    })
    buildType(publishToNexus)

    val reportCodeQuality = BuildType({
        template(buildTemplate)
        uuid = "28454d8c-3494-428e-ac2f-bcafab96e47c"
        id = "TeamCityJmxPlugin_ReportCodeQuality"
        name = "Report - Code Quality"

        params {
            param("gradle.opts", """
            %sonar.opts%
            -Dteamcity.version=%version% -Dteamcity.home=%teamcity.agent.jvm.user.home%/servers/TeamCity-%version%
        """.trimIndent())
            param("gradle.tasks", "clean sonarqube")
            param("java.home", "%java8.home%")
            param("version", "%teamcity81.version%")
        }

        steps {
            gradle {
                id = "RUNNER_37"
                enabled = false
                tasks = "sonarqube"
                buildFile = ""
                gradleParams = "%gradle.opts%"
                useGradleWrapper = true
                enableStacktrace = true
                jdkHome = "%java.home%"
            }
            stepsOrder = arrayListOf("RUNNER_19", "RUNNER_37")
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

        disableSettings("RUNNER_5", "vcsTrigger")
    })
    buildType(reportCodeQuality)
}
