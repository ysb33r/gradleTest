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
package org.ysb33r.gradle.gradletest.legacy20

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.legacy20.internal.IntegrationTestHelper
import spock.lang.Issue
import spock.lang.Specification

/**
 * @author Schalk W. Cronjé
 */
class GradleTestInitScriptIntegrationSpec extends Specification {
    static final File repoTestFile = new File(System.getProperty('GRADLETESTREPO'))
    Project project = IntegrationTestHelper.buildProject('gtisis')
    File simpleTestSrcDir = new File(IntegrationTestHelper.PROJECTROOT,'build/resources/integrationTest/initScripts/gradleTest')
    File simpleTestDestDir = new File(project.projectDir,'src/'+Names.DEFAULT_TASK)
    File expectedOutputDir = new File(project.buildDir,Names.DEFAULT_TASK + '/' + project.gradle.gradleVersion )
    File repoDir = new File(project.projectDir,'srcRepo').absoluteFile

    void setup() {

        assert  simpleTestSrcDir.exists()
        FileUtils.copyDirectory simpleTestSrcDir, simpleTestDestDir
        IntegrationTestHelper.createTestRepo(repoDir)

        project.allprojects {
            apply plugin: 'org.ysb33r.gradletest'

            project.repositories {
                flatDir {
                    dirs repoDir
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
                gradleTest ':doxygen:0.2'
            }

            // Only use the current gradle version for testing
            gradleTest {
                versions gradle.gradleVersion
                initscript new File(simpleTestSrcDir,'initScriptTest/templateFile.gradle')
            }
        }
    }

    @Issue('https://github.com/ysb33r/gradleTest/issues/30')
    def "Run a gradletest with a custom initscript" () {
        given: "The project is evaluated"
        project.evaluate()

        when: "The task is evaluated"
        project.tasks.gradleTest.execute()
        def results = project.tasks.gradleTest.testResults

        then: "Expect the templateFile to have been loaded"
        results[0].passed
    }
}