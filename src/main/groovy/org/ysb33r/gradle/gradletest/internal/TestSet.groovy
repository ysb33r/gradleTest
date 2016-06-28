package org.ysb33r.gradle.gradletest.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.ysb33r.gradle.gradletest.GradleTest
import org.ysb33r.gradle.gradletest.Names
import org.ysb33r.gradle.gradletest.TestGenerator

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

        addConfigurations(project,configNames)
        addTestTasks(project,setname)
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
        [ compile : "${name}Compile",
          runtime : "${name}Runtime"
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
        { Project p,String setname ->
            "${p.buildDir}/${setname}/src"
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

    /** Adds the requires configurations
     *
     * @param Project
     * @param configNames A map of configuration names as created by a call to @link #configurationNames
     */
    @PackageScope
    static void addConfigurations(Project project, final Map<String,String> configNames) {
        configNames.each { String key,String val ->
            project.configurations.create(val).extendsFrom(project.configurations.getByName(key))
        }
    }

    /** Adds the required test dependencies to a configuration
     *
     * @param project
     * @param configurationName
     */
    @PackageScope
    static void addTestDependencies(Project project,final String configurationName) {
        project.dependencies.with {
            add configurationName,spockDependency(project)
            add configurationName,gradleTestKit()
        }
    }

    @CompileDynamic
    @PackageScope
    static void addSourceSets(Project project,final String testSetBaseName,final Map<String,String> configNames) {

        final String genTaskName = getGeneratorTaskName(testSetBaseName)
        final String compileTaskName = getCompileTaskName(testSetBaseName)

        project.afterEvaluate {
            project.sourceSets.create testSetBaseName, {
                groovy.srcDirs = [project.file(getSourceDir(project,testSetBaseName))]
                resources.srcDirs = [project.file( getResourcesDir(project,testSetBaseName) )]
                compileClasspath = project.configurations.getByName(configNames['compile'])
                runtimeClasspath = output + compileClasspath +
                    project.configurations.getByName(configNames['runtime'])
            }

            project.tasks.getByName(compileTaskName).dependsOn(genTaskName)
        }
    }

    @PackageScope
    static void addTestTasks(Project project,final String testSetBaseName) {

        final String genTaskName = getGeneratorTaskName(testSetBaseName)
        final String compileTaskName = getCompileTaskName(testSetBaseName)
        final String testTaskName = getTestTaskName(testSetBaseName)

        Task genTask = project.tasks.create(genTaskName,TestGenerator)
        Task testTask = project.tasks.create(testTaskName, GradleTest)

        genTask.with {
            group = 'build'
            description = 'Creates text fixtures for compatibility testing'
        }

        testTask.with {
            group = Names.TASK_GROUP
            description = 'Runs Gradle compatibility tests'
            dependsOn genTask, compileTaskName
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
        project.dependencies.create("org.spockframework:spock-core:${SPOCKVERSION}") {
            exclude module : 'groovy-all'
        }
    }

    final static String SPOCKVERSION = "1.0-${GroovySystem.version.replaceAll(/\.\d+$/,'')}"
}
