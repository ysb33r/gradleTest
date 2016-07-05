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
package org.ysb33r.gradle.gradletest.legacy20

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.util.CollectionUtils
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.legacy20.internal.Infrastructure
import org.ysb33r.gradle.gradletest.legacy20.internal.TestRunner

/** Legacy GradleTest task type. Copies build projects to new directories and runs various
 * Gradle versions against those projects. Will download distributions if necessary.
 */
@CompileStatic
class GradleTest extends DefaultTask {

    GradleTest() {
        group = Names.TASK_GROUP
        description = "Runs all the compatibility tests for '${name}'"

//        onlyIf {
//            !getVersions().empty && !getTestNames().empty
//        }
    }

    /** Returns the set of Gradle versions to tests against
     *
     * @return Set of unique versions
     */
    @Input
    @CompileDynamic // We do this as the signature has changed between Gradle Versions
    Set<String> getVersions() {
        CollectionUtils.stringize(this.versions) as Set<String>
    }

//    No signature of method:
//    static org.gradle.util.CollectionUtils.stringize()
//    is applicable for argument types: (java.util.ArrayList) values: [[2.1]]
//    Possible solutions: stringize(java.util.List), stringize(java.lang.Iterable, java.util.Collection)

    /** Adds Gradle versions to be tested against.
     *
     * @param v One or more versions. Must be convertible to strings.
     */
    @CompileDynamic
    void versions(Object... v) {
        this.versions += (v as List).flatten()
    }

    /** Returns the list of tests to be executed
     *
     * @return Set of test names
     */
    @Input
    @CompileStatic
    List<String> getTestNames() {
        List<String> tests = []
        sourceDir.eachDir { File dir->
            if(new File(dir,'build.gradle').exists()) {
                tests.add(dir.name)
            }
        }
        tests
    }

    /** Sets the lcoation of an alternative init script.
     *
     * @param script Location of alternative Gradle init script.
     * Anything that can be passed to {@code project.file} is allowed.
     * @since 0.5.5
     */
    void initscript(Object script) {
        this.initscript = script
    }

    /** Returns the location of the initialisation script.
     * If it was not set it will be located from within the embedded resources.
     *
     * @return Location of the script.
     * @since 0.5.5
     */
    @Input
    Object getInitscript() {
        if(this.initscript == null) {
            setInitScriptFromResource()
        }
        this.initscript
    }

    /** Returns the source directory for finding tests.
     *
     * @return Top-level source directory
     */
    @CompileStatic
    @InputDirectory
    File getSourceDir() {
        (project as Project).file(new File(project.projectDir,"src/${name}"))
    }

    /** List of output directories where tests will be copied to and executed.
     *
     * @return A list of directories
     */
    @CompileStatic
    @OutputDirectories
    Set<File> getOutputDirs() {
        versions.collect {
            new File(project.buildDir,"${name}/${it}")
        } as Set<File>
    }

    /** Returns a list of test results
     *
     * @return The list of results (or null if task has not yet executed).
     */
    def getTestResults() {
        if(testRunners.empty) {
            return null
        }

        testRunners.collect { TestRunner run ->
            [ getTestName : {run.testName},
              getGradleVersion : {run.version},
              getPassed : {run.execResult.exitValue == 0}
            ] as CompatibilityTestResult
        }
    }

    @TaskAction
    void exec() {

        TreeMap<String,File> locations = allLocations

        logger.info "${name}: Preparing infrastructure for compatibility testing"
        testRunners = Infrastructure.create(
            project : project,
            tests : testNames,
            locations : locations,
            name : name,
            sourceDir : sourceDir,
            initScript : getInitscript(),
            versions : versions
        )

        for(TestRunner it in testRunners) {
            it.run()
            logger.lifecycle "${it.testName}: ${it.execResult.exitValue?'FAILED':'PASSED'}"
        }

        int failed = (int)testRunners.count { it.execResult.exitValue  }

        logger.lifecycle "${name} Compatibility Test Executor finished execuring tests"

        // Now gather the results
        if (failed) {
            String msg = "${failed} compatibility tests failed.\n"
            msg+=        "-------------------------------------\n"
            testRunners.findAll {it.execResult.exitValue}.each {
                msg+= "${name}:${it.testName}:${it.version}: FAILED\n"
            }
            msg+=        "-------------------------------------\n"
            logger.lifecycle msg
            throw new TaskExecutionException(this, new StopActionException("One or more compatibility tests have failed. Check the screen output for now."))
        }
    }

    @CompileDynamic
    private TreeMap<String,File> getAllLocations() {
        TreeMap<String,File> locations = [:] as TreeMap
        GradleTestExtension config = project.extensions.findByName(Names.EXTENSION) as GradleTestExtension
        for( String it in getVersions()) {
            locations[it] = config.distributions?.location(it)
            if(locations[it] == null) {
                throw new TaskExecutionException(this,new FileNotFoundException("No distribution is available for version '${it}'"))
            }
        }
        locations
    }

    @CompileDynamic
    private void setInitScriptFromResource() {
        Enumeration<URL> enumResources
        enumResources = this.class.classLoader.getResources( INIT_GRADLE_PATH)
        if(!enumResources.hasMoreElements()) {
            throw new GradleException ("Cannot find ${INIT_GRADLE_PATH} in classpath")
        } else {
            URI uri = enumResources.nextElement().toURI()
            String location = uri.getSchemeSpecificPart().replace('!/'+INIT_GRADLE_PATH,'')
            if(uri.scheme.startsWith('jar')) {
                location=location.replace('jar:file:','')
                this.initscript= project.zipTree(location).filter { it.name == 'init20.gradle'}
            } else if(uri.scheme.startsWith('file')) {
                this.initscript= location.replace('file:','')
            } else {
                throw new GradleException("Cannot extract ${uri}")
            }
        }
    }

    private List<Object> versions = []
    private List<TestRunner> testRunners = []
    private Object initscript
    private final static String  INIT_GRADLE_PATH = 'org/ysb33r/gradletest/legacy20/init20.gradle'

    /** Called by afterEvaluate to look for versions in all GradleTest tasks
     *
     * @param project Project that tasks are configured within.
     * @return A set of versions strings.
     */
    static Set<String> findAllRequiredVersions(Project project) {
        // Get list of required versions
        Set<String> foundVersions = []
        project.tasks.withType(GradleTest) { GradleTest t ->
            foundVersions.addAll(t.getVersions())
        }
        foundVersions
    }
}
