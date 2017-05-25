package org.ysb33r.gradle.gradlerunner.internal

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.ysb33r.gradle.gradlerunner.Step
import org.ysb33r.gradle.gradlerunner.StepInfo

/** Using an {@code Action} as a step
 *
 * @since 1.0
 */
@CompileStatic
class ActionStep extends AbstractStep {

    ActionStep(final String name, Action<StepInfo> action) {
        super(name)
        this.action = action
    }

    /** Executes the closure, passing a StepInfo
     *
     * @param workingDir Working directory for step
     * @param reportsDir Directory whether reports should be written to.
     */
    @Override
    void execute(File workingDir, File reportsDir) {
        createDirs(workingDir,reportsDir)
        action.execute(this)
    }

    private Action<StepInfo> action
}
