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
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.wrapper.Download
import org.gradle.wrapper.IDownload
import org.gradle.wrapper.Install
import org.gradle.wrapper.PathAssembler
import org.gradle.wrapper.WrapperConfiguration
import org.ysb33r.gradle.gradletest.Names

/** Unpacks a downloaded distributions to the gradle wrapper in {@code gradleUserHome}
 *
 * @author Schalk W. Cronjé
 */
class Unpacker {

    /** Creates a downloader to download a a distrbution to a specific location
     *
     * @param logger Logger to use. (Older versions of Wrapper API will ignore this).
     * @return An instance of a downloader
     */
    static IDownload createDownloader(Logger logger) {
        try {
            new Download(logger,'GradleTestPlugin','1.0')
        } catch(GroovyRuntimeException e) {
            new Download('GradleTestPlugin','1.0')
        }
    }

    /** Downloads a remote Gradle distribution to local folder, but does not unpack it.
     *
     * @param gradle Gradle instance to use
     * @param rootURI Root URI of remote site
     * @param downloadLocation Location to download to
     * @param version Version to download
     * @param variant Variant to download ('bin' or 'all')
     */
    static void downloadTo( Gradle gradle, final URI rootURI, final File downloadLocation, final String version, final String variant ) {
        IDownload downloader= createDownloader(gradle.rootProject.logger)
        String downloadURI = "${rootURI}/gradle-${version}-${variant}.zip"
        downloader.download(downloadURI.toURI(),new File(downloadLocation,"gradle-${version}-${variant}.zip"))
    }

    /** Downloads a remote Gradle distribution from the Gradle distribution site to local folder, but does not unpack it
     *
     * @param gradle Gradle instance
     * @param downloadLocation Location to download to
     * @param version Version to download
     * @param variant Variant to download ('bin' or 'all')
     */
    static void downloadTo( Gradle gradle, final File downloadLocation, final String version, final String variant ) {
        downloadTo(gradle, Names.GRADLE_SITE,downloadLocation,version,variant)
    }

    /** Unpacks a Gradle Distribution to a given root directory.
     *
     * This method also variations in the Wrapper API so that hopefully all 2.x Gradle distributions
     * are covered
     *
     * @param unpackLocation Location to unpack to
     * @param downloadedLocation Location where distribution has been downloaded to.
     * @param logger Logger to use. (Older versions of Wrapper API will ignore this).
     */
    static void unpackTo( File unpackLocation, File downloadedLocation, Logger logger ) {

        PathAssembler pa = new PathAssembler(unpackLocation)
        Install installer
        try {
            installer = new Install(logger,createDownloader(logger),pa)
        } catch (GroovyRuntimeException e) {
            installer = new Install(createDownloader(logger),pa)
        }

        WrapperConfiguration config = new WrapperConfiguration()
        config.distribution = downloadedLocation.absoluteFile.toURI()

        installer.createDist(config)
    }

    /** Installs the distribution into the Gradle User Home Directory
     *
     * @param gradle Gradle instance to use
     * @param downloadedLocation Location where distribution has been downloaded to.
     */
    static void unpackToUserHome( Gradle gradle, File downloadedLocation ) {
        unpackTo(gradle.gradleUserHomeDir,downloadedLocation,gradle.rootProject.logger )
    }

    /** Iterates through a set of URIs attempting to download a distribution.
     *
     * @param uriSet Iterable set of URIs
     * @param Gradle instance to use
     * @param outputDir Directory where to download to
     * @param version Version to download
     * @param variant Variant to download
     *
     * @return Returns the file if the disribution already exists or it has been downloaded successfully, otherwise null.
     */
    @CompileStatic
    static File downloadFileFrom(Iterable<URI> uriSet,Gradle gradle, final File outputDir, final String version,final String variant) {
        File target = new File( outputDir, "gradle-${version}-${variant}.zip" )
        if(!target.exists()) {
            uriSet.each { uri ->
                if (!target.exists()) {
                    try {
                        Unpacker.downloadTo(gradle, uri, outputDir, version, variant)
                    } catch (FileNotFoundException) {
                        // Expected exception in case of URI not existent
                    }
                }
            }
            return target.exists() ? target : null
        } else {
            return target
        }
    }

}
