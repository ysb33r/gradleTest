package org.ysb33r.gradle.gradletest

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.internal.Distribution as InternalDistribution

/**
 * @author Schalk W. Cronjé
 */
class GradleTestPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.with {
            configurations.maybeCreate  Names.CONFIGURATION
            configurations.maybeCreate  Names.DEFAULT_TASK

            def gradleExt = extensions.create Names.EXTENSION, GradleTestExtension, project
            tasks.create Names.DEFAULT_TASK, GradleTest

            afterEvaluate { Project p ->
                InternalDistribution.searchLocal(gradleExt)

                // Add
            }
        }
    }
}
