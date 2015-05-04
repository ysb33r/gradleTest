package org.ysb33r.gradle.gradletest

import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.ysb33r.gradle.gradletest.internal.GradleTestDownloader
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
        project.tasks.getByName(Names.DOWNLOADER_TASK) instanceof GradleTestDownloader
        project.tasks.getByName(Names.CLEAN_DOWNLOADER_TASK) instanceof Delete
        project.tasks.getByName(Names.CLEAN_DOWNLOADER_TASK).description?.size()

    }

    def 'Effect of plugin on afterEvaluate'() {
        given:
        project.with {
            apply plugin : 'org.ysb33r.gradletest'

            gradleTest {
                versions '1.999','1.998'
            }
            evaluate()
        }
        def defaultTask = project.tasks.getByName(Names.DEFAULT_TASK)
        def downloaderTask = project.tasks.getByName(Names.DOWNLOADER_TASK)


        expect:
        defaultTask.dependsOn.contains Names.DOWNLOADER_TASK
        downloaderTask.versions == ['1.999','1.998'] as Set<String>
    }
}

