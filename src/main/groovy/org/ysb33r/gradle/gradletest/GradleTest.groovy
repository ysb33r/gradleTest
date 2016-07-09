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
package org.ysb33r.gradle.gradletest

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.testing.Test
import org.gradle.util.CollectionUtils
import org.gradle.util.GradleVersion

/**
 * Runs compatibility tests using special compiled GradleTestKit-based tests
 */
@CompileStatic
class GradleTest extends Test {

    GradleTest() {
        if(GradleVersion.current() < GradleVersion.version('2.13')) {
            throw new GradleException("GradleTest is only compatible with Gradle 2.13+. " +
                "If you are using an older version of Gradle, please use " +
                "org.ysb33r.gradle.gradletest.legacy20.GradleTest instead."
            )
        }

        if(project.gradle.startParameter.offline) {
            arguments+= '--offline'
        }
    }

    /** Returns the set of Gradle versions to tests against
     *
     * @return Set of unique versions
     */
    @Input
    Set<String> getVersions() {
        String override = System.getProperty("${name}.versions")
        if(override?.size()) {
            return override.split(',') as Set<String>
        }
        CollectionUtils.stringize(this.versions) as Set<String>
    }

    /** Add Gradle versions to be tested against.
     *
     * @param ver List of versions
     */
    void versions(String... ver) {
        this.versions += (ver as List)
    }

    /** Append additional arguments to be sent to the running GradleTest instance.
     *
     * @param args Additional arguments
     */
     void gradleArguments(Object... args ) {
        arguments.addAll(args as List)
    }

    /** Override any existing arguments with new ones
     *
     * @param newArgs
     */
    void setGradleArguments(final List<Object> newArgs ) {
        arguments.clear()
        arguments.addAll(newArgs)
    }

    /** Sets the Base URI where to find distributions.
     * If this is not set, GradleTest will default to using whatever is in the cache at the time or try to download
     * from the Gradle distribution download site. If this is set, only this base URI will be used, nothing else.
     *
     * At runtime URL behaviour can be overridden by setting the system property
     * {@code org.ysb33r.gradletest.distribution.uri}.
     *
     * GradleTest will only look for {@code gradle*-bin.zip} packages.
     *
     * @param baseUri
     */
    void setGradleDistributionUri(Object baseUri) {
        switch(baseUri) {
            case null:
                baseDistributionUri = null
                break
            case URI:
                baseDistributionUri = (URI)baseUri
                break
            case File:
                baseDistributionUri= ((File)baseUri).absoluteFile.toURI()
                break
            case String:
                if( ((String)baseUri).empty ) {
                    baseDistributionUri = null
                } else {
                    final tmpStr = (String)baseUri
                    URI tmpUri = tmpStr.toURI()
                    if(tmpUri.scheme == null) {
                        setGradleDistributionUri(project.file(tmpStr))
                    } else {
                        baseDistributionUri = tmpUri
                    }
                }
                break
            default:
                setGradleDistributionUri( baseUri.toString() )
        }
    }

    /** Returns the base URI for finding Gradle distributions previously set by
     * {@link #setGradleDistributionUri}. If that is not set, it returns {@code null}, indicating that default
     * behaviour should be used.
     *
     * @return Location of distributions or null.
     */
    URI getGradleDistributionUri() {
        baseDistributionUri
    }

    /** Returns the arguments that needs to be passed to the running GradleTest instance
     *
     * @return List of arguments in order
     */
    @Input
    List<String> getGradleArguments() {
        ([ '--init-script',initScript ] as List<String>) +
        CollectionUtils.stringize(this.arguments) as List<String>
        /* Do we need any of these?
            args '--project-cache-dir',"${testProjectDir}/.gradle"
            args '--gradle-user-home',  "${testProjectDir}/../home"
            * If gradle is run with `--offline`, it will be passed to the Gradle.
* `--project-cache-dir` is always set to at the start of the project in `buildDir`
* `--full-stacktrace` is set and output is captured to test report.
* Handle no-daemon as a special case

        */
    }

    /** The name of the task that will be executed in the test project
     *
     * @return Test task name
     */
    @Input
    String getDefaultTask() {
        'runGradleTest'
    }

    @Override
    void useTestNG() {
        notSupported('useTestNG()')
    }

    @Override
    void useTestNG(Closure testFrameworkConfigure) {
        notSupported('useTestNG()')
    }

    /** Returns path to initscript that will be used for tests
     *
     * @return Path to init.gradle script
     */
    String getInitScript() {
        project.file("${project.buildDir}/${name}/init.gradle").absolutePath
    }

    private List<Object> arguments = [/*'--no-daemon',*/ '--full-stacktrace', '--info'] as List<Object>
    private List<Object> versions = []
    private URI baseDistributionUri


    private void notSupported(final String name) {
        throw new GradleException("${name} is not supported in GradleTest tasks")
    }
}

