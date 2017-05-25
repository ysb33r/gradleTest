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
package org.ysb33r.gradle.gradletest.legacy20

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.legacy20.internal.AvailableDistributionsInternal

/** Extension for global configuration of handling distributions used for
 * testing.
 *
 *
 */
@CompileStatic
class GradleTestExtension {

    GradleTestExtension(Project p) {
        project=p
    }

    /** Consider the current Gradle distribution the script runs under
     * when searching for distributions. Default is {@code true}.
     */
    boolean includeGradleHome = true

    /** Consider the distributions {@code gradle.gradleUserHomeDir}
     * when searching for distributions. Default is {@code true}.
     */
    boolean searchGradleUserHome = true

    /** Search the GVM installation folder for Gradle distributions.
     *  Default is {@code true}.
     */
    boolean searchGvm = true

    /** If required distributions are not available locally, download them.
     * Default is {@code true}.
     *
     * Note that if grdle is run with the {@code --offline} switch then downloads
     * will not be performed.
     */
    boolean download = true

    /** When downloading distributions store them in {@code gradle.gradleUserHomeDir}.
     * This might save time in future working with multiple distributions or even re-running
     * the build again. It does however, mean that your build script affects user configuration,
     * which is not necessarily in the Gradle spirit.
     *
     * Default is {@code false}.
     *
     */
    boolean downloadToGradleUserHome = false

    /** Allow downloading from the global Gradle distribution site.
     * Default is {@code true}.
     */
    boolean useGradleSite = true

    /** Returns a list of URIs which should be tried to download Gradle distributions from
     *
     * @return
     */
    List<URI> getUris() {
        distributionURIs
    }

    /** Provide one or more URIs to be tried for downloading Gradle distributions.
     *
     * @param u One or more objects convertible to URIs
     */
    void uri(Object... u) {
        URI evaluatedURI
        u.each ({ List<URI> listURIs,Object it ->
            switch(it) {
                case URI:
                    evaluatedURI= (URI)it
                    break
                case File:
                    evaluatedURI= ((File)it).toURI()
                    break
                case String:
                default:
                    evaluatedURI= it.toString().toURI()
            }
            listURIs.add(evaluatedURI)
        }.curry(distributionURIs))
    }

    /** A list of additional folders to search. These will be search in both {@code gradleUserHome} and
     * {@code GVM_HOME/gradle} style.
     */
    Set<File> getSearchFolders() {
        this.searchFolders
    }

    /** Adds a list of file paths that can be searched
     *
     * @param files One or more objects convertible to File objects using {@code project.files}
     */
    void search(Object... files) {
        this.searchFolders.addAll(project.files(files).files)
    }

    /** Manages the set of available distributions.
     * If {@code null}, default until such time that its gets set during the
     * evaluation phase.
     */
    AvailableDistributions distributions = null

    private Project project
    private List<URI> distributionURIs = []
    private Set<File> searchFolders = []

    static void addAvailableDistributions( GradleTestExtension ext ) {
        ext.distributions = new AvailableDistributionsInternal()
    }
}
