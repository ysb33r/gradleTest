/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2015 - 2017
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
package org.ysb33r.gradle.gradletest

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.ysb33r.gradle.gradletest.internal.GradleTestIntegrationSpecification
import spock.lang.Issue


class GradleTestIntegrationSpec extends GradleTestIntegrationSpecification {

    static final List TESTNAMES = ['alpha','beta','gamma']
    static final File GRADLETESTREPO = new File(System.getProperty('GRADLETESTREPO') ?: 'build/integrationTest/repo').absoluteFile
    @Delegate Project project
    File srcDir

    void setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        buildFile = new File(project.projectDir,'build.gradle')
        writeBuildScriptHeader(project.gradle.gradleVersion)

        buildFile <<  """
        repositories {
            flatDir {
                dirs '${GRADLETESTREPO.toURI()}'.toURI()
            }
        }
        dependencies {
          gradleTest 'org.ysb33r.gradle:doxygen:0.2'
        }
        gradleTest {
            versions '3.2', '2.13', '2.9', '2.8', '2.5', '2.1'
            gradleDistributionUri '${GRADLETESTREPO.toURI()}'

            doFirst {
                println 'I am the actual invocation of GradleTest (from GradleIntegrationSpec) and I am ' + GradleVersion.current()
            }
        }
        """

        new File(project.projectDir,'settings.gradle').text = ''


        srcDir = new File(project.projectDir,'src/gradleTest')
        srcDir.mkdirs()

    }

    void genTestStructureForSuccess( File genToDir ) {
        TESTNAMES.each {
            File testDir = new File(genToDir, it)
            testDir.mkdirs()
            new File(testDir, 'build.gradle').text = '''
            task runGradleTest  {
                doLast {
                    println "I'm  runGradleTest and I'm " + GradleVersion.current()
                    println "Hello, ${project.name}"
                }
            }
'''
        }
    }

    void genTestStructureForFailure() {
        TESTNAMES.each {
            File testDir = new File(srcDir, it)
            testDir.mkdirs()
            if(it == 'gamma') {
                new File(testDir, 'build.gradle').text = '''
            task runGradleTest  {
                doLast {
                    throw new GradleException('Expect this to fail')
                }
            }
'''

            } else {
                new File(testDir, 'build.gradle').text = '''
            task runGradleTest  {
                doLast {
                    println "I'm  runGradleTest and I'm " + GradleVersion.current()
                    println "Hello, ${project.name}"
                }
            }
'''
            }
        }
    }

    def "Running with 2.13+ invokes new GradleTestKit"() {

        setup:
        genTestStructureForSuccess(srcDir)

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

        and: "There is a file in the local repo"
        new File("${buildDir}/gradleTest/repo/doxygen-0.2.jar").exists()
    }

    def "Setting up a second test set for Gradle 2.13+"() {
        setup:
        buildFile << """
        additionalGradleTestSet ('second')

        dependencies {
          secondGradleTest 'org.ysb33r.gradle:doxygen:0.2'
        }

        secondGradleTest {
            versions tasks.gradleTest.versions
            gradleDistributionUri '${GRADLETESTREPO.toURI()}'

            doFirst {
                println 'I am the actual invocation of secondGradleTest (from GradleIntegrationSpec) and I am ' + GradleVersion.current()
            }
        }

"""
        File srcDir2 = new File(project.projectDir,'src/secondGradleTest')
        srcDir2.mkdirs()
        genTestStructureForSuccess(srcDir2)


        when:
        def result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments('secondGradleTest','-i')
            .withPluginClasspath(readMetadataFile())
            .forwardOutput()
            .build()

        then:
        result.task(":secondGradleTest").outcome == TaskOutcome.SUCCESS

        and: "There is a file in the local repo"
        new File("${buildDir}/secondGradleTest/repo/doxygen-0.2.jar").exists()
    }

    @Issue("https://github.com/ysb33r/gradleTest/issues/52")
    def "Handle expected failure"() {

        setup:
        buildFile << """
        gradleTest {
            expectFailure ~/gamma/
        }
"""
        genTestStructureForFailure()

        when:
        println "I'm starting this test chain and I'm " + GradleVersion.current()
        def result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments('gradleTest','-i')
            .withPluginClasspath(readMetadataFile())
            .forwardOutput()
            .build()
        println new File("${projectDir}/build/gradleTest/src/GammaGroovyDSLCompatibilitySpec.groovy").text

        then:
        result.task(":gradleTest").outcome == TaskOutcome.SUCCESS

    }

    @Issue('https://github.com/ysb33r/gradleTest/issues/59')
    def "Adding folders will re-run test task"() {
        setup: "A GradleTest structure with three tests"
        def gradleRunner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments('gradleTest','-i')
            .withPluginClasspath(readMetadataFile())
            .forwardOutput()
        genTestStructureForSuccess(srcDir)

        when: 'When executed'
        println "I'm starting this test chain and I'm " + GradleVersion.current()
        def result = gradleRunner.build()

        then: 'The tests execute successfully'
        result.task(":gradleTest").outcome == TaskOutcome.SUCCESS

        when: 'It is executed again'
        println "I'm running this test chain again and I'm " + GradleVersion.current()
        result = gradleRunner.build()

        then: 'The task should be skipped'
        result.task(":gradleTest").outcome == TaskOutcome.UP_TO_DATE

        when: 'An additional test is added'
        File testDir = new File(srcDir, 'delta')
        testDir.mkdirs()
        new File(testDir, 'build.gradle').text = '''
            task runGradleTest  {
                doLast {
                    println "DELTA: I'm  runGradleTest and I'm " + GradleVersion.current()
                    println "DELTA: Hello, ${project.name}"
                }
            }
'''

        and: 'The task is executed again'
        result = gradleRunner.build()

        then: 'Then the task will be successfully executed'
        result.task(":gradleTest").outcome == TaskOutcome.SUCCESS
    }

}