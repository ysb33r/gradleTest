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
package org.ysb33r.gradle.gradletest.legacy20.internal

import groovy.transform.CompileDynamic
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class UnpackerIntegrationSpec extends Specification {

    static final File TESTDIST = new File( System.getProperty('GRADLETESTDIST')  )
    static final String VERSION = versionFromFile(TESTDIST)


    static String versionFromFile(File dist) {
        def matcher = dist.name =~ /gradle-(.+?)(?:-(all|bin))?\.zip/
        assert matcher.matches()
        matcher[0][1]
    }

    static File findInstalledVersionDir( final File startDir,final String version ) {
        File found = null
        startDir.eachDir { File intermediateDir ->
            intermediateDir.eachDirMatch ~/gradle-${version}/, { installDir ->
                found = installDir
            }
        }
        found
    }

    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    Project project

    void setup() {
        project = ProjectBuilder.builder().withName('uis').withProjectDir(testProjectDir.root).build()

    }

    def "Unpack a downloaded distribution"() {
        given: "That I have a downloaded distribution"
        assert TESTDIST.exists()
        File userDir = new File(project.projectDir,'distribution')
        assert !userDir.exists()
        File outputTopDir = new File(userDir,"wrapper/dists/gradle-${VERSION}-bin")

        when: "I attempt to unpack to a fake Gradle User Home"
        Unpacker.unpackTo(userDir,TESTDIST,project.logger)

        then: "I expect the output directory to have been created"
        outputTopDir.exists()

        when: "I check the output directory"
        File installed = findInstalledVersionDir(outputTopDir,VERSION)

        then: "The distribution must be unpacked in to the appropriate version folder"
        installed != null
        new File(installed,'bin/gradle').exists()


    }
}