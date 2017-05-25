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
package org.ysb33r.gradle.gradletest.legacy20.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import groovy.transform.CompileDynamic
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.ysb33r.gradle.gradletest.legacy20.GradleTestExtension
import org.ysb33r.gradle.gradletest.legacy20.Distribution
import org.ysb33r.gradle.gradletest.Names

import java.util.regex.Matcher

/**
 * Locates distributions on the file system.
 */
@TupleConstructor
@Sortable(excludes=['location'])
@EqualsAndHashCode(excludes=['location'])
@ToString
class DistributionInternal implements Distribution {

    int compareTo(Object o) {
        compareTo(o as DistributionInternal)
    }

    String version
    File location

    static DistributionInternal getCurrentGradle(Gradle gradle) {
        gradle.rootProject.logger.debug "Found gradle version ${gradle.gradleVersion} in ${gradle.gradleHomeDir}"
        new DistributionInternal(gradle.gradleVersion, gradle.gradleHomeDir)
    }

    /** Search a folder that has a Gradle cache layout.
     *
     * @param folder
     * @param logger
     * @return
     */
    @CompileDynamic
    static List<DistributionInternal> searchCacheFolder(final File folder,Logger logger) {
        List<DistributionInternal> wrapper = []
        File searchFolder = new File(folder,'wrapper/dists').absoluteFile
        if(searchFolder.exists()) {
            searchFolder.eachDir { File topDir ->
                // topDir is a folder potentially containing folders which
                // in themselves might contain a folder with an unpacked distribution
                topDir.eachDir { File intermediateDir ->
                    logger.debug "Searching ${intermediateDir} for gradle distributions"
                    intermediateDir.eachDirMatch Names.GRADLE_PATTERN, { File gradleDir ->
                        Matcher matcher = gradleDir.name =~ Names.GRADLE_PATTERN
                        if (matcher.matches()) {
                            logger.debug "Found gradle (via cache) version ${matcher[0][1]} in ${gradleDir}"
                            wrapper += new DistributionInternal(matcher[0][1], gradleDir)
                        }
                    }
                }
            }
        }
        wrapper
    }

    /** Search a folder where various gradle distributions might have been unpacked into
     *
     * @param searchFolder
     * @param logger
     * @return
     */
    static searchInstallFolder(final File searchFolder,Logger logger) {
        List<DistributionInternal> gvm = []
        logger.debug "Searching ${searchFolder} for gradle distributions"
        if(searchFolder.exists()) {
            searchFolder.eachDir { File dir ->
                if (dir.name != 'current') {
                    if (new File(dir, 'bin/gradle').exists()) {
                        logger.debug "Found gradle (via install) version ${dir.name} in ${dir}"
                        String version = dir.name
                        if (version.startsWith('gradle-')) {
                            version = version.replace('gradle-', '')
                        }
                        gvm += new DistributionInternal(version, dir)
                    }
                }
            }
        }
        gvm
    }

    /** Searches {@code GRADLE_USER_HOME} folder
     *
     * @param gradle
     * @return
     */
    static List<DistributionInternal> searchGradleHome(Gradle gradle) {
        searchCacheFolder(gradle.gradleUserHomeDir,gradle.rootProject.logger)
    }

    /** Search GVM folder
     *
     * @param logger
     * @return
     */
    static List<DistributionInternal> searchGvm(Logger logger) {
        File searchFolder = new File(System.getProperty('user.home') + '/.gvm/gradle').absoluteFile
        searchInstallFolder(searchFolder,logger)
    }

    /** Searches locally for unpacked distributions
     *
     * @param project The project this search is attached to.
     * @param config The search configuration
     * @return
     */
    static Set<DistributionInternal> searchLocal(Project project,GradleTestExtension config) {
        List<DistributionInternal> all = []

        if (config.includeGradleHome) {
            all += DistributionInternal.getCurrentGradle(project.gradle)
        }

        if (config.searchGradleUserHome) {
            all += DistributionInternal.searchGradleHome(project.gradle)
        }

        if (config.searchGvm) {
            all += DistributionInternal.searchGvm(project.logger)
        }

        config.searchFolders.each { File folder ->
            project.logger.debug "Search user supplied folder '${folder}'"
            all+= DistributionInternal.searchCacheFolder(folder,project.logger)
            all+= DistributionInternal.searchInstallFolder(folder,project.logger)
        }

        all as Set
    }


}

