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

import groovy.io.FileType
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar


import java.util.regex.Pattern

/** Generates test files that will be compiled against GradleTestKit.
 *
 * @since 1.0
 */
@CompileStatic
class TestGenerator extends DefaultTask {

    final static String GROOVY_BUILD_SCRIPT = 'build.gradle'
    final static String KOTLIN_BUILD_SCRIPT = 'build.gradle.kts'
    final static String GROOVY_TEST_POSTFIX = 'GroovyDSL'
    final static String KOTLIN_TEST_POSTFIX = 'KotlinDSL'

    final static String GROOVY_BUILD_EXTENSION = "gradle"
    final static String KOTLIN_BUILD_EXTENSION = "gradle.kts"

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

    /** Whether to treat Gradle's deprecation messages as failures.
     *
     * @return {@code true} if deprecation messages should fail the tests.
     */
    @Input
    boolean getDeprecationMessageAreFailures() {
        linkedTask.getDeprecationMessagesAreFailures()
    }

    /** Whether to add tests for Kotlin scripts if they are available.
     *
     * @return {@code true} if Kotlin scripts should be tested.
     */
    @Input
    boolean getKotlinDsl() {
        linkedTask.kotlinDsl
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
     * Locates all folder below the root which contains a {@code build.gradle} file -
     * other folders are ignored. If {@code getKotlinDsl()} returns {@code true}, then
     * folders containing {@code build.gradle.kts} will also be included
     *
     * @return A map of consisting of {@code <TestName,PathToTest>}.
     */
    @Input
    TreeMap<String,TestDefinition> getTestMap() {
        TreeMap<String,TestDefinition> derivedTestNames = [:] as TreeMap
        final File root = getTestRootDirectory()
        if(root.exists()) {

            // define the file name filter to find one or more build files
            def groovyFilter = new FilenameFilter() {
                boolean accept(File path, String filename) {
                    return filename.endsWith(GROOVY_BUILD_EXTENSION)
                }
            }

            def kotlinFilter = new FilenameFilter() {
                boolean accept(File path, String filename) {
                    return filename.endsWith(KOTLIN_BUILD_EXTENSION)
                }
            }

            root.eachFile( FileType.DIRECTORIES) { File dir ->
                List<File> groovyBuildFiles =  dir.listFiles(groovyFilter) as List
                List<File> kotlinBuildFiles =  dir.listFiles(kotlinFilter) as List

                if ((groovyBuildFiles.size() > 0) || (kotlinBuildFiles.size() > 0)) {
                    derivedTestNames[dir.name] = new TestDefinition(dir.canonicalFile, groovyBuildFiles, kotlinBuildFiles)
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

    /** Task action will generate testsas per the testnames returned by @Link #etTestMap().
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
        final List<Pattern> testPatternsForFailures = getLinkedTask().getExpectedFailures()
        final boolean deprecation = getDeprecationMessageAreFailures()

        Set<File> externalDependencies = []
        try {
            DomainObjectSet<ExternalModuleDependency> externalDependencySet = project.configurations.getByName('runtime').allDependencies.withType(ExternalModuleDependency)
            DomainObjectSet<ProjectDependency> projectDependencySet = project.configurations.getByName('runtime').allDependencies.withType(ProjectDependency)
            externalDependencies = project.configurations.getByName('runtime').files { dep ->
                externalDependencySet.contains(dep) || projectDependencySet.contains(dep)
            }
        } catch(UnknownConfigurationException e) {}

        createInitScript(workDir,pluginJarDirectory,repoDir,externalDependencies)

        // Before generation delete existing generated code
        outputDir.deleteDir()

        testMap.each { String testName,TestDefinition testDef ->

            boolean expectFailure = testPatternsForFailures.find { pat ->
                testName =~ pat
            }

            copy(
                outputDir,
                testName,
                defaultTask,
                manifestFile,
                workDir,
                testDef,
                gradleArguments,
                expectFailure,
                deprecation
            )
        }

        createRepo(repoDir)
    }

    /** Created a local repo for use by tests.
     * it will copy from the appropriate configuration i.e. {@code gradleTest}, {@code fooGradleTest}} etc.
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
     * @param jarDir The path where the plugin JAR will be found
     * @param repoDir The path to where the local repo will be created
     * @param externalDependencies A list of external dependencies (JARs).
     */
    @CompileDynamic
    private void createInitScript(final File targetDir,final File jarDir,final File repoDir,Set<File> externalDependencies) {
        final def fromSource = templateInitScript
        final String pluginJarPath = pathAsUriStr(jarDir)
        final String repoPath = pathAsUriStr(repoDir)
        final String externalDepPaths = externalDependencies.collect {
            "'${pathAsUriStr(it)}'.toURI()"
        }.join(',')

        project.copy {
            from fromSource
            into targetDir
            expand PLUGINJARPATH: pluginJarPath,
                LOCALREPOPATH :  repoPath,
                EXTERNAL_DEP_URILIST : externalDepPaths
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

    /** Processes the groovy and kotlin arrays to generate the spock files
     *
     * @param targetDir Directory to generate Spock file into.
     * @param testName Name of the test
     * @param defaultTask Default task to execute
     * @param manifestFile Where to locate the classpath manifest
     * @param workDir Directory where run artifacts will be written to
     * @param testProjectSrcDir Directory where test project is located
     * @param arguments Arguments that will be passed during a run.
     * @param willFail Set to {@code true} if the Gradle script under test is expected to fail.
     * @param deprecationMessageMode Set to {@code true} to force Gradle's deprecation messages to fail the test.
     */
    @CompileDynamic
    private void copy(
            final File targetDir,
            final String testBase,
            final String defaultTask,
            final File manifestFile,
            final File workDir,
            final TestDefinition testDefinitions,
            final List<String> arguments,
            final boolean willFail,
            final boolean deprecationMessageMode
    ) {
        for (File gfile in testDefinitions.groovyBuildFiles){
            String testName = testBase + gfile.name.split("\\.")[0] + GROOVY_TEST_POSTFIX
            List<String> workerArguments = arguments
            workerArguments.addAll(["--build-file", gfile.name])

            copyWorker(
                    targetDir,
                    testName,
                    defaultTask,
                    manifestFile,
                    workDir,
                    testDefinitions.testDir,
                    workerArguments,
                    willFail,
                    deprecationMessageMode
            )
        }

        for (File kFile in testDefinitions.kotlinBuildFiles){
            String testName = testBase + kFile.name.split("\\.")[0] + KOTLIN_TEST_POSTFIX
            List<String> workerArguments = arguments
            workerArguments.addAll(["--build-file", kFile.name])

            copyWorker(
                    targetDir,
                    testName,
                    defaultTask,
                    manifestFile,
                    workDir,
                    testDefinitions.testDir,
                    workerArguments,
                    willFail,
                    deprecationMessageMode
            )
        }
    }

    /** Generates one source file
     *
     * @param targetDir Directory to generate Spock file into.
     * @param testName Name of the test
     * @param defaultTask Default task to execute
     * @param manifestFile Where to locate the classpath manifest
     * @param workDir Directory where run artifacts will be written to
     * @param testProjectSrcDir Directory where test project is located
     * @param arguments Arguments that will be passed during a run.
     * @param willFail Set to {@code true} if the Gradle script under test is expected to fail.
     * @param deprecationMessageMode Set to {@code true} to force Gradle's deprecation messages to fail the test.
     */
    @CompileDynamic
    private void copyWorker(
        final File targetDir,
        final String testName,
        final String defaultTask,
        final File manifestFile,
        final File workDir,
        final File testProjectSrcDir,
        final List<String> arguments,
        final boolean willFail,
        final boolean deprecationMessageMode
    ) {

        final boolean isKotlinTest = testName.endsWith(KOTLIN_TEST_POSTFIX)

        final def fromSource = templateFile
        final String verText = quoteAndJoin(isKotlinTest ? kotlinDslSafeVersions() : versions)
        final String argsText = quoteAndJoin([defaultTask] + arguments)
        final String manifest = pathAsUriStr(manifestFile)
        final String work = pathAsUriStr(workDir)
        final String src = pathAsUriStr(testProjectSrcDir)
        final String deprecation = deprecationMessageMode.toString()
        final String deleteScript = isKotlinTest ? GROOVY_BUILD_SCRIPT : KOTLIN_BUILD_SCRIPT

        if(verText.empty) {
            logger.warn("Kotlin DSL testing has been enabled, but none of the specified Gradle versions are 4.0 or later.")
            return
        }

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
                SOURCEDIR : src,
                FAILMODE : willFail,
                CHECK_WARNINGS : deprecation,
                DELETE_SCRIPT : deleteScript
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

    /** Returns versions which are safe for Kotlin testing
     *
     * @return versions for which Kotlin DSL can be tested.
     */
    private Iterable<String> kotlinDslSafeVersions() {
        versions.findAll {
            !it.startsWith( '2.') && !it.startsWith( '3.' )
        }
    }

    /** Ensures that files are represented as URIs.
     * (This helps with compatibility across operating systems).
     * @param path Path to output
     */
    String pathAsUriStr(final File path) {
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
