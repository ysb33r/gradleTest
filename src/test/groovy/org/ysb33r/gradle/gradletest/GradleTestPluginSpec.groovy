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

import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.GroovyCompile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ysb33r.gradle.gradletest.legacy20.DeprecatingGradleTestExtension
import org.ysb33r.gradle.gradletest.internal.GradleTestSpecification
import spock.lang.Specification


class GradleTestPluginSpec extends GradleTestSpecification {

    def "Applying the plugin"() {
        when: "The plugin is applied"
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'
            additionalGradleTestSet 'foo'
            evaluate()
        }

        then: "The test task is created"
        tasks.getByName('gradleTest') instanceof GradleTest

        and: "The generator task is created"
        tasks.getByName('gradleTestGenerator') instanceof TestGenerator

        and: "A compile task is created"
        tasks.getByName('compileGradleTestGroovy') instanceof GroovyCompile

        and: "A compile configuration is added"
        configurations.findByName('gradleTestCompile') != null

        and: "A compile task depends on generator task"
        tasks.getByName('compileGradleTestGroovy').dependsOn.contains 'gradleTestGenerator'

        and: "A runtime configuration is added"
        configurations.findByName('gradleTestRuntime') != null

        and: "A (deprecated) extension is added"
        extensions.getByName(Names.EXTENSION) instanceof DeprecatingGradleTestExtension

        and: "Appropriate configurations & tasks are added for the additional test sets"
        tasks.getByName('fooGradleTest') instanceof GradleTest
        tasks.getByName('fooGradleTestGenerator') instanceof TestGenerator
        configurations.findByName('fooGradleTestCompile') != null
        tasks.getByName('compileFooGradleTestGroovy').dependsOn.contains 'fooGradleTestGenerator'

        when: "The sourceset is evaluated"
        SourceSet sourceSet = project.sourceSets.getByName('gradleTest')
        SourceDirectorySet sourceDirSet = sourceSet.getGroovy()

        then: "A test sourceset containing a single source directory is created"
        sourceDirSet.srcDirs.size() == 1
        sourceDirSet.srcDirs[0].canonicalPath == file("${buildDir}/gradleTest/src").canonicalPath

    }

}

