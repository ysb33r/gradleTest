package org.ysb33r.gradle.gradletest.internal

import groovy.transform.TupleConstructor
import org.gradle.api.Project
import org.gradle.process.ExecResult

/**
 * @author Schalk W. Cronjé
 */
@TupleConstructor
class TestRunner {

    Project project
    File gradleLocationDir
    File testProjectDir
    File initScript
    String version
    String testName

    ExecResult execResult = null

    ExecResult run() {

        File settings = new File(testProjectDir,'settings.gradle')

        if(!settings.exists()) {
            settings.text = ''
        }

        project.logger.info "Running ${testName} against Gradle ${version}"

        execResult = project.javaexec {
            classpath "${gradleLocationDir}/lib/gradle-launcher-${version}.jar"
            workingDir testProjectDir.absoluteFile

            main 'org.gradle.launcher.GradleMain'
            args '--project-cache-dir',"${testProjectDir}/.gradle"
            args '--gradle-user-home',  "${testProjectDir}/../home"
            args '--no-daemon'
            args '--full-stacktrace'
            args '--info'
            args '--init-script',initScript.absolutePath

            if(project.gradle.startParameter.offline) {
                args '--offline'
            }

            args 'runGradleTest'

            systemProperties 'org.gradle.appname' : this.class.name

            // Capture standardOutput and errorOutput

            // systemProperties & environment in a later release
            // lib/gradle-launcher-2.0.jar
        }
    }
}
