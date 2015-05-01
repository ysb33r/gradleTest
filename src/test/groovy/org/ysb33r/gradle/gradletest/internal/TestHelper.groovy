package org.ysb33r.gradle.gradletest.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.gradle.testfixtures.ProjectBuilder

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class TestHelper {
    static final String TESTROOT = new File( System.getProperty('TESTROOT') ?: '.').absoluteFile

    static Project buildProject(final String testName) {
        File pDir = new File(TESTROOT,testName)
        if(pDir.exists()) {
            pDir.deleteDir()
        }
        pDir.mkdirs()
        ProjectBuilder.builder().withName(testName).withProjectDir(pDir).build()
    }

}
