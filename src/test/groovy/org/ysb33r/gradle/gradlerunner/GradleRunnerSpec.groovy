package org.ysb33r.gradle.gradlerunner

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification



class GradleRunnerSpec extends Specification {

    Project project = ProjectBuilder.builder().build()
    GradleRunnerSteps gradleRunner = project.tasks.create('gradleRunner',GradleRunnerSteps)

    def 'Add basic steps and execute'() {
        when:
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

}