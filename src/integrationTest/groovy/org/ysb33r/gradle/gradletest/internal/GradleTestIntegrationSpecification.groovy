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

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class GradleTestIntegrationSpecification extends Specification {

    static final File PLUGIN_METADATA_FILE = new File( System.getProperty('PLUGIN_METADATA_FILE') ?: 'build/pluginUnderTestMetadata/plugin-under-test-metadata.properties')
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    void setup() {
        buildFile = testProjectDir.newFile('build.gradle')

    }

    void writeBuildScriptHeader(final String version) {
        GradleVersion current = GradleVersion.version(version)
        if (current < GradleVersion.version('2.8')) {
            String uriList = readMetadataFile().collect {
                "'" + it.absolutePath.replace('\\','\\\\') + "'"
            }.join(',')
            buildFile << """
            buildscript {
                dependencies {
                    classpath files(${uriList})
                }
            }

            apply plugin : 'org.ysb33r.gradletest'
"""
        } else {
            buildFile << """
            plugins {
                id 'org.ysb33r.gradletest'
            }
"""

        }

    }
    // reads a TestKit metadata file
    List<File> readMetadataFile() {
        Properties props = new Properties()
        PLUGIN_METADATA_FILE.withReader { r ->
            props.load(r)
        }
        props.getProperty('implementation-classpath').split(File.pathSeparator).collect { String it ->
            new File(it)
        }
    }

}
