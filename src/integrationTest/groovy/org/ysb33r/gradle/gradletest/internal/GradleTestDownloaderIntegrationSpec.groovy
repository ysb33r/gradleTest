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

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.GradleTestExtension
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.internal.GradleTestDownloader
import org.ysb33r.gradle.gradletest.internal.IntegrationTestHelper
import spock.lang.IgnoreIf
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class GradleTestDownloaderIntegrationSpec extends Specification {

    Project project = IntegrationTestHelper.buildProject('gtdis')
    def ext = project.extensions.create Names.EXTENSION,GradleTestExtension,project
    def downloader = project.tasks.create Names.DOWNLOADER_TASK,GradleTestDownloader

    void setup() {
        // Don't pollute Gradle User Home, just assume that it works to download there
        ext.downloadToGradleUserHome = false
    }

    @IgnoreIf({IntegrationTestHelper.OFFLINE})
    def "Download Gradle distributions from Gradle Site"() {
        given:
        downloader.versions '2.1','2.2'

        when:
        downloader.execute()

        then:
        new File(downloader.outputDir,'gradle-2.1-bin.zip').exists()
        new File(downloader.outputDir,'gradle-2.2-bin.zip').exists()
    }

    @IgnoreIf({IntegrationTestHelper.OFFLINE})
    def "Download non-existant distribution"() {
        given:
        downloader.versions '1.999'

        when:
        downloader.execute()

        then:
        thrown(GradleException)
    }
}
