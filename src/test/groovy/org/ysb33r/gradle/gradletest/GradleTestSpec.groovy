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


/**
 * @author Schalk W. Cronjé
 */
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
        def defaultTestTask    = project.tasks.getByName('gradleTest')
        def defaultGenTask     = project.tasks.getByName('gradleTestGenerator')

        then: "The test task will contain a list of these versions"
        defaultTestTask.versions == ['1.999','1.998','1.997'] as Set<String>

        and: "The generator task will contain a list of these versions"
        defaultGenTask.versions == ['1.999','1.998','1.997'] as Set<String>

        // TODO: arguments, defaultTask
    }

//    def "Compile"() {
//        given: "A project that contains a src/gradleTest layout"
//        def tests = ['alpha','beta','gamma','delta']
//        File srcDir = new File(projectDir,'src/gradleTest')
//        srcDir.mkdirs()
//        tests.each {
//            File testDir = new File(srcDir,it)
//            testDir.mkdirs()
//            if(it != 'delta') {
//                new File(testDir,'build.gradle').text = ''
//            }
//        }
//
//    }
//    def "Unconfigured task"() {
//        given:
//        def task = project.tasks.create('Foo',GradleTest)
//
//        expect:
//        task.group == Names.TASK_GROUP
//        task.description == "Runs all the compatibility tests for 'Foo'"
//        task.versions.empty
//        task.sourceDir == project.file("${project.projectDir}/src/Foo")
//        task.outputDirs.empty
//
//    }
//
//    def "Basic Configured task"() {
//        given:
//        def task = project.tasks.create('Foo',GradleTest)
//        def output = new File(project.buildDir,'Foo')
//
//        task.versions '2.2','2.3'
//        task.versions '2.4','2.4'
//
//        expect:
//        task.versions == (['2.2','2.3','2.4'] as Set<String>)
//        task.outputDirs == [ new File(output,'2.2'),new File(output,'2.3'),new File(output,'2.4')] as Set
//
//    }
}

