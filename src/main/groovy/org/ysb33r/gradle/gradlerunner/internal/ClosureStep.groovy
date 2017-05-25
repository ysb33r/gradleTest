package org.ysb33r.gradle.gradlerunner.internal

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.ysb33r.gradle.gradlerunner.Step
import org.ysb33r.gradle.gradlerunner.StepInfo

/** Using a closure as a step
 *
 * @since 1.0
 */
@CompileStatic
class ClosureStep extends AbstractStep {

    ClosureStep( final String name, Closure action) {
        super(name)
        if( action.parameterTypes.size() != 1 && !(action.parameterTypes[0] instanceof StepInfo) ) {
            throw new GradleException( " Closure needs to take a StepInfo as parameter")
        }

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
        action.call(this)
    }

    Closure action
}
