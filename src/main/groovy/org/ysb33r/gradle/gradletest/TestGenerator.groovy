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

import groovy.io.FileType
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.os.OperatingSystem

/** Generates test files that will be compiled against GradleTestKit.
 *
 * @since 1.0
 */
@CompileStatic
class TestGenerator extends DefaultTask {

    TestGenerator() {
        onlyIf { getTestRootDirectory().exists() }
    }

    /** Name of the test task this is linked to.
     * Under normal circumstances this property should not be modified by a build script author
     */
    String linkedTestTaskName = name.replaceAll(Names.GENERATOR_TASK_POSTFIX,'')

    /** Name of the package tests will be placed within.
     * Under normal circumstances this property should not be modified by a build script author
     */
    String testPackageName = "${linkedTestTaskName}.tests"

    /** The Gradle versions that test code will be generated for
     *
     * @return List of (valid) Gradle versions
     */
    @Input
    Set<String> getVersions() {
        linkedTask.versions
    }

    /** List of arguments that needs to be passed to TestKit.
     *
     * @return List of arguments in order as passed to linked @link #GradleTest task.
     */
    @Input
    List<String> getGradleArguments() {
        linkedTask.gradleArguments
    }

    /** The default task to be executed.
     *
     * @return Default task as obtained from linked @link #GradleTest task.
     */
    @Input
    String getDefaultTask() {
        linkedTask.defaultTask
    }

    /** The root directory where to find tests for this specific GradleTest grouping
     * The default root directory by convention is {@code src/gradleTest}. THe patterns for the
     * directory is {@code src/} + {@code gradleTestSetName}.
     *
     * @return The directory as a file object resolved as per {@code project.file()}.
     */
    @InputDirectory
    File getTestRootDirectory() {
        project.file("${project.projectDir}/src/${linkedTestTaskName}")
    }

    /** A map of the tests found in the appropriate GradleTest directory.
     * The default root directory by convention. See @link #getTestRootDirectory().
     * Locates all fodler below the root which contains a {@code build.gradle} file -
     * other folders are ignored.
     *
     * @return A map of consisting of {@code <TestName,PathToTest>}.
     */
    @Input
    TreeMap<String,File> getTestMap() {
        TreeMap<String,File> derivedTestNames = [:] as TreeMap
        final File root = getTestRootDirectory()
        if(root.exists()) {
            root.eachFile( FileType.DIRECTORIES) { File dir ->
                if(project.file("${dir}/build.gradle").exists()) {
                    derivedTestNames[dir.name] = dir.canonicalFile
                }
            }
        }
        derivedTestNames
    }

    /** Where generated source is written to.
     *
     * @return Output directory.
     */
    @OutputDirectory
    File getOutputDir() {
        sourceDirectorySet.srcDirs.first()
    }

    /** The directory where the plugin JAR is to be found. By default it will be {@code jar.destinationDir}
     *
     * @return Plugin directory.
     */
    File getPluginJarDirectory() {
        project.file(pluginJarDir)
    }

    /** Overrides the directory where the plugin JAR is to be found in.
     *
     * @param dir Sets a new location
     * @return
     */
    void setPluginJarDirectory(Object dir) {
        pluginJarDir = dir
    }

    /** Distribution URI to use when looking for Gradle distributions
     *
     * @return Distribution URI or null (indicating to use official Gradle repository).
     */
    @Input
    @Optional
    URI getGradleDistributionUri() {
        linkedTask.gradleDistributionUri
    }

    /** Task action will generate as per testnames returned by @Link #etTestMap().
     *
     */
    @TaskAction
    void exec() {
        if(!templateFile) {
            setTemplateLocationFromResource()
        }

        if(!templateInitScript) {
            setInitScriptLocationFromResource()
        }

        final ClasspathManifest manifestTask = project.tasks.getByName(TestSet.getManifestTaskName(linkedTestTaskName)) as ClasspathManifest
        final File manifestDir = manifestTask.outputDir
        final File manifestFile = new File("${manifestDir}/${manifestTask.outputFilename}")
        final File workDir = project.file("${project.buildDir}/${linkedTestTaskName}")
        final File repoDir = new File(workDir,'repo')

        createInitScript(workDir,pluginJarDirectory,repoDir)

        testMap.each { String testName,File testLocation ->

            copy(
                outputDir,
                testName,
                defaultTask,
                manifestFile,
                workDir,
                testLocation,
                gradleArguments
            )
        }

        createRepo(repoDir)
    }

    /** Created a local repo for use by tests.
     *
     * @param repoDir Where to copy files to
     */
    @CompileDynamic
    private void createRepo(final File repoDir) {
        project.copy {
            from project.configurations.getByName(linkedTestTaskName)
            into repoDir
        }
    }

