package org.ysb33r.gradle.gradlerunner

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

/** Adds a default task called {@code gradleRunner}
 *
 * @since 1.0
 */
class GradleRunnerPlugin implements Plugin<Project> {
    static final String DEFAULT_TASK = 'gradleRunner'

    @Override
    void apply(Project project) {

        if (GradleVersion.current() < GradleVersion.version('2.14.1')) {
            throw new GradleException('This plugin requires a minimum version of 2.14.1')
        }
        
        project.tasks.create DEFAULT_TASK, GradleRunnerSteps
    }
}
