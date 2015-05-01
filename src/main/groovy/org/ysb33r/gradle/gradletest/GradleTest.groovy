package org.ysb33r.gradle.gradletest

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

/**
 * @author Schalk W. Cronjé
 */
class GradleTest extends SourceTask {

    @Input
    @SkipWhenEmpty
    List<String> getVersions() {
        CollectionUtils.stringize(this.versions)
    }

    void versions(Object... v) {
        this.versions += (v as List).flatten()
    }

    @InputDirectory
    File getSourceDir() {
        project.file("${project.projectDir}/src/${name}")
    }

    @OutputDirectories
    Set<File> getOutputDirs() {
        versions.collect {
            new File(project.buildDir,"${name}/${it}")
        } as Set<File>
    }

    @TaskAction
    void exec() {
        // For each version, check whether it's available
        // For missing versions, if --offline, skip otherwise download (fail if not available).
        // Copy all files
    }

    private GradleTestExtension getConfig() {
        project.extensions.findByName(Names.EXTENSION)
    }

    private List<Object> versions

//    private void copyFiles(final File src, final Set<File> files) {
//        files.each { dest ->
//            project.copy {
//                from src
//                into dest
//            }
//            /
//            dest.ea
//        }
//
//    }
}
