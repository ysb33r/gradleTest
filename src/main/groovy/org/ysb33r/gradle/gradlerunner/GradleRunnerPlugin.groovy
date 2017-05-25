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
package org.ysb33r.gradle.gradlerunner

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

/** Adds a default task called {@code gradleRunner}
 *
 * @since 1.0
 */
class GradleRunnerPlugin implements Plugin<Project> {
    static final String DEFAULT_TASK = 'gradleRunner'

    @Override
    void apply(Project project) {

        if (GradleVersion.current() < GradleVersion.version('2.14.1')) {
            throw new GradleException('This plugin requires a minimum version of 2.14.1')
        }
        
        project.tasks.create DEFAULT_TASK, GradleRunnerSteps
    }
}
