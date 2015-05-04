package org.ysb33r.gradle.gradletest

/**
 * @author Schalk W. Cronjé
 */
interface CompatibilityTestResult {
    String getTestName()
    String getGradleVersion()
    boolean getPassed()
}