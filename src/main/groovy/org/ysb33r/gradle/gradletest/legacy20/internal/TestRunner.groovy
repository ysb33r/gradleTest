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
package org.ysb33r.gradle.gradletest.legacy20.internal

import groovy.transform.TupleConstructor
import org.gradle.api.Project
import org.gradle.process.ExecResult

/** Runs a single test in legacy mode.
 *
 */
@TupleConstructor
class TestRunner {

    Project project
    File gradleLocationDir
    File testProjectDir
    File initScript
    String version
    String testName

    ExecResult execResult = null

    ExecResult run() {

        File settings = new File(testProjectDir,'settings.gradle')

        if(!settings.exists()) {
            settings.text = ''
        }

        project.logger.info "Running ${testName} against Gradle ${version}"

        execResult = project.javaexec {
            ignoreExitValue = true
            classpath "${gradleLocationDir}/lib/gradle-launcher-${version}.jar"
            workingDir testProjectDir.absoluteFile

            main 'org.gradle.launcher.GradleMain'
            args '--project-cache-dir',"${testProjectDir}/.gradle"
            args '--gradle-user-home',  "${testProjectDir}/../home"
            args '--no-daemon'
            args '--full-stacktrace'
            args '--info'
            args '--init-script',initScript.absolutePath

            if(project.gradle.startParameter.offline) {
                args '--offline'
            }

            args 'runGradleTest'

            systemProperties 'org.gradle.appname' : this.class.name

            // Capture standardOutput and errorOutput
            // errorOutput = new ByteArrayOutputStream()
            // standardOutput = new ByteArrayOutputStream()
            // systemProperties & environment in a later release
        }
    }
}
