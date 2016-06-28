package org.ysb33r.gradle.gradletest

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.testing.Test
import org.gradle.util.CollectionUtils
import org.gradle.util.GradleVersion

/**
 * @author Schalk W. Cronjé
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
        CollectionUtils.stringize(this.versions) as Set<String>
    }

    /** Add Gradle versions to be tested against.
     *
     * @param ver List of versions
     */
    void versions(String... ver) {
        this.versions += (ver as List)
    }

    /** Append additional arguments to be sent to the runnign GradleTest instance.
     *
     * @param args Additional arguments
     */
     void setGradleArguments(Object... args ) {
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

    /** Returns the arguments that needs to be passed to the running GradleTest instance
     *
     * @return List of arguments in order
     */
    @Input
    List<String> getGradleArguments() {
        CollectionUtils.stringize(this.arguments) as List<String>
        /* Do we need any of these?
            args '--project-cache-dir',"${testProjectDir}/.gradle"
            args '--gradle-user-home',  "${testProjectDir}/../home"
            args '--init-script',initScript.absolutePath

        */
    }

    @Input
    String getDefaultTask() {
        'runGradleTest'
    }

    private List<Object> arguments = ['--no-daemon','--full-stacktrace','--info'] as List<Object>
    private List<Object> versions = []
}

