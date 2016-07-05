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

import org.ysb33r.gradle.gradletest.internal.GradleTestSpecification
import spock.lang.Specification


class ClasspathManifestSpec extends GradleTestSpecification {

    static final File GRADLETESTREPO = new File(System.getProperty('GRADLETESTREPO') ?: 'build/integrationTest/repo').absoluteFile

    def "Creating a plugin classpath manifest"() {
        given: "A configured GradleTest plugin"
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'

            repositories {
                flatDir {
                    dirs GRADLETESTREPO
                }
            }

            gradleTest {
                versions '1.999','1.998'
            }
            evaluate()
        }

        when: "The manifest task is executed"
        ClasspathManifest manifestTask = tasks.getByName('gradleTestClasspathManifest')
        File outputFile = new File(manifestTask.outputDir,manifestTask.outputFilename)
        manifestTask.execute()

        then: "The manifest file is generated"
        outputFile.exists()

        when: "The manifest is inspected"
        String text = outputFile.text

        then: "It will contain the directory with the test files"
        text.contains(project.sourceSets.gradleTest.output.classesDir.toString())

        and: "It will contain the Spock Framework jar path"
        text.contains("spock-core-${TestSet.SPOCK_VERSION}.jar")

        and: "It will contain the Apache Commons IO jar path"
        text.contains("commons-io-${TestSet.COMMONS_IO_VERSION}.jar")

        and: "It will contain the JUNit jar path"
        text.contains("junit-${TestSet.JUNIT_VERSION}.jar")
    }
}