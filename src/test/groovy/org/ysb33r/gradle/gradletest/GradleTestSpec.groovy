package org.ysb33r.gradle.gradletest

import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.internal.TestHelper
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class GradleTestSpec extends Specification {
    Project project = TestHelper.buildProject('gts')

    def "Unconfigured task"() {
        given:
        def task = project.tasks.create('Foo',GradleTest)

        expect:
        task.group == Names.TASK_GROUP
        task.description == "Runs all the compatibility tests for 'Foo'"
        task.versions.empty
        task.sourceDir == project.file("${project.projectDir}/src/Foo")
        task.outputDirs.empty

    }

    def "Basic Configured task"() {
        given:
        def task = project.tasks.create('Foo',GradleTest)
        def output = new File(project.buildDir,'Foo')

        task.versions '2.2','2.3'
        task.versions '2.4','2.4'

        expect:
        task.versions == (['2.2','2.3','2.4'] as Set<String>)
        task.outputDirs == [ new File(output,'2.2'),new File(output,'2.3'),new File(output,'2.4')] as Set

    }
}

