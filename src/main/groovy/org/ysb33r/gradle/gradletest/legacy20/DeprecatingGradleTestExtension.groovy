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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.Names

/** This is used under 2.13+ to warn the user that the gradleLocations extension is no longer in use.
 *
 * @author Schalk W. Cronjé
 * @since 1.0
 */
@CompileStatic
class DeprecatingGradleTestExtension {

    DeprecatingGradleTestExtension(Project project) {
        this.project = project
    }

    @CompileDynamic
    def methodMissing(String name,args) {

        String fieldName = PROPLIST.find {
            name.endsWith(it.capitalize())
        }

        if(fieldName != null) {
            if(name.startsWith('set') && args.size() == 1) {
                logDeprecation "'${name}' is a NOOP"
                return null
            } else if(name.startsWith('get') && args.size() == 0) {
                logDeprecation "'${name}' will return false"
                return false
            }
        } else {

        }

        throw new MissingMethodException(name,this.class,args)
    }

    List<URI> getUris() {
        logDeprecation "'getUris' will be empty"
        []
    }

    void uri(Object... u) {
        logDeprecation "'uri' is a NOOP"
    }

    Set<File> getSearchFolders() {
        logDeprecation "'ggetSearchFolders' will be empty"
        [] as Set<File>
    }

    void search(Object... files) {
        logDeprecation "'search' is a NOOP"
    }

    private void logDeprecation(final String msg) {
        project.logger.warn "Since Gradle 2.13 the extension ${Names.EXTENSION} is no longer in use. " +
            "It will be removed in a future release. ${msg}."
    }

    private Project project

    private static final List<String> PROPLIST = [
        'download','includeGradleHome','searchGvm','searchGradleUserHome',
        'downloadToGradleUserHome','useGradleSite'
    ]
}
