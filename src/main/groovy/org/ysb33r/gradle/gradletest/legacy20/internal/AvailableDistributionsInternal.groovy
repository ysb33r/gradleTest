/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2015 - 2016
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

import groovy.transform.CompileStatic
import org.ysb33r.gradle.gradletest.legacy20.AvailableDistributions
import org.ysb33r.gradle.gradletest.legacy20.Distribution

/** Implementation to report local available distributions
 *
 */
@CompileStatic
class AvailableDistributionsInternal implements AvailableDistributions {

    /** Returns all distributions that either found locally or downloaded and unpacked
     *
     * @return A map with the version as key and the location as value.
     */
    @Override
    Map<String, File> getAllDistributionsAsMap() {
        distributions
    }

    /** Adds one or more distribution to the collection
     *
     * @param dist Distribution to be added
     */
    @Override
    void addDistributions(Distribution... dist) {
        addDistributions dist as List
    }

    /** Adds one or more distribution to the collection
     * If versions already exist in the list they are not updated. (First add wins)
     *
     * @param dist Distribution to be added
     */
    @Override
    void addDistributions(Iterable<Distribution> dists) {
        dists.each { Distribution dist ->
            if(!distributions.containsKey(dist.version))    {
                distributions[ "${dist.version}".toString() ] = dist.location
            }
        }
    }

    /** Returns the location for a specific version.
     *
     * @param version Version that is required
     * @return Location (or null if version is not available)
     */
    @Override
    File location(final String version) {
        distributions[version]
    }

    private final Map<String,File> distributions = [:]

}

