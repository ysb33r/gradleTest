package org.ysb33r.gradle.gradlerunner

import groovy.transform.CompileStatic

/** A step to be executed.
 *
 * @since 1.0
 */
@CompileStatic
interface Step {

    /** Name of this step.
     *
     * @return Name.
     */
    String getName()

    /** Executes a step
     *
     * @param workingDir Working directory for step
     * @param reportsDir Directory whether reports should be written to.
     */
    void execute( final File workingDir, final File reportsDir )

    /** Information that was used during execution of the step
     *
     * @return Step information.
     */
    StepInfo getStepInfo()
}