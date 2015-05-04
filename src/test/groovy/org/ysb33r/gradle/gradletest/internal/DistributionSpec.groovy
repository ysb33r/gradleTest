package org.ysb33r.gradle.gradletest.internal

import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class DistributionSpec extends Specification {

    def "Converting a list of Distribution to a Set"() {
        given:
        List<DistributionInternal> list = [
            new DistributionInternal('2.2',new File('loc1')),
            new DistributionInternal('2.2',new File('loc2'))
        ]
        Set<DistributionInternal> set = list as Set

        expect:
        set.size() == 1
        set[0].location.name == 'loc1'

    }
}