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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.util.CollectionUtils
import org.ysb33r.gradle.gradletest.Distribution
import org.ysb33r.gradle.gradletest.Names

/** The downloader is a background task which will download Gradle distributions to a local
 * folder in the build directory.
 *
 *  This task is only enabled if the gradle job was not run offline.
 * In the current implementation it will only look for 'bin' variants.
 *
 * @author Schalk W. Cronjé
 */
class GradleTestDownloader extends DefaultTask {

    GradleTestDownloader() {
        enabled = !project.gradle.startParameter.offline
        group = Names.TASK_GROUP
        description = 'Downloads gradle distributions if needed'
    }

    /** Get the list of versions that needs to be downloaded.
     *
     * @return Set of version strings.
     */
    @Input
    Set<String> getVersions() {
        this.versions
    }

    /** Get the output directory.
     * The default is {@code ${buildDir}/gradleDist
     */

    @OutputDirectory
    File getOutputDir() {
        this.outputDir
    }

    // TODO: Decide whether this is needed. Probably not in the first version though.
//    /** Overrides te output directory
//     *
//     * @param dir
//     */
//    void setOutputDir(Object dir) {
//        this.outputDir=project.file(dir)
//    }
//
//    /** Overrides te output directory
//     *
//     * @param dir
//     */
//    void outputDir(Object dir) {
//        setOutputDir(dir)
//    }

    /** Adds one or more versions that need to be downloaded.
     *
     * @param v List of version that needs downloading.
     */
    void versions(Object... v) {
        versions+= CollectionUtils.stringize(v as List)
    }

    /** Adds one or more versions that need to be downloaded.
     *
     * @param v List of version that needs downloading.
     */
    void versions(Iterable<Object> v) {
        versions += CollectionUtils.stringize(v as List)
    }

    /** Returns the URIs this downloader will attempt to use
     *
     */
    @SkipWhenEmpty
    @Input
    Set<URI> getUris() {
        Set<URI> uriSet = []
        if(project.extensions.getByName(Names.EXTENSION).useGradleSite) {
            uriSet += Names.GRADLE_SITE
        }
        uriSet.addAll(project.extensions.getByName(Names.EXTENSION).uris)
        uriSet
    }

    /** Returns a set of downloaded distributions and their locations. This only becomes set after execution
     *
     */
    Set<Distribution> getDownloaded() {
        downloaded
    }

    /** Downloads any necessary distributions and unpacks them.
     * Any exceptions that occur during downloading will stop downloading of other distributions
     *
     *  @throw StopActionException if a requested version has not been found
     */
    @TaskAction
    void exec() {
        Set<URI> uriSet = uris
        if(!uris.empty) {
            outputDir.mkdirs()
            versions.each { String version ->

                File target = Unpacker.downloadFileFrom(uriSet,project.gradle,outputDir,version,'bin')
                if(target == null) {
                    throw new TaskExecutionException(this,new StopActionException("Could not find Gradle ${version}"))
                }
                Unpacker.unpackTo(outputDir,target,logger)
                if(project.extensions.getByName(Names.EXTENSION).downloadToGradleUserHome) {
                    Unpacker.unpackToUserHome(project.gradle,target)
                }

            }

            downloaded = DistributionInternal.searchCacheFolder(outputDir,logger)
        }
    }

    private Set<String> versions = []
    private File outputDir = new File(project.buildDir,Names.DOWNLOAD_FOLDER)
    private List<Distribution> downloaded = null

    /** Updates the versions for a given downloader task. Intended to be called in
     * {@code afterEvaluate} phase.
     *
     * @param task Downloader tasks to be updated
     * @param useTheseVersions List of versions to be downloaded.
     */
    static void updateVersions( GradleTestDownloader task,Iterable<String> useTheseVersions) {
        task.versions useTheseVersions
    }
}
