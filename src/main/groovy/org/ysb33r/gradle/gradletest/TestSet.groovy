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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.util.GradleVersion
import org.ysb33r.gradle.gradletest.internal.GradleVersions

import static org.ysb33r.gradle.gradletest.internal.GradleVersions.GRADLE_4_0_OR_LATER

/** Internal utility functions to add a new GradleTest test set.
 *
 * @since 1.0
 */
@CompileStatic
class TestSet {

    /** Adds a test set with default dependencies
     *
     * @param project Project to add tests to
     * @param testSetName Name of test set
     */
    static void addTestSet(Project project, final String testSetName) {
        String setname = baseName(testSetName)
        Map<String,String> configNames = configurationNames(testSetName)

        project.configurations.maybeCreate setname
        addConfigurations(project,configNames)
        addTestTasks(project,setname)
        addManifestTask(project,setname,configNames['runtime'])
        addTestDependencies(project,configNames['compile'])
        addSourceSets(project,setname,configNames)
    }

    /** Get a testset base name
     *
     * @param name Name to use as seed
     * @return Name that will be used as basename for configrations and tasks
     */
    static String baseName(final String testSetName) {
        testSetName == Names.DEFAULT_TASK ? Names.DEFAULT_TASK : "${testSetName}GradleTest"
    }

    /** Returns a map of configuration names key off {@code compile} and {@code runtime}
     *
     * @param testSetName
     * @return
     */
    static Map<String,String> configurationNames(final String testSetName) {
        final String name = baseName(testSetName)
        [ compile : "${name}Compile".toString(),
          runtime : "${name}Runtime".toString()
        ]
    }

    /** Gets the source dir where test code will be generated into.
     *
     * @param project
     * @param testSetBaseName A basename as returned by @link #baseName
     * @return A closure that can be lazy-evaluated.
     */
    @PackageScope
    static Closure getSourceDir(Project project,final String testSetBaseName) {
        { Project p, final String setname, final String postfix ->
            "${p.buildDir}/${setname}/src${postfix}"
        } .curry(project,testSetBaseName,GRADLE_4_0_OR_LATER ? '/groovy' : '')
    }

    /** Gets the directory where manifest file will be generated into.
     *
     * @param project
     * @param testSetBaseName A basename as returned by @link #baseName
     * @return A closure that can be lazy-evaluated.
     */
    static Closure getManifestDir(Project project,final String testSetBaseName) {
        { Project p,String setname ->
            "${p.buildDir}/${setname}/manifest"
        } .curry(project,testSetBaseName)
    }

    /** Gets the resource directory where test resources will be generated into.
     *
     * @param project
     * @param testSetBaseName A basename as returned by @link #baseName
     * @return A closure that can be lazy-evaluated.
     */
    @PackageScope
    static Closure getResourcesDir(Project project,final String testSetBaseName) {
        { Project p,String setname ->
            "${p.buildDir}/${setname}/resources"
        } .curry(project,testSetBaseName)
    }

    @PackageScope
    static String getGeneratorTaskName(final String testSetBaseName) {
        "${testSetBaseName}${Names.GENERATOR_TASK_POSTFIX}"
    }

    @PackageScope
    static String getCompileTaskName(final String testSetBaseName) {
        "compile${testSetBaseName.capitalize()}Groovy"
    }

    @PackageScope
    static String getTestTaskName(final String testSetBaseName) {
        testSetBaseName
    }

    static String getManifestTaskName(final String testSetBaseName) {
        "${testSetBaseName}${Names.MANIFEST_TASK_POSTFIX}"
    }

    /** Adds the requires configurations
     *
     * @param Project
     * @param configNames A map of configuration names as created by a call to @link #configurationNames
     */
    @PackageScope
    static void addConfigurations(Project project, final Map<String,String> configNames) {
        configNames.each { String key,String val ->
            project.configurations.create(val) //.extendsFrom(project.configurations.getByName(key))
        }
        project.configurations.getByName(configNames['runtime']).extendsFrom(
            project.configurations.getByName(configNames['compile'])
        )
    }

