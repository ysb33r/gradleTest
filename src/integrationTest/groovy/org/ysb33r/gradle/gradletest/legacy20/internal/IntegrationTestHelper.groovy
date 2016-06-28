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
package org.ysb33r.gradle.gradletest.legacy20.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.maven.artifact.ant.shaded.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class IntegrationTestHelper {
    static final String TESTROOT = new File( System.getProperty('TESTROOT') ?: 'build/tmp/integrationtestroot').absoluteFile
    static final String PROJECTROOT = new File( System.getProperty('PROJECTROOT') ?: '.' ).absoluteFile
    static final boolean OFFLINE = System.getProperty('OFFLINE')
    static final File CURRENT_GRADLEHOME = new File( System.getProperty('CURRENT_GRADLEHOME') )
    static final File TESTDIST = new File( System.getProperty('GRADLETESTDIST')  )
    static final String REPOTESTFILES = System.getProperty('GRADLETESTREPO')

    static Project buildProject(final String testName) {
        File pDir = new File(TESTROOT,testName)
        if(pDir.exists()) {
            pDir.deleteDir()
        }
        pDir.mkdirs()
        ProjectBuilder.builder().withName(testName).withProjectDir(pDir).build()
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

    @CompileDynamic
    static String versionFromFile(File dist) {
        def matcher = dist.name =~ /gradle-(.+?)(?:-(all|bin))?\.zip/
        assert matcher.matches()
        matcher[0][1]
    }

    static void createTestRepo(final File repoDir) {
        repoDir.mkdirs()
        REPOTESTFILES.split(',').each { String it->
            File src = new File(it)
            FileUtils.copyFile(src,new File(repoDir,src.name))
        }
    }
}
