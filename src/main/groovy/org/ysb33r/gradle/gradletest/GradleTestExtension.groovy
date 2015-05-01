package org.ysb33r.gradle.gradletest

import org.gradle.api.Project
import org.ysb33r.gradle.gradletest.Distribution

/**
 * @author Schalk W. Cronjé
 */
class GradleTestExtension {

    GradleTestExtension(Project p) {
        project=p
    }

    boolean includeGradleHome = true

    boolean searchGradleUserHome = true

    boolean searchGvm = true

    boolean download = true

    boolean useGradleSite = true

    void urls(String... u) {
        // Add an Ivy repository
//        project.repositories {
//            ivy {
//                ur
//            }
//        }
    }

    Set<Distribution> getLocalDistributionLocations() {
        null
    }

    Set<Distribution> getAllDistributionLocations() {
        null
    }

    Set<String> getDownloadableVersions() {

    }
//    List<Distribution> distributions

    Project project
}