    /** Creates a init script to be used for running tests
     *
     * @param targetDir Where the init script is copied to
     * @param jarDir THe path where the plugin JAR will be found
     * @param repoDir The path to where the local repo will be created
     */
    @CompileDynamic
    private void createInitScript(final File targetDir,final File jarDir,final File repoDir) {
        final def fromSource = templateInitScript
        final String pluginJarPath = pathAsUriStr(jarDir)
        final String repoPath = pathAsUriStr(repoDir)
        project.copy {
            from fromSource
            into targetDir
            expand PLUGINJARPATH: pluginJarPath,
                LOCALREPOPATH :  repoPath
        }
    }

    /** Get linked test as a @link #GradleTest object.
     *
     * @return @link #GradleTest object.
     */
    private GradleTest getLinkedTask() {
        ((GradleTest)project.tasks.getByName(linkedTestTaskName))
    }

    /** Finds the Groovy source directory set associated with this GradleTest set.
     *
     * @return {@code SourceDirectorySet}
     */
    @CompileDynamic
    private SourceDirectorySet getSourceDirectorySet() {
        (project.sourceSets as SourceSetContainer).getByName(linkedTestTaskName).getGroovy()
    }

    /** Generates one source file
     *
     * @param targetDir Directory to generate Spock file into.
     * @param testName Name of the test
     * @param defaultTask Default task to execute
     * @param testProjectSrcDir Directory where test project is located
     * @param arguments Arguments that will be passed during a run.
     */
    @CompileDynamic
    private void copy(
        final File targetDir,
        final String testName,
        final String defaultTask,
        final File manifestFile,
        final File workDir,
        final File testProjectSrcDir,
        final List<String> arguments
    ) {

        final def fromSource = templateFile
        final String verText = quoteAndJoin(versions)
        final String argsText = quoteAndJoin([defaultTask] + arguments)
        final String manifest = pathAsUriStr(manifestFile)
        final String work = pathAsUriStr(workDir)
        final String src = pathAsUriStr(testProjectSrcDir)

        project.copy {
            from fromSource
            into targetDir
            rename ~/.+/,"${testName.capitalize()}CompatibilitySpec.groovy"
            expand TESTPACKAGE : testPackageName,
                TESTNAME : testName.capitalize(),
                MANIFEST : manifest,
                ARGUMENTS : argsText,
                DEFAULTTASK : defaultTask,
                VERSIONS : verText,
                DISTRIBUTION_URI : gradleDistributionUri ?: '',
                WORKDIR : work,
                SOURCEDIR : src
        }
    }

    /** Quotes items in non-interpolated strings amd then joins them as a comma-separated list
     *
     * @param c Any container
     * @return Comma-separated list
     */
    @CompileDynamic
    private String quoteAndJoin(Iterable c) {
        c.collect { "'${it}'" }.join(',')
    }

    /** Ensures that files are represented as URIs.
     * (This helps with compatibility across operating systems).
     * @param path Path to output
     */
    private String pathAsUriStr(final File path) {
        path.absoluteFile.toURI().toString()
    }

    /** Finds the template in the classpath.
     *
     */
    private void setTemplateLocationFromResource() {
        this.templateFile = getLocationFromResource(TEST_TEMPLATE_PATH)
    }

    /** Finds the init script template in the classpath.
     *
     */
    private void setInitScriptLocationFromResource() {
        this.templateInitScript = getLocationFromResource(INIT_TEMPLATE_PATH)
    }

    private def getLocationFromResource(final String resourcePath) {
        Enumeration<URL> enumResources
        String resourceName = new File(resourcePath).name
        enumResources = this.class.classLoader.getResources( resourcePath )
        if(!enumResources.hasMoreElements()) {
            throw new GradleException ("Cannot find ${resourcePath} in classpath")
        }

        URI uri = enumResources.nextElement().toURI()
        String location = uri.getSchemeSpecificPart().replace('!/'+resourcePath,'')
        if(uri.scheme.startsWith('jar')) {
            location=location.replace('jar:file:','')
            return project.zipTree(location).filter { File it -> it.name == resourceName }
        } else if(uri.scheme.startsWith('file')) {
            return  location.replace('file:','')
        }

        throw new GradleException("Cannot extract ${uri}")
    }

    private def pluginJarDir = {
        Jar task = project.tasks.getByName('jar') as Jar
        task.destinationDir
    }

    private def templateFile
    private def templateInitScript
    static final String TEST_TEMPLATE_PATH = 'org/ysb33r/gradletest/GradleTestTemplate.groovy.template'
    static final String INIT_TEMPLATE_PATH = 'org/ysb33r/gradletest/init.gradle'
}
