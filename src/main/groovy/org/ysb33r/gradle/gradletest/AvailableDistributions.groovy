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

/**
 * @author Schalk W. Cronjé
 */
interface AvailableDistributions {

//    /** Gets a list of versions that are available locally
//     *
//     * @return List of versions
//     */
//    Set<String> getLocalVersions()

    /** Returns all distributions that either found locally or downloaded and unpacked
     *
     * @return A map with the version as key and the location as value.
     */
    Map<String,File> getAllDistributionsAsMap()

    /** Adds one or more distribution to the collection
     *
     * @param dist Distribution to be added
     */
    void addDistributions( Distribution... dist )

    /** Adds one or more distribution to the collection
     *
     * @param dist Distribution to be added
     */
    void addDistributions( Iterable<Distribution> dist )

    /** Returns the location for a specific version.
     *
     * @param version Version that is required
     * @return Location (or null if version is not available)
     */
    File location( String version )


}