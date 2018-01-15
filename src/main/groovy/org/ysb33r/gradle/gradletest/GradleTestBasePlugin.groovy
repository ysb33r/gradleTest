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
package org.ysb33r.gradle.gradletest

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.ysb33r.gradle.gradletest.internal.GradleVersions.EARLIER_THAN_GRADLE_3_0

/** A base plugin for GradleTest. It provides only the ability to add new test sets,
 * but does not create any.
 *
 */
@CompileStatic
class GradleTestBasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (EARLIER_THAN_GRADLE_3_0) {
            throw new GradleException('This plugin is only compatible with Gradle 3.0+')
        }
        project.apply plugin: 'groovy'
        addTestKitTriggers(project)
    }

    /** Adds the appropriate task triggers.
     */
    @CompileDynamic
    private void addTestKitTriggers(Project project) {
        project.ext {
            additionalGradleTestSet = { String name ->
                TestSet.addTestSet(project, name)
            }
        }
    }
}
