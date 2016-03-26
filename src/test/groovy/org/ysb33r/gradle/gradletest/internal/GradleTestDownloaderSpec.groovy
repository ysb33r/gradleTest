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

import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.GradleTestExtension
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.internal.GradleTestDownloader
import org.ysb33r.gradle.gradletest.internal.TestHelper
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class GradleTestDownloaderSpec extends Specification {

    Project project = TestHelper.buildProject('gtds')

    def "Default group and description"() {
        given:
        def downloader = project.tasks.create 'Foo', GradleTestDownloader

        expect:
        downloader.group == Names.TASK_GROUP
        downloader.description == 'Downloads gradle distributions if needed'

    }
    def "Default download directory"() {
        given:
        def downloader = project.tasks.create 'Foo', GradleTestDownloader

        expect:
        downloader.outputDir == new File(project.buildDir,Names.DOWNLOAD_FOLDER)

        and: "No items have been downloaded"
        downloader.downloaded == [] as Set
    }

    def "An instantiated task must be able to set basic parameters"() {
        given:
        def downloader = project.tasks.create 'Foo', GradleTestDownloader

        when:
        downloader.versions '2.1','2.2'
        downloader.versions '2.1','2.4'

        then:
        downloader.versions == ['2.1','2.2','2.4'] as Set
    }

    def "Default URIs should only be the Gradle Site (if set)"() {
        given:
        project.extensions.create Names.EXTENSION, GradleTestExtension, project
        def downloader = project.tasks.create 'Foo', GradleTestDownloader

        when:
        downloader.versions '2.1','2.2'

        then:
        downloader.uris == [Names.GRADLE_SITE] as Set<URI>

    }

    def "Default URIs should be empty if only no extra URIs and Gradle Site is disabled"() {
        given:
        def ext = project.extensions.create Names.EXTENSION, GradleTestExtension, project
        def downloader = project.tasks.create 'Foo', GradleTestDownloader

        when:
        ext.useGradleSite = false
        downloader.versions '2.1','2.2'

        then:
        downloader.uris.isEmpty()

    }

    def "Check enabled state against offline"() {
        given:
        def downloader = project.tasks.create 'Foo', GradleTestDownloader

        expect:
        downloader.enabled == !project.gradle.startParameter.offline

    }
}