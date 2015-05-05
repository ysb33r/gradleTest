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
package org.ysb33r.gradle.gradletest.internal

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.Names
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class InfrastructureIntegrationSpec extends Specification {
    static final File simpleTestSrcDir = new File(IntegrationTestHelper.PROJECTROOT,'build/resources/integrationTest/gradleTest')
    static final File gradleLocation  = IntegrationTestHelper.CURRENT_GRADLEHOME

    Project project = IntegrationTestHelper.buildProject('iis')
    String gradleVersion = project.gradle.gradleVersion

    File simpleTestDestDir = new File(project.projectDir,'src/' + Names.DEFAULT_TASK)
    File expectedOutputDir = new File(project.buildDir,Names.DEFAULT_TASK + '/' + project.gradle.gradleVersion )
    File initScript = new File(project.projectDir,'foo.gradle')
    File repoDir = new File(project.projectDir,'srcRepo')

    boolean exists( final String path ) {
        new File("${project.buildDir}/${Names.DEFAULT_TASK}/${path}" ).exists()
    }

    void setup() {
        assert  simpleTestSrcDir.exists()
        IntegrationTestHelper.createTestRepo(repoDir)

        FileUtils.copyDirectory simpleTestSrcDir, simpleTestDestDir

        project.repositories {
            flatDir {
                dirs repoDir
            }
        }

        project.allprojects {
            apply plugin: 'org.ysb33r.gradletest'

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
        exists 'init.gradle'
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
        runners[0].initScript == new File("${project.buildDir}/${Names.DEFAULT_TASK}/init.gradle")
        runners[0].version == gradleVersion
        runners[0].testName == 'simpleTest'
    }
}

/*

    Project project
    File gradleLocationDir
    File testProjectDir
    File initScript
    String version
    String testName

            tests.each { test ->
                testRunners+= new TestRunner(
                    project : project,
                    gradleLocationDir : locations[ver],
                    testProjectDir : new File(dest,test),
                    testName : test,
                    version : ver,
                    initScript : initGradle
                )
            }
        }

        project.copy {
            from project.configurations.getByName(name).files
            into repo
        }.assertNormalExitValue()

 */