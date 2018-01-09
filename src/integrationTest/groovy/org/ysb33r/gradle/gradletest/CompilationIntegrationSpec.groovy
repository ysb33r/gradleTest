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
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.ysb33r.gradle.gradletest.internal.GradleTestIntegrationSpecification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class CompilationIntegrationSpec extends GradleTestIntegrationSpecification {

    static final File GRADLETESTREPO = new File(System.getProperty('GRADLETESTREPO') ?: 'build/integrationTest/repo').absoluteFile

    Project project
    File srcDir

    void setup() {
        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        srcDir = new File(project.projectDir,'src/gradleTest')
        srcDir.mkdirs()
    }

    @Unroll
    def 'Compile generated code using Gradle #gradleVer'() {
        setup:
        configureGradleTest()
        genTestStructure()

        when:
        def result = GradleRunner.create()
            .withProjectDir(project.projectDir)
            .withArguments('compileGradleTestGroovy', 'gradleTestClasses', 'checkTests', 'gradleTest', '--console=plain', '-i')
            .withPluginClasspath(readMetadataFile())
            .withGradleDistribution(new File(GRADLETESTREPO,"gradle-${gradleVer}-bin.zip").toURI())
            .forwardOutput()
.withDebug(true)
            .build()

        then:
        result.task(':gradleTestGenerator').outcome == SUCCESS
        result.task(':compileGradleTestGroovy').outcome == SUCCESS
        result.task(':gradleTestClasses').outcome == SUCCESS
        new File(project.buildDir,"classes${classesLang}/gradleTest/gradleTest/tests/AlphaGroovyDSLCompatibilitySpec.class").exists()
        result.task(':gradleTest').outcome == SUCCESS

        where:
        gradleVer | classesLang
        '3.0'     | ''
        '4.0.2'   | '/groovy'
    }

    private void genTestStructure() {
        File testDir = new File(srcDir, 'alpha')
        testDir.mkdirs()
        new File(testDir, 'build.gradle').text = '''
            task runGradleTest  {
                doLast {
                }
            }
'''
    }

    private void configureGradleTest() {
        writeBuildScriptHeader()
        buildFile << """

        repositories {
            flatDir {
                dirs '${GRADLETESTREPO.toURI()}'.toURI()
            }
        }

        gradleTest {
            versions '3.0', '4.0.2'
            gradleDistributionUri '${GRADLETESTREPO.toURI()}'
        }
        
        task checkTests {
            doLast {
                FileTree ft
                if(GradleVersion.current() < GradleVersion.version('4.0')) {
                  ft = fileTree(gradleTest.testClassesDir)
                } else {
                  ft = gradleTest.testClassesDirs.asFileTree               
                } 
                println "***** \${ft.files}"
                if(ft.files.empty) {
                  throw GradleException('No classes available to test')                 
                }
            }
        }
"""
    }
}