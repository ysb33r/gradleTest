package org.ysb33r.gradle.gradletest.internal

import org.gradle.api.Project
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class UnpackerIntegrationSpec extends Specification {

    static final File TESTDIST = new File( System.getProperty('GRADLETESTDIST')  )
    static final String VERSION = IntegrationTestHelper.versionFromFile(TESTDIST)

    Project project = IntegrationTestHelper.buildProject('uis')

    def "Unpack a downloaded distribution"() {
        given: "That I have a downloaded distribution"
        assert TESTDIST.exists()
        File userDir = new File(project.projectDir,'distribution')
        assert !userDir.exists()
        File outputTopDir = new File(userDir,"wrapper/dists/gradle-${VERSION}")

        when: "I attempt to unpack to a fake Gradle User Home"
        Unpacker.unpackTo(userDir,TESTDIST,project.logger)

        then: "I expect the output diretcory to have been created"
        outputTopDir.exists()

        when: "I check the output directory"
        File installed = IntegrationTestHelper.findInstalledVersionDir(outputTopDir,VERSION)

        then: "The distribution must be unpacked in to the appropriate version folder"
        installed != null
        new File(installed,'bin/gradle').exists()


    }
}