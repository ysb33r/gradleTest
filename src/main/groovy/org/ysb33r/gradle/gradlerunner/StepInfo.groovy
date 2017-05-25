package org.ysb33r.gradle.gradlerunner

import groovy.transform.CompileStatic

/** Information requires to execute a step
 *
 * @since 1.0
 */
@CompileStatic
interface StepInfo {

    /** Name of the step
     *
     * @return Name as a string
     */
    String getName()

    /** Directory where reports should be written to
     *
     * @return Directory unique to this step. Can be null.
     */
    File getReportDir()

    /** Base directory where work occurs in
     *
     * @return Directory. Can be null.
     */
    File getWorkingDir()
}