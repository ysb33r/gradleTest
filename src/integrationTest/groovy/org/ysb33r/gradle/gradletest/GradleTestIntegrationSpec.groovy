package org.ysb33r.gradle.gradletest

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.internal.IntegrationTestHelper
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class GradleTestIntegrationSpec extends Specification {
    static final File repoTestFile = new File(System.getProperty('GRADLETESTREPO'))
    Project project = IntegrationTestHelper.buildProject('gtis')
    File simpleTestSrcDir = new File(IntegrationTestHelper.PROJECTROOT,'build/resources/integrationTest/gradleTest')
    File simpleTestDestDir = new File(project.projectDir,'src/'+Names.DEFAULT_TASK)
    File expectedOutputDir = new File(project.buildDir,Names.DEFAULT_TASK + '/' + project.gradle.gradleVersion )

    void setup() {

        assert  simpleTestSrcDir.exists()
        FileUtils.copyDirectory simpleTestSrcDir, simpleTestDestDir

        project.allprojects {
            apply plugin: 'org.ysb33r.gradletest'

            project.repositories {
                flatDir {
                    dirs repoTestFile.parentFile
                }
            }

            // Restrict the test to no downloading except from a local source
            gradleLocations {
                includeGradleHome = false
                searchGradleUserHome = false
                searchGvm = false
                download = false
                useGradleSite = false
                downloadToGradleUserHome = false
                search IntegrationTestHelper.CURRENT_GRADLEHOME.parentFile
            }

            dependencies {
                gradleTest ':commons-cli:1.2'
            }

            // Only use the current gradle version for testing
            gradleTest {
                versions gradle.gradleVersion
            }
        }
    }

    def "A simple gradleTest that is expected to pass"() {

        when: 'Evaluation has been completed'
        project.evaluate()

        then:
        project.tasks.gradleTest.versions.size()
        project.tasks.gradleTest.sourceDir == simpleTestDestDir
        project.tasks.gradleTest.outputDirs.contains( expectedOutputDir )

        when: 'The tasks is executed'
        project.tasks.gradleTest.execute()

        then:
        new File(expectedOutputDir,'simpleTest').exists()
        project.tasks.gradleTest.didWork
        false
    }

}