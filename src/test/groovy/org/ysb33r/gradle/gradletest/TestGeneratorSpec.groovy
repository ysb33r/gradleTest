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
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskState
import org.ysb33r.gradle.gradletest.internal.GradleTestSpecification
import spock.lang.Specification


class TestGeneratorSpec extends GradleTestSpecification {

    def 'Creating source code from template'() {
        given: "A project that contains a src/gradleTest layout"
        def tests = ['alpha','beta','gamma','delta','eta']
        File srcDir = new File(projectDir,'src/gradleTest')
        srcDir.mkdirs()
        tests.each {
            File testDir = new File(srcDir,it)
            testDir.mkdirs()
            if(it != 'delta') {
                new File(testDir,'build.gradle').text = ''
            }
            if (it=='eta') {
                new File(testDir,'build.gradle.kts').text = ''
            }
        }

        when: "The plugin is applied and test task is configured"
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'

            gradleTest {
                versions '1.999','1.998'
                versions '1.997'
                kotlinDsl = true
            }
            evaluate()
        }
        TestGenerator genTask = tasks.getByName('gradleTestGenerator')

        and: "The generator task is executed"
        genTask.execute()
        Set testNames = genTask.testMap.keySet()
        String initScriptContent = new File(genTask.outputDir.parentFile,'init.gradle').text

        then: "The test names reflect the directories under gradleTest"
        testNames.containsAll(['alphaGroovyDSL','betaGroovyDSL','gammaGroovyDSL','etaGroovyDSL','etaKotlinDSL'])

        and: "Groovy+Spock test files are generated in the source set directory under the build directory"
        genTask.outputDir.exists()
        new File(genTask.outputDir,"AlphaGroovyDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"BetaGroovyDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"GammaGroovyDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"EtaKotlinDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"EtaGroovyDSLCompatibilitySpec.groovy").exists()

        and: "Subdirectories without a build.gradle file will not be included"
        !testNames.containsAll(['delta'])
        !new File(genTask.outputDir,"DeltaCompatibilitySpec.groovy").exists()

        and: "An initscript is created"
        initScriptContent.contains("classpath fileTree ('${new File(buildDir,'libs').toURI()}'.toURI())")
        initScriptContent.contains("dirs '${new File(buildDir,'gradleTest/repo').toURI()}'.toURI()")

        when: "The generated source file is inspected"
        String source = new File(genTask.outputDir,"AlphaGroovyDSLCompatibilitySpec.groovy").text

        then:
        source.contains "package ${genTask.testPackageName}"
        source.contains "result.task(':runGradleTest')" 
        source.contains "def \"AlphaGroovyDSL : #version\"()"
        source.contains "version << ['1.999','1.998','1.997']"
    }

    def "When there is no gradleTest folder, the task should not fail, just be skipped"() {
        given: "There is no src/gradleTest folder and the plugin is applied"
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'

            gradleTest {
                versions '1.997'
            }
            evaluate()
        }

        when: 'The evaluation phase is completed'
        project.evaluate()

        then: "No exception when getTestMap is requested"
        [:] == project.gradleTestGenerator.testMap

        when: 'The task is executed'
        project.tasks.getByName(TestSet.getGeneratorTaskName(Names.DEFAULT_TASK)).execute()

        then: 'The generator task should be skipped'
        project.tasks.getByName(TestSet.getGeneratorTaskName(Names.DEFAULT_TASK)).state.skipped
    }
}