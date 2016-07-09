/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2015 - 2016
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

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.WorkResult

/** Utiity class to aid in building the files structure for compatibility testing
 *
 *
 */
class Infrastructure {
    /** Creates the file layout. It takes a property map as aparaneter with the following required non-null-value properties:
     *
     *  @li name - Name of the the test task (usually {@code gradleTest}).
     *  @li project - THe project that these tests are being built for
     *  @li tests - A list of tests to be executed
     *  @li locations - A map containing gradle versions (as key) and their locations (as value).
     *  @li sourceDir - Source dreictory where tests are copied from
     *  @li templateFile - URI of the templateFile that will be used
     *  @li versions - A list of versions that the compatibility tests will be execurted against
     *
     * @param settings Property map
     * @return A list of TestRunner obkect which can be executed
     */
    static List<TestRunner> create( Map settings ) {

        final Project project = settings.project
        final List<String> tests = settings.tests
        final Map<String,File> locations = settings.locations
        final String name = settings.name
        final File src = settings.sourceDir
        final Object initScript = settings.initScript
        final Set<String> versions = settings.versions

        assert project != null
        assert tests != null
        assert locations != null
        assert name != null
        assert src != null
        assert initScript != null
        assert versions != null

        final File baseDir = new File(project.buildDir,name)
        final File initGradle = new File(baseDir,"init20.gradle" )
        final File repo = new File( baseDir,"repo" )
        final Logger logger = project.logger
        final File pluginDir = project.tasks.getByName('jar').destinationDir

        List<TestRunner> testRunners = []
        WorkResult wr

        if(initGradle.exists()) {
            logger.debug "Deleting '${initGradle}' as it exists."
            initGradle.delete()
        }

        logger.debug "Infrastructure: Copying templateFile from '${initScript}'"
        wr = project.copy {
            from initScript
            into initGradle.parentFile
            rename { initGradle.name }
            expand PLUGINDIR : pluginDir.absolutePath
        }

        assert wr.didWork

        versions.each { ver ->
            File dest = new File(baseDir,ver)
            logger.debug "Infrastructure: Copying project files from '${src}' to '${dest}'"
            wr = project.copy {
                from src, {
                    include '**'
                }
                into dest
                filter { line ->
                    line.replaceAll('%%GROUP%%',project.group).
                    replaceAll('%%MODULE%%',project.tasks.jar.baseName).
                    replaceAll('%%VERSION%%',project.version)
                }

            }
            assert wr.didWork

            tests.each { test ->
                testRunners+= new TestRunner(
                    project : project,
                    gradleLocationDir : locations[ver],
                    testProjectDir : new File(dest,test),
                    testName : test,
                    version : ver,
                    initScript : initGradle
                )
                logger.debug "Infrastructure: Created test runner for '${ver}:${test}'"
            }
        }

        logger.debug "Infrastructure: Copying ${project.configurations.getByName(name).files} to '${repo}'"
        project.copy {
            from project.configurations.getByName(name).files
            into repo
        }

        testRunners
    }
}
