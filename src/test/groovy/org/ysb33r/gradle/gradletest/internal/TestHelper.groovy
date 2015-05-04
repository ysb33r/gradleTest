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
package org.ysb33r.gradle.gradletest.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.testfixtures.ProjectBuilder

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class TestHelper {
    static final String TESTROOT = new File( System.getProperty('TESTROOT') ?: '.').absoluteFile

    static Project buildProject(final String testName) {
        File pDir = new File(TESTROOT,testName)
        if(pDir.exists()) {
            pDir.deleteDir()
        }
        pDir.mkdirs()
        ProjectBuilder.builder().withName(testName).withProjectDir(pDir).build()
    }

}