    /** Adds the required test dependencies to a configuration
     *
     * @param project
     * @param configurationName
     */
    @PackageScope
    static void addTestDependencies(Project project,final String configurationName) {
        project.dependencies.with {
            // IMPORTANT: Due to GRADLE-3433 gradleTestKit must come before gradleApi
            add configurationName,localGroovy()
            add configurationName,gradleTestKit()
            add configurationName,gradleApi()
            add configurationName,spockDependency(project)
            add configurationName,"junit:junit:${JUNIT_VERSION}"
            add configurationName,"org.hamcrest:hamcrest-core:${HAMCREST_VERSION}"
        }
    }

    @CompileDynamic
    @PackageScope
    static void addSourceSets(Project project,final String testSetBaseName,final Map<String,String> configNames) {

        final String genTaskName = getGeneratorTaskName(testSetBaseName)
        final String compileTaskName = getCompileTaskName(testSetBaseName)
        final String testTaskName = getTestTaskName(testSetBaseName)



            project.sourceSets.create testSetBaseName, {
                groovy{
                    srcDirs = [project.file(getSourceDir(project,testSetBaseName))]

                    if (project.hasProperty("gradleTest.glob.include")){
                        String globs = project.properties.get("gradleTest.glob.include")

                        for (def glob in globs.split(';')){
                            include glob
                        }
                    }

                    if (project.hasProperty("gradleTest.glob.exclude")){
                        String globs = project.properties.get("gradleTest.glob.exclude")

                        for (def glob in globs.split(';')){
                            exclude glob
                        }
                    }
                }
                resources.srcDirs = [project.file( getResourcesDir(project,testSetBaseName) )]
                compileClasspath = project.configurations.getByName(configNames['compile'])
                runtimeClasspath = output + compileClasspath +
                    project.configurations.getByName(configNames['runtime']) +
                    project.sourceSets.main.output
            }

            project.tasks.getByName(compileTaskName).with {
                dependsOn(genTaskName)
                doFirst {
                    destinationDir.deleteDir()
                }

            }

            project.tasks.getByName(testTaskName).configure {
                if(GRADLE_4_0_OR_LATER) {
                    testClassesDirs = project.sourceSets.getByName(testSetBaseName).output.classesDirs
                    inputs.files (project.sourceSets.getByName(testSetBaseName).output.classesDirs).skipWhenEmpty

                } else {
                    testClassesDir = project.sourceSets.getByName(testSetBaseName).output.classesDir
                    inputs.dir (project.sourceSets.getByName(testSetBaseName).output.classesDir).skipWhenEmpty
                }
                classpath = project.sourceSets.getByName(testSetBaseName).runtimeClasspath

            }
    }

    @PackageScope
    static void addManifestTask(Project project,final String testSetBaseName,final String configurationName) {
        ClasspathManifest task = project.tasks.create( getManifestTaskName(testSetBaseName),ClasspathManifest)

        task.with {
            group = Names.TASK_GROUP
            description = 'Creates manifest file for ' + testSetBaseName
        }
    }

    @PackageScope
    static void addTestTasks(Project project,final String testSetBaseName) {

        final String genTaskName = getGeneratorTaskName(testSetBaseName)
        final String compileTaskName = getCompileTaskName(testSetBaseName)
        final String testTaskName = getTestTaskName(testSetBaseName)
        final String manifestTaskName = getManifestTaskName(testSetBaseName)

        Task genTask = project.tasks.create(genTaskName,TestGenerator)
        Task testTask = project.tasks.create(testTaskName, GradleTest)

        genTask.with {
            group = 'build'
            description = 'Creates text fixtures for compatibility testing'
        }

        testTask.with {
            group = Names.TASK_GROUP
            description = 'Runs Gradle compatibility tests'
            dependsOn genTask, compileTaskName, manifestTaskName
            mustRunAfter 'test'
        }

        project.tasks.getByName('check').dependsOn testTaskName
    }

    /** Creates a Spock Framework dependency
     *
     * @param project
     * @return
     */
    @PackageScope
    @CompileDynamic
    static Dependency spockDependency(Project project) {
        project.dependencies.create("org.spockframework:spock-core:${SPOCK_VERSION}") {
            exclude module : 'groovy-all'
        }
    }

    final static String SPOCK_VERSION      = "1.0-groovy-${GroovySystem.version.replaceAll(/\.\d+$/,'')}"
    final static String JUNIT_VERSION      = '4.12'
    final static String HAMCREST_VERSION   = '1.3'
}
