package org.ysb33r.gradle.gradletest

import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.internal.TestHelper
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class GradleTestPluginSpec extends Specification {
    Project project = TestHelper.buildProject('gtp')

    def "Applying the plugin"() {
        given:
        project.with {
            apply plugin : 'org.ysb33r.gradletest'
        }

        expect:
        project.tasks.getByName(Names.DEFAULT_TASK) instanceof GradleTest
        project.extensions.getByName(Names.EXTENSION) instanceof GradleTestExtension
        project.configurations.findByName(Names.CONFIGURATION) != null
        project.configurations.findByName(Names.DEFAULT_TASK) != null
    }
}