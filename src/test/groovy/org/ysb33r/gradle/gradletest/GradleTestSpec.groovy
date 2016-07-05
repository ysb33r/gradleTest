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

import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.internal.GradleTestSpecification
import spock.lang.Specification


class GradleTestSpec extends GradleTestSpecification {

    def 'Effect of plugin on afterEvaluate'() {
        when: "Versions are configured "
        project.with {
            apply plugin : 'org.ysb33r.gradletest'

            gradleTest {
                versions '1.999','1.998'
                versions '1.997'


            }
            evaluate()
        }
        GradleTest defaultTestTask    = project.tasks.getByName('gradleTest')
        def defaultGenTask     = project.tasks.getByName('gradleTestGenerator')

        then: "The test task will contain a list of these versions"
        defaultTestTask.versions == ['1.999','1.998','1.997'] as Set<String>

        and: "The generator task will contain a list of these versions"
        defaultGenTask.versions == ['1.999','1.998','1.997'] as Set<String>

        and: "Distribution URI will be null (indicating default behaviour)"
        defaultTestTask.gradleDistributionUri == null

        and: 'Default task is runGradleTest'
        defaultTestTask.defaultTask == 'runGradleTest'
    }

    def "Adding additional arguments"() {
        when: "Arguments are configured "
        project.with {
            apply plugin : 'org.ysb33r.gradletest'

            gradleTest {
                versions '1.997'
                gradleArguments '--max-workers',4
            }
            evaluate()
        }
        GradleTest defaultTestTask    = project.tasks.getByName('gradleTest')

        then: "The arguments will also contain the extras"
        defaultTestTask.gradleArguments.containsAll '4','--max-workers'

        and: "As well as the required arguments"
        defaultTestTask.gradleArguments.containsAll '--init-script'
    }

    def 'Accept Distribution URI as String'() {
        when: "Distribution URI is configured"
        project.with {
            apply plugin: 'org.ysb33r.gradletest'

            gradleTest {
                gradleDistributionUri 'file:///foo/bar'
            }
            evaluate()
        }

        GradleTest defaultTestTask = project.tasks.getByName('gradleTest')

        then:
        defaultTestTask.gradleDistributionUri == 'file:///foo/bar'.toURI()
    }

    def 'Accept Distribution URI as File'() {
        when: "Distribution URI is configured"
        project.with {
            apply plugin: 'org.ysb33r.gradletest'

            gradleTest {
                gradleDistributionUri file('foo')
            }
            evaluate()
        }

        GradleTest defaultTestTask = project.tasks.getByName('gradleTest')

        then:
        defaultTestTask.gradleDistributionUri == file('foo').toURI()
    }

}

