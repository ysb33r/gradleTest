package org.ysb33r.gradle.gradletest.internal

import groovy.transform.CompileStatic
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.ysb33r.gradle.gradletest.AvailableDistributions
import org.ysb33r.gradle.gradletest.Distribution
import org.ysb33r.gradle.gradletest.GradleTestExtension
import org.ysb33r.gradle.gradletest.Names

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AvailableDistributionsInternal implements AvailableDistributions {

//    /** Gets a list of versions that are available locally
//     *
//     * @return List of versions
//     */
//    @Override
//    Set<String> getLocalVersions() {
//        return null
//    }

    /** Returns all distributions that either found locally or downloaded and unpacked
     *
     * @return A map with the version as key and the location as value.
     */
    @Override
    Map<String, File> getAllDistributionsAsMap() {
        distributions
    }

    /** Adds one or more distribution to the collection
     *
     * @param dist Distribution to be added
     */
    @Override
    void addDistributions(Distribution... dist) {
        addDistributions dist as List
    }

    /** Adds one or more distribution to the collection
     * If versions already exist in the list they are not updated. (First add wins)
     *
     * @param dist Distribution to be added
     */
    @Override
    void addDistributions(Iterable<Distribution> dists) {
        dists.each { Distribution dist ->
            if(!distributions.containsKey(dist.version))    {
                distributions[ "${dist.version}".toString() ] = dist.location
            }
        }
    }

    /** Returns the location for a specific version.
     *
     * @param version Version that is required
     * @return Location (or null if version is not available)
     */
    @Override
    File location(final String version) {
        distributions[version]
    }

    private final Map<String,File> distributions = [:]

}

//    /** Gets a list of versions that are available locally
//     *
//     * @return List of versions
//     */
//    Set<String> localVersions() {
//        distributions.collect { it.version } as Set<String>
//    }
//
//    /** Gets a set of distributions that were found locally.
//     * If more than one of the same version have been found only
//     * one will be included.
//     * @return Set od distributions to consider
//     */
//    Set<org.ysb33r.gradle.gradletest.internal.Distribution> getLocalDistributions() {
//        distributions
//    }
//
//    /** Lists location of all distributions, including the ones that need downloading.
//     *
//     * @return
//     */
//    Set<org.ysb33r.gradle.gradletest.internal.Distribution> getAllDistributions() {
//        if(downloadedDistributions.empty) {
//            addDistributionsFromConfiguration
//        }
//        distributions + downloadedDistributions
//    }
//
////    Set<String> getDownloadableVersions() {
////
////    }
//
//    void addLocalDistributions(Iterable<org.ysb33r.gradle.gradletest.internal.Distribution> dists) {
//        distributions+= dists
//    }
//
//    void addDownloadedDistributions(Iterable<org.ysb33r.gradle.gradletest.internal.Distribution> dists) {
//        downloadedDistributions+= dists
//    }
//
//    private Set<org.ysb33r.gradle.gradletest.internal.Distribution> distributions = []
//    private Set<org.ysb33r.gradle.gradletest.internal.Distribution> downloadedDistributions = []
//
