package org.ysb33r.gradle.gradletest.internal

import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class DistributionSpec extends Specification {

    def "Converting a list of Distribution to a Set"() {
        given:
        List<Distribution> list = [
            new Distribution('2.2',new File('loc1')),
            new Distribution('2.2',new File('loc2'))
        ]
        Set<Distribution> set = list as Set

        expect:
        set.size() == 1
        set[0].location.name == 'loc1'

    }
}