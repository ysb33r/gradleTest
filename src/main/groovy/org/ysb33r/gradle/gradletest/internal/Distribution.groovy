package org.ysb33r.gradle.gradletest.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import groovy.transform.TupleConstructor
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.internal.os.OperatingSystem
import org.ysb33r.gradle.gradletest.GradleTestExtension

import org.ysb33r.gradle.gradletest.Distribution as DistributionInterface

import java.util.regex.Matcher

/**
 * @author Schalk W. Cronjé
 */
@TupleConstructor
@Sortable(excludes=['location'])
@EqualsAndHashCode(excludes=['location'])
class Distribution implements DistributionInterface {

    String version
    File location

    static Distribution getCurrentGradle(Gradle gradle) {
        gradle.rootProject.logger.debug "Found gradle version ${gradle.gradleVersion} in ${gradle.gradleHomeDir}"
        new Distribution(gradle.gradleVersion, gradle.gradleHomeDir)
    }

    @CompileDynamic
    static List<Distribution> searchGradleHome(Gradle gradle) {
        Logger logger = gradle.rootProject.logger
        List<Distribution> wrapper = []
        File searchFolder = new File(gradle.gradleUserHomeDir,'wrapper/dists').absoluteFile
        searchFolder.eachDir { File topDir ->
            // topDir is a folder potentially containing folders which
            // in themselves might contain a folder with an unpacked distribution
            topDir.eachDir { File intermediateDir ->
                logger.debug "Searching ${intermediateDir} for gradle distributions"
                intermediateDir.eachDirMatch ~/gradle-(.+)(-(bin|all))?/, { File gradleDir ->
                    Matcher matcher = gradleDir.name =~ /gradle-(.+)(?:-(bin|all))?/
                    if( matcher.matches() ) {
                        logger.debug "Found gradle version ${matcher[0][1]} in ${gradleDir}"
                        nwrapper+= ew Distribution(matcher[0][1],gradleDir)
                    }
                }
            }
        }
        wrapper
    }

    static List<Distribution> searchGvm(Logger logger) {
        // TODO: Check directories for WIndows & Cygwin. Also check behaviour of posh-gvm
        File searchFolder = new File(System.getProperty('user.home') + '/.gvm/gradle').absoluteFile

//        def os = OperatingSystem.current()
//        if(os.isWindows()) {
//
//        } else {
//
//        }

        List<Distribution> gvm = []
        logger.debug "Searching ${searchFolder} for gradle distributions"
        searchFolder.eachDir { File dir ->
            if(dir.name != 'current') {
                if( new File(dir,'bin/gradle').exists()) {
                    logger.debug "Found gradle version ${dir.name} in ${dir}"
                    gvm+= new Distribution(dir.name,dir)
                }
            }
        }
        gvm
    }

    static List<Distribution> searchLocal(GradleTestExtension config) {
        List<Distribution> all = []
        Project project = config.project

        if (config.includeGradleHome) {
            all += getCurrentGradle(project.gradle)
        }

        if (config.searchGradleUserHome) {
            all += searchGradleHome(project.gradle)
        }

        if (config.searchGvm) {
            all += searchGvm(project.logger)
        }

        all
    }


}
