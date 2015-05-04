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

import java.util.regex.Pattern

/**
 * @author Schalk W. Cronjé
 */
class Names {

    /** The name of the default task that will be created by the plugin
     *
     */
    static final String DEFAULT_TASK    = 'gradleTest'

    /** The name of the extension that is to used to configure location and
     * downloadability of Gradle distributions
     */
    static final String EXTENSION       = 'gradleLocations'

    /** A configuration for managing downloading of Gradle distributions
     *
     */
    static final String CONFIGURATION   = 'gradleDistributions'

    /** Name of task that is responsible for downloading Gradle distributions
     *
     */
    static final String DOWNLOADER_TASK = 'gradleDistributionDownloader'

    /** Name of tasks that is reponsible for cleaning downloaded distributions from the
     * build tree
     */
    static final String CLEAN_DOWNLOADER_TASK = 'cleanGradleDistributionDownloader'

    /** A regex for matchign Gradle distribution packages
     *
     */
    static final Pattern GRADLE_PATTERN = ~/gradle-(.+)(-(bin|all))?/

    /** Subdirectory where distributions are downloaded to and unpacked if necessary
     *
     */
    static final String DOWNLOAD_FOLDER = 'gradleDist'

    /** Default group for all tasks
     *
     */
    static final String TASK_GROUP = 'Gradle Compatibility Testing'

    /** URI for Gradle distributions
     *
     */
    static final URI GRADLE_SITE = 'https://services.gradle.org/distributions'.toURI()
}
