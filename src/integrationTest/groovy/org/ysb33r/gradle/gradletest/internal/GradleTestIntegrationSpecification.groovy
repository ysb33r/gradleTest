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
package org.ysb33r.gradle.gradletest.internal

import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Before running anything from here, ensure that `gradle createIntegrationTestClasspathManifest` has been run.
 */
class GradleTestIntegrationSpecification extends Specification {

    static final File PLUGIN_METADATA_FILE = new File(System.getProperty('PLUGIN_METADATA_FILE') ?: 'build/integrationTest/manifest/plugin-classpath.txt')
    static final List AVAILABLE_GRADLE_VERSIONS = ['3.0', '3.1', '3.5', '4.0.2', '4.3.1']

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    void setup() {
        buildFile = testProjectDir.newFile('build.gradle')

    }

    void writeBuildScriptHeader(final String version) {
        buildFile << """
            plugins {
                id 'org.ysb33r.gradletest'
            }
"""
    }


    List<File> readMetadataFile() {
        PLUGIN_METADATA_FILE.readLines().collect {
            new File(it)
        }
    }

}
