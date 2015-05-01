package org.ysb33r.gradle.gradletest

/**
 * @author Schalk W. Cronjé
 */
interface Distribution extends Comparable {
    String getVersion()
    File getLocation()
}