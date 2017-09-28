/*
// This line is used as an import canary
// It relates to https://github.com/ysb33r/gradleTest/issues/47
import org.ysb33r.gradle.gradletest.GradleTest

apply plugin : 'org.ysb33r.gradletest'
apply plugin : 'groovy'

repositories {
    jcenter()
}

dependencies {
    compile localGroovy()
    compile gradleApi()
}

gradleTest {
    versions '2.13','3.0'
}

// This is nice for debugging, but can only be done on later versions
if(GradleVersion.current() > GradleVersion.version('2.13')) {
    gradleTest {
        beforeTest {
            println '-'.multiply(60)
            println "| [${project.name}] ${it.name}"
            println "| ${configurations.gradleTestCompile.asPath}"
            println '-'.multiply(60)
        }
    }
}

task runGradleTest( dependsOn : ['assemble','gradleTest'] )
 */

plugins {
    `java-gradle-plugin`
    groovy
}

group = "my"

version = "1.0"

gradlePlugin {
    (plugins) {
        "documentation" {
            id = "my.documentation"
            implementationClass = "my.DocumentationPlugin"
        }
    }
}

apply {
    plugin("org.ysb33r.gradletest")
}

repositories {
    jcenter()
}

tasks {

    "gradleTest" {
        versions ("4.1","4.2")
    }

    val runGradleTest by creating {
        dependsOn (gradleTest)
    }
}
