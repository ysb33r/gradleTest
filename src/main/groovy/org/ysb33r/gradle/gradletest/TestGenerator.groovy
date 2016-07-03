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
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class TestGenerator extends DefaultTask {

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
        getTestRootDirectory().eachFile( FileType.DIRECTORIES) { File dir ->
            if(project.file("${dir}/build.gradle").exists()) {
                derivedTestNames[dir.name] = dir.canonicalFile
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

    /** Distribution URI to use when looking for Gradle distributions
     *
     * @return Distribution URI or null (indicating to use official GRadle repository).
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

        final ClasspathManifest manifestTask = project.tasks.getByName(TestSet.getManifestTaskName(linkedTestTaskName)) as ClasspathManifest
        File manifestDir = manifestTask.outputDir
        final String manifestFile = new File("${manifestDir}/${manifestTask.outputFilename}").absolutePath

        testMap.each { String testName,File testLocation ->

            copy(
                outputDir,
                testName,
                defaultTask,
                manifestFile,
                testLocation,
                gradleArguments
            )
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
        final String manifestName,
        final File testProjectSrcDir,
        final List<String> arguments
    ) {

        final def fromSource = templateFile
        final String verText = quoteAndJoin(versions)
        final String argsText = quoteAndJoin([defaultTask] + arguments)

        project.copy {
            from fromSource
            into targetDir
            rename ~/.+/,"${testName.capitalize()}CompatibilitySpec.groovy"
            expand TESTPACKAGE : testPackageName,
                TESTNAME : testName.capitalize(),
                MANIFEST : manifestName,
                ARGUMENTS : argsText,
                DEFAULTTASK : defaultTask,
                VERSIONS : verText,
                DISTRIBUTION_URI : gradleDistributionUri ?: '',
                SOURCEDIR : testProjectSrcDir.absolutePath
        }
    }

    /** Quotes items in non-interpoalted strings amd then joins them as a comma-separated list
     *
     * @param c Any container
     * @return Comma-separated list
     */
    @CompileDynamic
    private String quoteAndJoin(Iterable c) {
        c.collect { "'${it}'" }.join(',')
    }

    /** Finds the template in the classpath.
     *
     */
    private void setTemplateLocationFromResource() {
        Enumeration<URL> enumResources
        enumResources = this.class.classLoader.getResources( TEST_TEMPLATE_PATH )
        if(!enumResources.hasMoreElements()) {
            throw new GradleException ("Cannot find ${TEST_TEMPLATE_PATH} in classpath")
        } else {
            URI uri = enumResources.nextElement().toURI()
            String location = uri.getSchemeSpecificPart().replace('!/'+TEST_TEMPLATE_PATH,'')
            if(uri.scheme.startsWith('jar')) {
                location=location.replace('jar:file:','')
                this.templateFile= project.zipTree(location).filter { File it -> it.name == 'GradleTestTemplate.groovy.template'}
            } else if(uri.scheme.startsWith('file')) {
                this.templateFile= location.replace('file:','')
            } else {
                throw new GradleException("Cannot extract ${uri}")
            }
        }
    }

    private def templateFile
    static final String TEST_TEMPLATE_PATH = 'org/ysb33r/gradletest/GradleTestTemplate.groovy.template'
}
