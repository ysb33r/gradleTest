package org.ysb33r.gradle.gradlerunner

import org.gradle.api.Plugin
import org.gradle.api.Project

/** Adds a default task called {@code gradleRunner}
 *
 * @since 1.0
 */
class GradleRunnerPlugin implements Plugin<Project> {
    static final String DEFAULT_TASK = 'gradleRunner'

    @Override
    void apply(Project project) {
        project.tasks.create DEFAULT_TASK, GradleRunnerSteps
    }
}
