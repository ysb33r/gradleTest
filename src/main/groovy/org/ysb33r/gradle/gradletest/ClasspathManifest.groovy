package org.ysb33r.gradle.gradletest

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

/** Generates a manifest file that can be re-used by @link GradleTest tasks to set the correct classpath
 * for {@code GradleRunner}.
 *
 * @since 1.0
 */
@CompileStatic
class ClasspathManifest extends DefaultTask {

    /** Name of the test task this is linked to.
     * Under normal circumstances this property should not be modified by a build script author
     */
    String linkedTestTaskName = name.replaceAll(Names.MANIFEST_TASK_POSTFIX,'')

    ClasspathManifest() {
        outputDir = TestSet.getManifestDir(project,linkedTestTaskName)
    }

    /** Returns directory where mnaifest file will be written to
     *
     * @return
     */
    @OutputDirectory
    File getOutputDir() {
        project.file(this.outputDir)
    }

    /** The name of the manifest file.
     *
     * @return Filename as a string.
     */
    String getOutputFilename() {
        linkedTestTaskName + '-manifest.txt'
    }

    /** Returns the runtime classpath associated with this manifest
     *
     * @return Runtime classpath
     */
    @InputFiles
    FileCollection getRuntimeClasspath() {
        sourceSet.runtimeClasspath
    }

    @TaskAction
    void exec() {
        File dest = getOutputDir()
        dest.mkdirs()
        project.file("${dest}/${outputFilename}").text = runtimeClasspath.join("\n")
    }

    private Object outputDir

    @CompileDynamic
    private SourceSet getSourceSet() {
        (project.sourceSets as SourceSetContainer).getByName(linkedTestTaskName)
    }
}
