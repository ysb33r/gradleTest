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
package org.ysb33r.gradle.gradletest.legacy20

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.internal.GradleTestIntegrationSpecification
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Unroll

/**
 * This test is specifically to test compatibility with a slection of distributions of Gradle 2.0-2.12.
 * Distributions selected are typically on key feature change points in Gradle
 */
class LegacyGradleTestIntegrationSpec extends GradleTestIntegrationSpecification {
    static final File REPODIR = new File(System.getProperty('GRADLETESTREPO') ?: 'build/integrationTest/repo')
    static File simpleTestSrcDir = new File(System.getProperty('TESTPROJECTSRCDIR') ?: 'build/resources/integrationTest/gradleTest')
    File simpleTestDestDir

    void setup() {
        simpleTestDestDir = new File(testProjectDir.root,'src/'+Names.DEFAULT_TASK)
        simpleTestDestDir.mkdirs()
        assert  simpleTestSrcDir.exists()
    }


    @Unroll
    def "Gradle #version (<2.13): Simepl project that will pass"() {

        setup:
        copyTestDir('simpleTest')
        buildScript(version)

        when: 'Evaluation has been completed'
        println buildFile.text
        GradleVersion currentGradle = GradleVersion.version(version)
        BuildResult result = getGradleRunner(version).build()
        String output = result.output

        then:
        if(currentGradle >= GradleVersion.version('2.5')) {
            result.task(':gradleTest').outcome == TaskOutcome.SUCCESS
        }
        !output.contains(':gradleTest SKIPPED')
        output.contains(':gradleTest')

        where:
        version << ['2.1','2.5','2.8','2.9']
    }

//    @Issue('https://github.com/ysb33r/gradleTest/issues/30')
//    def "Run a gradletest with a custom initscript" () {
//        setup:
//        copyTestDir('simpleTest')
//        buildScript(version)
//        buildFile << """
//        gradleTest {
//            initscript file('${new File(simpleTestSrcDir,'../initScripts/gradleTest/initScriptTest/initscript.gradle').absolutePath}')
//        }
//"""
//
//        when: 'Evaluation has been completed'
//        println buildFile.text
//        GradleVersion currentGradle = GradleVersion.version(version)
//        BuildResult result = getGradleRunner(version).build()
//        String output = result.output
//
//        then:
//        if(currentGradle >= GradleVersion.version('2.5')) {
//            result.task(':gradleTest').outcome == TaskOutcome.SUCCESS
//        }
//        !output.contains(':gradleTest SKIPPED')
//        output.contains(':gradleTest')
//
//        where:
//        version << ['2.1','2.5','2.8','2.9']
//
//    }

    private void buildScript(final String version) {
        writeBuildScriptHeader(version)
        buildFile << """
            repositories {
                flatDir {
                    dirs '${REPODIR.toURI()}'
                }
            }

            gradleLocations {
                includeGradleHome = false
                searchGradleUserHome = false
                searchGvm = false
                download = false
                useGradleSite = false
                downloadToGradleUserHome = false
                uri '${REPODIR.absoluteFile.toURI()}'
            }

            dependencies {
                gradleTest ':commons-cli:1.2'
                gradleTest ':doxygen:0.2'
            }

            gradleTest {
                versions '${version}'
            }
"""
    }


    private void copyTestDir(final String name) {
        FileUtils.copyDirectory new File(simpleTestSrcDir,name), simpleTestDestDir
    }

    private GradleRunner getGradleRunner(final String version) {
        GradleVersion currentGradle = GradleVersion.version(version)
        URI distribution = new File(REPODIR,"gradle-${version}-bin.zip").absoluteFile.toURI()
        GradleRunner gradleRunner = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(
            'gradleTest',
            '--info',
            '-s',
            currentGradle > GradleVersion.version('2.1') ? '--console=plain' : '--no-color'
        )
            .withGradleDistribution(distribution)
        if(currentGradle > GradleVersion.version('2.5')) {
            gradleRunner.withPluginClasspath(readMetadataFile())
        }
        gradleRunner
    }

}

