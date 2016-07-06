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
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.ysb33r.gradle.gradletest.internal.GradleTestSpecification
import spock.lang.Specification


class TestGeneratorSpec extends GradleTestSpecification {

    def 'Creating source code from template'() {
        given: "A project that contains a src/gradleTest layout"
        def tests = ['alpha','beta','gamma','delta']
        File srcDir = new File(projectDir,'src/gradleTest')
        srcDir.mkdirs()
        tests.each {
            File testDir = new File(srcDir,it)
            testDir.mkdirs()
            if(it != 'delta') {
                new File(testDir,'build.gradle').text = ''
            }
        }

        when: "The plugin is applied and test task is configured"
        configure(project) {
            apply plugin : 'org.ysb33r.gradletest'

            gradleTest {
                versions '1.999','1.998'
                versions '1.997'
            }
            evaluate()
        }
        TestGenerator genTask = tasks.getByName('gradleTestGenerator')

        and: "The generator task is executed"
        genTask.execute()
        Set testNames = genTask.testMap.keySet()

        then: "The test names reflect the directories under gradleTest"
        testNames.containsAll(['alpha','beta','gamma'])

        and: "Groovy+Spock test files are generated in the source set directory under the build directory"
        genTask.outputDir.exists()
        new File(genTask.outputDir,"AlphaCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"BetaCompatibilitySpec.groovy").exists()
        new File(genTask.outputDir,"GammaCompatibilitySpec.groovy").exists()

        and: "Subdirectories without a build.gradle file will not be included"
        !testNames.containsAll(['delta'])
        !new File(genTask.outputDir,"DeltaCompatibilitySpec.groovy").exists()

        and: "An initscript is created"
        new File(genTask.outputDir.parentFile,'init.gradle').text.contains ("""allprojects {
    buildscript {
        dependencies {
            classpath fileTree ('${new File(buildDir,'libs').toURI()}') {
                include '*.jar'
            }
        }
    }
}
""")
        when: "The generated source file is inspected"
        String source = new File(genTask.outputDir,"AlphaCompatibilitySpec.groovy").text

        then:
        source.contains "package ${genTask.testPackageName}"
        source.contains "result.task(':runGradleTest')" 
        source.contains "def \"Alpha : #version\"()"
        source.contains "version << ['1.999','1.998','1.997']"

    }

}