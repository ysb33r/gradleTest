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
package org.ysb33r.gradle.gradletest

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.ysb33r.gradle.gradletest.internal.GradleTestIntegrationSpecification
import spock.lang.Issue
import spock.lang.Specification


/**
 * Run `gradle integrationTestRepo` before running this from IDE.
 */
class CompileIntegrationSpec extends Specification {

    static final List TESTNAMES = ['alpha','beta','gamma']
    static final File GRADLETESTREPO = new File(System.getProperty('GRADLETESTREPO') ?: 'build/integrationTest/repo').absoluteFile

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    @Delegate Project project

    void setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
    }

    void setupForNames(List<String> projNames) {
        File srcDir = new File(project.projectDir,'src/gradleTest')
        srcDir.mkdirs()
        projNames.each {
            File testDir = new File(srcDir,it)
            testDir.mkdirs()
            new File(testDir,'build.gradle').text = "task runGradleTest {println '${it}'}"
        }
    }

    def "Compiling generated source code"() {
        given: "A GradleTest project"
        setupForNames(TESTNAMES)
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'

            repositories {
                flatDir {
                    dirs GRADLETESTREPO
                }
            }

            gradleTest {
                versions '2.1','2.2'
            }

            evaluate()
        }

        when: "The generated source is compiled"
        Task generatorTask = tasks.getByName('gradleTestGenerator')
        Task compileTask = tasks.getByName('compileGradleTestGroovy')
        Task testTask = tasks.getByName('gradleTest')
        SourceSet sourceSet = project.sourceSets.getByName('gradleTest')
        File classesDir = sourceSet.output.classesDir
        generatorTask.execute()
        compileTask.execute()


        then: "Generator task to have executed"
        generatorTask.didWork

        and: "Expected classes in output directory"
        new File(classesDir,"gradleTest/tests/AlphaCompatibilitySpec.class").exists()
    }

    @Issue('https://github.com/ysb33r/gradleTest/issues/46')
    def "Compiling a project name containing non-word characters"() {
        given: "A GradleTest project"
        def troublesomeNames = [ 'contains-d-a-s-h-e-s', 'contains s p a c e s']
        setupForNames(troublesomeNames)
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'

            repositories {
                flatDir {
                    dirs GRADLETESTREPO
                }
            }

            gradleTest {
                versions '2.1','2.2'
            }

            evaluate()
        }

        when: "The generated source is compiled"
        Task generatorTask = tasks.getByName('gradleTestGenerator')
        Task compileTask = tasks.getByName('compileGradleTestGroovy')
        SourceSet sourceSet = project.sourceSets.getByName('gradleTest')
        File classesDir = sourceSet.output.classesDir
        generatorTask.execute()
        compileTask.execute()

        then: "Generator task to have executed"
        generatorTask.didWork

        and: "Expected classes in output directory"
        new File(classesDir,"gradleTest/tests/Contains_d_a_s_h_e_sCompatibilitySpec.class").exists()
        new File(classesDir,"gradleTest/tests/Contains_s_p_a_c_e_sCompatibilitySpec.class").exists()

    }

    @Issue('https://github.com/ysb33r/gradleTest/issues/49')
    def "Delete old generated files when tests are deleted"() {
        given: "A GradleTest project"
        File buildDir = project.buildDir
        setupForNames(TESTNAMES)
        def configureProject = {
            configure(project) {
                apply plugin : 'org.ysb33r.gradletest'

                repositories {
                    flatDir {
                        dirs GRADLETESTREPO
                    }
                }

                gradleTest {
                    versions '2.13'
                }

                evaluate()
            }
        }

        when: "The generated source is compiled"
        configureProject()
        Task generatorTask = tasks.getByName('gradleTestGenerator')
        Task compileTask = tasks.getByName('compileGradleTestGroovy')
        File classesDir = project.sourceSets.getByName('gradleTest').output.classesDir
        File alphaSource = new File(buildDir,"gradleTest/src/AlphaCompatibilitySpec.groovy")
        File betaSource = new File(buildDir,"gradleTest/src/BetaCompatibilitySpec.groovy")
        File alphaClass = new File(classesDir,"gradleTest/tests/AlphaCompatibilitySpec.class")
        File betaClass = new File(classesDir,"gradleTest/tests/BetaCompatibilitySpec.class")
        generatorTask.execute()
        compileTask.execute()


        then: "Generator task to have executed"
        generatorTask.didWork
        alphaSource.exists()
        betaSource.exists()
        alphaClass.exists()
        betaClass.exists()

        when: "Alpha test is deleted"
        new File(project.projectDir,'src/gradleTest/alpha').deleteDir()
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        configureProject()
        generatorTask = tasks.getByName('gradleTestGenerator')
        compileTask = tasks.getByName('compileGradleTestGroovy')
        generatorTask.execute()
        compileTask.execute()

        then: "Alpha generated source & compiles class is also deleted"
        generatorTask.didWork
        !alphaSource.exists()
        betaSource.exists()
        !alphaClass.exists()
        betaClass.exists()
    }

    @Issue('https://github.com/ysb33r/gradleTest/issues/59')
    def "Adding folders rerun test task"() {
        given: "A GradleTest project"
        File buildDir = project.buildDir
        setupForNames(TESTNAMES)
        def configureProject = {
            configure(project) {
                apply plugin : 'org.ysb33r.gradletest'

                repositories {
                    flatDir {
                        dirs GRADLETESTREPO
                    }
                }

                gradleTest {
                    versions '2.13'
                }

                evaluate()
            }
        }

        when: "The generated source is compiled"
        configureProject()
        Task generatorTask = tasks.getByName('gradleTestGenerator')
        Task compileTask = tasks.getByName('compileGradleTestGroovy')
        File classesDir = project.sourceSets.getByName('gradleTest').output.classesDir
        File alphaSource = new File(buildDir,"gradleTest/src/AlphaCompatibilitySpec.groovy")
        File betaSource = new File(buildDir,"gradleTest/src/BetaCompatibilitySpec.groovy")
        File alphaClass = new File(classesDir,"gradleTest/tests/AlphaCompatibilitySpec.class")
        File betaClass = new File(classesDir,"gradleTest/tests/BetaCompatibilitySpec.class")
        generatorTask.execute()
        compileTask.execute()


        then: "Generator task to have executed"
        generatorTask.didWork
        alphaSource.exists()
        betaSource.exists()
        alphaClass.exists()
        betaClass.exists()

        when: "Alpha test is deleted"
        new File(project.projectDir,'src/gradleTest/alpha').deleteDir()
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        configureProject()
        generatorTask = tasks.getByName('gradleTestGenerator')
        compileTask = tasks.getByName('compileGradleTestGroovy')
        generatorTask.execute()
        compileTask.execute()
        println "*** ${alphaClass.parentFile.listFiles()}"

        then: "Alpha generated source & compiles class is also deleted"
        generatorTask.didWork
        !alphaSource.exists()
        betaSource.exists()
        !alphaClass.exists()
        betaClass.exists()
    }

}