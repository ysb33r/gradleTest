package org.ysb33r.gradle.gradlerunner.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.ysb33r.gradle.gradlerunner.GradleRunnerPlugin

import java.lang.reflect.Method

/** Provides a private project extension for loading GradleRunner instances.
 *
 * @since 1.0
 */
@CompileStatic
class GradleRunnerFactory {

    GradleRunnerFactory(Project project) {
        URL[] urls = project.configurations.getByName(GradleRunnerPlugin.CLASSPATH_CONFIGURATION).files.collect { File f ->
            f.absoluteFile.toURI().toURL()
        } as URL[]
        URLClassLoader loader = URLClassLoader.newInstance(urls,this.getClass().classLoader)
        this.gradleRunner = loader.loadClass('org.gradle.testkit.runner.GradleRunner')
    }

    def createRunner() {
        Method createMethod = gradleRunner.getDeclaredMethod('create')
        createMethod.invoke(gradleRunner)
    }

    private Class gradleRunner
}
