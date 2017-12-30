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

import org.ysb33r.gradle.gradletest.internal.GradleTestSpecification

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
            if(it == 'beta') {
                new File(testDir,'testTwo.gradle.kts').text = ''
            }
            if (it=='eta') {
                new File(testDir,'build.gradle.kts').text = ''
            }
        }

        when: "The plugin is applied and test task is configured"
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'

            gradleTest {
                versions '2.999','2.998'
                versions '2.997'
                versions '4.0'
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
        testNames.containsAll(['alpha', 'beta', 'gamma', 'eta'])
        assert genTask.testMap['alpha'].kotlinBuildFiles.size() == 0
        assert genTask.testMap['alpha'].groovyBuildFiles.size() == 1
        assert genTask.testMap['beta'].kotlinBuildFiles.size() == 1
        assert genTask.testMap['beta'].groovyBuildFiles.size() == 1
        assert genTask.testMap['gamma'].kotlinBuildFiles.size() == 0
        assert genTask.testMap['gamma'].groovyBuildFiles.size() == 1
        assert genTask.testMap['eta'].kotlinBuildFiles.size() == 1
        assert genTask.testMap['eta'].groovyBuildFiles.size() == 1

        and: "Groovy+Spock test files are generated in the source set directory under the build directory"
        genTask.outputDir.exists()
        new File(genTask.outputDir,"Alpha_BuildGroovyDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"Beta_BuildGroovyDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"Beta_TestTwoKotlinDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"Gamma_BuildGroovyDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"Eta_BuildGroovyDSLCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"Eta_BuildKotlinDSLCompatibilitySpec.groovy").exists()
//        new File(genTask.outputDir,"EtaGroovyDSLCompatibilitySpec.groovy").exists()

        and: "Subdirectories without a build.gradle file will not be included"
        !testNames.containsAll(['delta'])
        !new File(genTask.outputDir,"DeltabuildCompatibilitySpec.groovy").exists()

        and: "An initscript is created"
        initScriptContent.contains("classpath fileTree ('${new File(buildDir,'libs').toURI()}'.toURI())")
        initScriptContent.contains("dirs '${new File(buildDir,'gradleTest/repo').toURI()}'.toURI()")

        when: "The generated source file is inspected"
        String source = new File(genTask.outputDir,"Alpha_BuildGroovyDSLCompatibilitySpec.groovy").text

        then:
        source.contains "package ${genTask.testPackageName}"
        source.contains "result.task(':runGradleTest')" 
        source.contains "def \"Alpha_BuildGroovyDSL : #version\"()"
        source.contains "version << ['2.999','2.998','2.997','4.0']"
    }

    def "When there is no gradleTest folder, the task should not fail, just be skipped"() {
        given: "There is no src/gradleTest folder and the plugin is applied"
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'

            gradleTest {
                versions '2.997'
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
