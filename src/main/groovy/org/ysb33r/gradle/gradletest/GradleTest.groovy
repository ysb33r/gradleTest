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

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.commons.lang.NotImplementedException
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.util.CollectionUtils
import org.ysb33r.gradle.gradletest.internal.Infrastructure
import org.ysb33r.gradle.gradletest.internal.TestRunner

/**
 * @author Schalk W. Cronjé
 */
class GradleTest extends DefaultTask {

    GradleTest() {
        group = Names.TASK_GROUP
        description = "Runs all the compatibility tests for '${name}'"
    }

    /** Returns the set of Gradle versions to tests against
     *
     * @return Set of unique versions
     */
    @Input
    @SkipWhenEmpty
    @TypeChecked
    Set<String> getVersions() {
        CollectionUtils.stringize(this.versions) as Set<String>
    }

    /** Returns the list of tests to be executed
     *
     * @return Set of test names
     */
    @Input
    @SkipWhenEmpty
    @CompileStatic
    List<String> getTestNames() {
        List<String> tests = []
        sourceDir.eachDir { File dir->
            if(new File(dir,'build.gradle').exists()) {
                tests+= dir.name
            }
        }
        tests
    }



    /** Adds Gradle versions to be tested against.
     *
     * @param v One or more versions. Must be convertible to strings.
     */
    @CompileStatic
    void versions(Object... v) {
        this.versions += (v as List).flatten()
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

        if(initScript == null) {
            setInitScriptFromResource()
        }

        Map<String,File> locations = [:]
        GradleTestExtension config = project.extensions.findByName(Names.EXTENSION) as GradleTestExtension
        getVersions().each { String it ->
            locations[it] = config.distributions?.location(it)
            if(locations[it] == null) {
                throw new TaskExecutionException(this,new FileNotFoundException("No distribution is available for version '${it}'"))
            }
        }

        logger.info "${name}: Preparing infrastructure for compatibility testing"
        testRunners = Infrastructure.create(
            project : project,
            tests : testNames,
            locations : locations,
            name : name,
            sourceDir : sourceDir,
            initScript : initScript,
            versions : versions
        )

        testRunners.each {
            it.run()
            logger.lifecycle "${it.testName}: ${it.execResult.exitValue?'FAILED':'PASSED'}"
        }

        int failed = testRunners.count { it.execResult.exitValue  }

        logger.lifecycle "${name} Compatibility Test Executor finished execuring tests"
        // Now gather the results
        // TODO: Build HTML report into build/reports

        if (failed) {
            String msg = "${failed} compatibility tests failed.\n"
            msg+=        "-------------------------------------\n"
            testRunners.findAll {it.execResult.exitValue}.each {
                msg+= "${name}:${it.testName}:${it.version}: FAILED\n"
            }
            msg+=        "-------------------------------------\n"
            logger.lifecycle msg
// TODO:            throw new TaskExecutionException(this, new StopActionException("One or more compatibility tests have failed: You can see the report at TODO"))
            throw new TaskExecutionException(this, new StopActionException("One or more compatibility tests have failed. Check the screen output for now."))
        }
    }



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
                initScript= project.zipTree(location).filter { it.name == 'init.gradle'}
            } else if(uri.scheme.startsWith('file')) {
                initScript= location.replace('file:','')
            } else {
                throw new GradleException("Cannot extract ${uri}")
            }
        }

        /*
                    String location = uri.getSchemeSpecificPart().replace('!/'+BOOTSTRAP_TEMPLATE_PATH,'')
            if(uri.scheme.startsWith('jar')) {
                location=location.replace('jar:file:','')
                source= task.project.zipTree(location)
            } else if(uri.scheme.startsWith('file')) {
                source= location.replace('file:','')
            } else {
                throw new GradleException("Cannot extract ${uri}")
            }

         */
    }

    private List<Object> versions = []
    private List<TestRunner> testRunners = []
    private Object initScript
    private final static String  INIT_GRADLE_PATH = 'org.ysb33r.gradletest/init.gradle'

    /** Called by afterEvaluate to look for versions in all GradleTest tasks
     *
     * @param project Project that tasks are configured within.
     * @return A set of versions strings.
     */
    @CompileStatic
    static Set<String> findAllRequiredVersions(Project project) {
        // Get list of required versions
        Set<String> foundVersions = []
        project.tasks.withType(GradleTest) { GradleTest t ->
            foundVersions+= t.versions as Set<String>
        }
        foundVersions
    }
}
