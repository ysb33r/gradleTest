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

import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.internal.GradleTestSpecification
import spock.lang.Specification

class GradleTestSpec extends GradleTestSpecification {

    def "Unconfigured task"() {
        given:
        def task = project.tasks.create('Foo',GradleTest)

        expect:
        task.group == Names.TASK_GROUP
        task.description == "Runs all the compatibility tests for 'Foo'"
        task.versions.empty
        task.sourceDir == project.file("${project.projectDir}/src/Foo")
        task.outputDirs.empty

    }

    def "Basic Configured task"() {
        given:
        def task = project.tasks.create('Foo',GradleTest)
        def output = new File(project.buildDir,'Foo')

        task.versions '2.2','2.3'
        task.versions '2.4','2.4'

        expect:
        task.versions == (['2.2','2.3','2.4'] as Set<String>)
        task.outputDirs == [ new File(output,'2.2'),new File(output,'2.3'),new File(output,'2.4')] as Set

    }
}

