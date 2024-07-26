import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

project {

    buildType(Build)
    buildType(Installer)
    buildType(Deployment)
    buildTypesOrder = arrayListOf(Build, Installer, Deployment)

}

object Build : BuildType({
    name = "Build"

    artifactRules = "+:text.txt => text.txt"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            id = "simpleRunner"
            scriptContent = "echo Build Number: %build.number%  > text.txt"
        }
    }

    triggers {
        vcs {
        }
    }

    features {
        perfmon {
        }
    }
})

object Deployment : BuildType({
    name = "DEPLOYMENT"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    vcs {
        root(DslContext.settingsRoot)
    }

    dependencies {
        dependency(Build) {
            snapshot {
            }

            artifacts {
                artifactRules = "+:text.txt"
            }
        }
        dependency(Installer) {
            snapshot {
                synchronizeRevisions = false
            }

            artifacts {
                artifactRules = "+:installer.txt"
            }
        }
    }
})

object Installer : BuildType({
    name = "Installer"

    artifactRules = "+:installer.txt => installer.txt"

    steps {
        script {
            id = "simpleRunner"
            scriptContent = """
                cat text.txt
                cat text.txt > installer.txt
                                
                echo ##teamcity[buildStatus status='SUCCESS' text='{build.status.text}']
            """.trimIndent()
        }
    }

    dependencies {
        dependency(Build) {
            snapshot {
            }

            artifacts {
                artifactRules = "+:text.txt"
            }
        }
    }
})
