
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
