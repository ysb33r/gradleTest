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
import org.gradle.util.GradleVersion
import org.ysb33r.gradle.gradletest.internal.GradleTestIntegrationSpecification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS


class CommandLineIntegrationSpec extends GradleTestIntegrationSpecification {

    static final List TESTNAMES = ['alpha']
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
            versions '${GradleTestIntegrationSpecification.AVAILABLE_GRADLE_VERSIONS[0]}'
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

    def "Running with 3.0+ honours --rerun-tasks"() {

        setup:
        genTestStructureForSuccess(srcDir)
        buildFile << """
        gradleTest {
                if(!gradle.startParameter.isRerunTasks()) {
                    throw new GradleException("--rerun-tasks not set")
                }
         }
"""

        when:
        println "I'm starting this test chain and I'm " + GradleVersion.current()
        def result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments('gradleTest','-i','--rerun-tasks')
            .withPluginClasspath(readMetadataFile())
            .forwardOutput()
            .build()

        then:
        result.task(":gradleTest").outcome == SUCCESS

    }

}