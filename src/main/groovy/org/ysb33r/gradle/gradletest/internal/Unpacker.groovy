package org.ysb33r.gradle.gradletest.internal

import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.wrapper.Download
import org.gradle.wrapper.IDownload
import org.gradle.wrapper.Install
import org.gradle.wrapper.PathAssembler
import org.gradle.wrapper.WrapperConfiguration

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
    static void downloadTo( Gradle gradle, final String rootURI, final File downloadLocation, final String version, final String variant ) {
        IDownload downloader= createDownloader(gradle.rootProject.logger)
        String downloadURI = "${rootURI}/gradle-${version}-${variant}.zip"
        downloader.download(downloadURI.toURI(),downloadLocation)
    }

    /** Downloads a remote Gradle distribution from the Gradle distribution site to local folder, but does not unpack it
     *
     * @param gradle Gradle instance
     * @param downloadLocation Location to download to
     * @param version Version to download
     * @param variant Variant to download ('bin' or 'all')
     */
    static void downloadTo( Gradle gradle, final File downloadLocation, final String version, final String variant ) {
        downloadTo(gradle,'https://services.gradle.org/distributions',downloadLocation,version,variant)
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
}
