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
package org.ysb33r.gradle.gradletest.legacy20.internal

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.legacy20.LegacyGradleTestPlugin
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class InfrastructureIntegrationSpec extends Specification {
    static final File PROJECTROOT= new File( System.getProperty('PROJECTROOT') ?: '.' ).absoluteFile
    static final File SIMPLETEST_SRCDIR = new File(PROJECTROOT,'build/resources/integrationTest/gradleTest')
    static final File GRADLETESTREPO = new File( System.getProperty('GRADLETESTREPO') ?: 'build/integrationTest/repo' )

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    Project project
    String gradleVersion
    File simpleTestDestDir
    File expectedOutputDir
    File initScript
    File gradleLocation

    boolean exists( final String path ) {
        new File("${project.buildDir}/${Names.DEFAULT_TASK}/${path}" ).exists()
    }

    void setup() {
        assert  SIMPLETEST_SRCDIR.exists()
        project = ProjectBuilder.builder().withName('iis').withProjectDir(testProjectDir.root).build()
        gradleLocation = testProjectDir.newFolder('gradlehome')
        gradleVersion = project.gradle.gradleVersion
        simpleTestDestDir = new File(project.projectDir,'src/' + Names.DEFAULT_TASK)
        expectedOutputDir = new File(project.buildDir,Names.DEFAULT_TASK + '/' + project.gradle.gradleVersion )
        initScript = new File(project.projectDir,'foo.gradle')

        FileUtils.copyDirectory SIMPLETEST_SRCDIR, simpleTestDestDir

        project.repositories {
            flatDir {
                dirs GRADLETESTREPO
            }
        }

        project.allprojects {
            apply plugin: LegacyGradleTestPlugin

            // Restrict the test to no downloading
            gradleLocations {
                searchGradleUserHome = false
                searchGvm = false
                download = false
            }

            dependencies {
                gradleTest 'commons-cli:commons-cli:1.2'
            }

        }

        initScript.text = '// Nothing here'

    }

    def "Creating an infrastructure for compatibility testing"() {
        when:
        assert new File(simpleTestDestDir,'simpleTest').exists()

        Map<String,File> locations = [:]
        locations[gradleVersion] = gradleLocation
        def runners = Infrastructure.create (
            project : project,
            tests : ['simpleTest'],
            locations : locations,
            name : Names.DEFAULT_TASK,
            sourceDir : simpleTestDestDir,
            initScript : initScript,
            versions : [ gradleVersion ]
        )

        then: "These files must be created"
        exists ''
        exists 'init20.gradle'
        exists gradleVersion
        exists "${gradleVersion}/simpleTest"
        exists "${gradleVersion}/simpleTest/build.gradle"
        exists 'repo'
        exists 'repo/commons-cli-1.2.jar'

        and: 'Runners should be created'
        runners.size() == 1
        runners[0].project == project
        runners[0].gradleLocationDir == gradleLocation
        runners[0].testProjectDir == new File(project.buildDir,Names.DEFAULT_TASK + '/' + gradleVersion + '/' + 'simpleTest')
        runners[0].initScript == new File("${project.buildDir}/${Names.DEFAULT_TASK}/init20.gradle")
        runners[0].version == gradleVersion
        runners[0].testName == 'simpleTest'
    }
}
