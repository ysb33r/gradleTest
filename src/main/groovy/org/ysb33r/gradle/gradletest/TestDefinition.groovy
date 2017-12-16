package org.ysb33r.gradle.gradletest

import groovy.transform.TupleConstructor

@TupleConstructor
class TestDefinition implements Serializable {
      File testDir
      List<File> groovyBuildFiles
      List<File> kotlinBuildFiles
}
