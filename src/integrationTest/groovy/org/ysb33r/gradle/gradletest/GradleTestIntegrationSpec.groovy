package org.ysb33r.gradle.gradletest

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.ysb33r.gradle.gradletest.internal.GradleTestIntegrationSpecification


class GradleTestIntegrationSpec extends GradleTestIntegrationSpecification {

    static final List TESTNAMES = ['alpha','beta','gamma']
    static final File GRADLETESTREPO = new File(System.getProperty('GRADLETESTREPO') ?: 'build/integrationTest/repo').absoluteFile
    @Delegate Project project


    void setup() {
//        project = ProjectBuilder.builder().withProjectDir(new File('/Users/schalkc/Projects/GradleTest/TMP')).build()
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        buildFile = new File(project.projectDir,'build.gradle')
        writeBuildScriptHeader(project.gradle.gradleVersion)

        buildFile <<  """
        repositories {
            flatDir {
                dirs '${GRADLETESTREPO}'
            }
        }
        gradleTest {
            versions '2.13', '2.9', '2.8', '2.5', '2.1'
            gradleDistributionUri file('${GRADLETESTREPO}').toURI()

            doFirst {
                println 'I am the actual invocation of GradleTest (from GradleIntegrationSpec) and I am ' + GradleVersion.current()
            }
        }
        """

        new File(project.projectDir,'settings.gradle').text = ''


        File srcDir = new File(project.projectDir,'src/gradleTest')
        srcDir.mkdirs()
        TESTNAMES.each {
            File testDir = new File(srcDir,it)
            testDir.mkdirs()
            new File(testDir,'build.gradle').text = '''
            task runGradleTest << {
                println "I'm  runGradleTest and I'm " + GradleVersion.current()

                println "Hello, ${project.name}"
            }
'''
        }
    }



    def "Running with 2.13+ invokes new GradleTestKit"() {

        when:
        println "I'm starting this test chain and I'm " + GradleVersion.current()
        def result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments('gradleTest','-i')
            .withPluginClasspath(readMetadataFile())
            .forwardOutput()
            .build()

        then:
        result.task(":gradleTest").outcome == TaskOutcome.SUCCESS
//        result.output.contains('Hello world!')
    }
}