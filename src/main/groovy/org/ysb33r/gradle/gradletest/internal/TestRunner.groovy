package org.ysb33r.gradle.gradletest.internal

import org.gradle.api.Project
import org.gradle.process.ExecResult

/**
 * @author Schalk W. Cronjé
 */
class TestRunner {

    Project project

    ExecResult run(
        final String version,
        final File GradleLocationDir,
        final File testProjectDir,
        final File initScript
    ) {

        project.javaexec {
            classpath "${GradleLocationDir}/lib/gradle-launcher-${version}.jar"
            workingDir testProjectDir.absoluteFile

            main 'org.gradle.launcher.GradleMain'
            args '--project-cache-dir',"${testProjectDir}/.gradle"
            args '--gradle-home-dir',  "${testProjectDir}/../home"
            args '--no-daemon'
            args '--full-stacktrace'
            args '--info'
            args '--init-script',initScript.absolutePath

            if(p.gradle.startParameter.offline) {
                args '--offline'
            }

            systemProperties 'org.gradle.appname' : this.class.name

            // Capture standardOutput and errorOutput

            // systemProperties & environment in a later release
            // lib/gradle-launcher-2.0.jar
        }
    }
}
