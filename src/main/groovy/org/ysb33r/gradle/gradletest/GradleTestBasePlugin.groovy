package org.ysb33r.gradle.gradletest

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

/** A base plugin for GradleTest. It provides only the ability to add new test sets,
 * but does not create any.
 *
 */
@CompileStatic
class GradleTestBasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if(GradleVersion.current() < GradleVersion.version('2.13')) {
            throw new GradleException('This plugin is only compatible with Gradle 2.13+')
        }
        project.apply plugin : 'groovy'
        addTestKitTriggers(project)
    }

    /** Adds the appropriate task triggers.
     */
    @CompileDynamic
    private void addTestKitTriggers(Project project) {
        project.ext {
            additionalGradleTestSet = { String name ->
                TestSet.addTestSet(project, name)
            }
        }
    }
}
