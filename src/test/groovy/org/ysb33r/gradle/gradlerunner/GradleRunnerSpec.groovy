package org.ysb33r.gradle.gradlerunner

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.gradle.gradlerunner.internal.GradleStep
import spock.lang.Specification



class GradleRunnerSpec extends Specification {

    Project project = ProjectBuilder.builder().build()

    def 'Add basic steps and execute'() {
        when:
        GradleRunnerSteps gradleRunner = project.tasks.create('gradleRunner',GradleRunnerSteps)

        gradleRunner.step'closure', {
            StepInfo info -> println project.name
        }

        gradleRunner.step'action', new Action<StepInfo>() {
            void execute(StepInfo i) {
                println i.getName()
            }
        }

        gradleRunner.step 'gradle', 'tasks'

        gradleRunner.failingStep 'failing', 'non-existant-task'

        then:
        gradleRunner.steps.size() == 4

        when:
        gradleRunner.execute()
        File reportDir = gradleRunner.getStepReportDir( 'gradle')

        then:
        gradleRunner.getStepReportDir(0) == new File("${project.buildDir}/reports/runners/gradleRunner/closure")
        gradleRunner.workingDir == new File("${project.buildDir}/runners/gradleRunner")
        reportDir.parentFile.parentFile.exists()
        reportDir.parentFile.exists()
        reportDir.exists()
        new File(reportDir,'out.txt').exists()

    }

    def 'Running the task from the plugin'() {
        setup:
        project.allprojects {
            apply plugin : 'org.ysb33r.gradlerunner'

            gradleRunner {
                step 'gradle', 'tasks', '--all'
            }
        }

        when:
        project.evaluate()
        project.gradleRunner.execute()

        then:
        ((GradleStep)(project.gradleRunner.getSteps()[0])).output.exists()

    }
}