package org.ysb33r.gradle.gradlerunner.internal

import org.ysb33r.gradle.gradlerunner.Step
import org.ysb33r.gradle.gradlerunner.StepInfo

/** Abstract base class for implementing steps
 *
 * @since 1.0
 */
abstract class AbstractStep implements Step, StepInfo {

    /** Information that was used during execution of the step
     *
     * @return Step information.
     */
    @Override
    StepInfo getStepInfo() {
        this
    }

    /** Directory where reports should be written to
     *
     * @return Directory unique to this step
     */
    @Override
    File getReportDir() {
        this.reportDir
    }

    /** Base directory where work occurs in
     *
     * @return Directory/ Null if action
     */
    @Override
    File getWorkingDir() {
        this.workingDir
    }

    /** Name of this step.
     *
     * @return Name.
     */
    @Override
    String getName() {
        this.name
    }


    /** Set working directory for this step.
     *
     * @param wd Working directory
     */
    protected void setWorkingDir(File wd) {
        this.workingDir = wd
    }

    /** Set unique reporting directory for this step.
     *
     * @param rd Reporting directory
     */
    protected void setReportDir(File rd) {
        this.reportDir = rd
    }

    protected void createDirs(File workingDir, File reportsDir) {
        this.workingDir = workingDir
        this.reportDir = reportsDir
        this.workingDir.mkdirs()
        this.reportDir.mkdirs()
    }

    protected AbstractStep(final String name) {
        this.name = name
    }

    final String name
    File reportDir
    File workingDir
}
