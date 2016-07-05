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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskState
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.legacy20.internal.AvailableDistributionsInternal
import org.ysb33r.gradle.gradletest.legacy20.internal.DistributionInternal
import org.ysb33r.gradle.gradletest.legacy20.internal.GradleTestDownloader

/** Legacy GradleTest plugin which works with Gradle 2.0-2.12.
 *
 */
@CompileStatic
class LegacyGradleTestPlugin implements Plugin<Project> {

    @CompileDynamic
    void apply(Project project) {
        project.with {

            // Force the JAVA plugin to be applied. You need at least it to build
            // a plugin anyway
            apply plugin : 'java'

            configurations.maybeCreate  Names.CONFIGURATION
            configurations.maybeCreate  Names.DEFAULT_TASK

            def gradleExt = extensions.create Names.EXTENSION, GradleTestExtension, project
            def downloader = tasks.create Names.DOWNLOADER_TASK, GradleTestDownloader
            tasks.create Names.DEFAULT_TASK, org.ysb33r.gradle.gradletest.legacy20.GradleTest

            def cleaner = tasks.create Names.CLEAN_DOWNLOADER_TASK, Delete
            cleaner.description = 'Remove downloaded distributions from build directory'

            // When downloader finished we want the extension to have the new distributions added
            gradle.addListener( createUpdateListener() )

            afterEvaluate { Project p ->

                // Get list of required versions
                Set<String> versions = GradleTest.findAllRequiredVersions(p)

                // Add a dependency for all Gradle tasks on the downloader tasks
                // Add a configuration on the jar task, so that it's output becomes
                // part of the gradleTask configuration
                tasks.withType(GradleTest) { t ->
                    t.dependsOn Names.DOWNLOADER_TASK
                    p.dependencies.add(t.name,p.tasks.jar.outputs.files)
                }

                // Search for local distributions
                Set<Distribution> dists = DistributionInternal.searchLocal(p,gradleExt) as Set
                Set<String> hasVersions = dists.collect { it.version } as Set<String>

                GradleTestDownloader.updateVersions(downloader,versions-hasVersions)

                // Adds the local distributions to available distributions
                if(gradleExt.distributions == null) {
                    gradleExt.distributions = new AvailableDistributionsInternal()
                }

                gradleExt.distributions.addDistributions dists

                tasks.getByName(Names.CLEAN_DOWNLOADER_TASK).configure {
                    description
                    delete tasks.getByName(Names.DOWNLOADER_TASK).outputDir
                }
            }
        }
    }

    /** Creates a a listener which will update the Gradle Distribution Extension with the location of
     * downloaded (and unpacked) distributions
     *
     * @return An instance of a {@code TaskExecutionListeneer}
     */
    private static TaskExecutionListener createUpdateListener() {
        new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {}

            @Override
            void afterExecute(Task task, TaskState state) {
                if(task instanceof GradleTestDownloader && state.didWork && !state.skipped) {
                    GradleTestExtension ext = task.project.extensions.getByName(Names.EXTENSION) as GradleTestExtension
                    GradleTestDownloader downloader = task as GradleTestDownloader
                    if(ext.distributions == null) {
                        ext.distributions = new AvailableDistributionsInternal()
                    }
                    (ext.distributions as AvailableDistributionsInternal).addDistributions downloader.downloaded
                }
            }
        }
    }
}
