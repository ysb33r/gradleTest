package org.ysb33r.gradle.gradlerunner.internal

import groovy.transform.CompileStatic
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.gradle.gradlerunner.Step
import org.ysb33r.gradle.gradlerunner.StepInfo

/** Runs a Gradle execution.
 *
 * @since 1.0
 */
@CompileStatic
class GradleStep extends AbstractStep {

    GradleStep(final String name, final List<String> args,boolean expectFailure) {
        super(name)
        this.args = args
        this.expectFailure = expectFailure
    }

    GradleStep(final String name,final Iterable<String> args,boolean expectFailure) {
        super(name)
        this.args = []
        this.args.addAll(args)
        this.expectFailure = expectFailure
    }

    @Override
    void execute(File projectDir, File reportDir) {
        createDirs(projectDir,reportDir)
        this.outputFile = new File(reportDir,'out.txt')
        this.errorFile = new File(reportDir,'err.txt')
        run( projectDir, this.outputFile, this.errorFile)
    }

    /** Location of output from execution.
     *
     * @return Output file. Null if step has not executed yet.
     */
    File getOutput() {
        this.outputFile
    }

    /** Location of error output from execution.
     *
     * @return Error file. Null if step has not executed yet.
     */
    File getError() {
        this.errorFile
    }

    private BuildResult run(final File projDir, final File outFile,final  File errFile) {
        BuildResult result
        GradleRunner runner = GradleRunner.create().withArguments(this.args).withProjectDir(projDir)
        outFile.withPrintWriter { PrintWriter out ->
            errFile.withPrintWriter { PrintWriter err ->
                runner.forwardStdError(err).forwardStdOutput(out)
                result = this.expectFailure ? runner.buildAndFail() : runner.build()
            }
        }
        return result
    }

    private File outputFile
    private File errorFile

    private final List<String> args
    private boolean expectFailure

}
