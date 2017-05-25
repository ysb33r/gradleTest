package multiproject

import org.gradle.api.Plugin
import org.gradle.api.Project

class MyLibPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.tasks.create ('myLibRunner') {
            doLast {
                println MyLib.NAME
            }
        }
    }
}