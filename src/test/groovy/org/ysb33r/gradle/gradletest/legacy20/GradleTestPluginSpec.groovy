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

import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.legacy20.internal.GradleTestDownloader
import org.ysb33r.gradle.gradletest.internal.GradleTestSpecification
import spock.lang.Specification

/**
 * @author Schalk W. Cronjé
 */
class GradleTestPluginSpec extends GradleTestSpecification {

    def "Applying the plugin"() {
        given:
        project.with {
            apply plugin : LegacyGradleTestPlugin
        }

        expect:
        tasks.getByName(Names.DEFAULT_TASK) instanceof GradleTest
        extensions.getByName(Names.EXTENSION) instanceof GradleTestExtension
        configurations.findByName(Names.CONFIGURATION) != null
        configurations.findByName(Names.DEFAULT_TASK) != null
        tasks.getByName(Names.DOWNLOADER_TASK) instanceof GradleTestDownloader
        tasks.getByName(Names.CLEAN_DOWNLOADER_TASK) instanceof Delete
        tasks.getByName(Names.CLEAN_DOWNLOADER_TASK).description?.size()
    }

    def 'Effect of plugin on afterEvaluate'() {
        given:
        configure(project) {
            apply plugin : LegacyGradleTestPlugin

            gradleLocations {
                searchGvm = false
            }

            gradleTest {
                versions '1.999','1.998'
            }
            evaluate()
        }
        def defaultTask = tasks.getByName(Names.DEFAULT_TASK)
        def downloaderTask = tasks.getByName(Names.DOWNLOADER_TASK)

        expect:
        defaultTask.dependsOn.contains Names.DOWNLOADER_TASK
        downloaderTask.versions == ['1.999','1.998'] as Set<String>
    }
}

