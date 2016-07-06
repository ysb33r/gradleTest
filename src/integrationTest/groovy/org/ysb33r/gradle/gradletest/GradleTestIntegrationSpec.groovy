/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2015
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


class GradleTestIntegrationSpec extends GradleTestIntegrationSpecification {

    static final List TESTNAMES = ['alpha','beta','gamma']
    static final File GRADLETESTREPO = new File(System.getProperty('GRADLETESTREPO') ?: 'build/integrationTest/repo').absoluteFile
    @Delegate Project project

    void setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()

        buildFile = new File(project.projectDir,'build.gradle')
        writeBuildScriptHeader(project.gradle.gradleVersion)

        buildFile <<  """
        repositories {
            flatDir {
                dirs '${GRADLETESTREPO.toURI()}'
            }
        }
        gradleTest {
            versions '2.13', '2.9', '2.8', '2.5', '2.1'
            gradleDistributionUri '${GRADLETESTREPO.toURI()}'

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