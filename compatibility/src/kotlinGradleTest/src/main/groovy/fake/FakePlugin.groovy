package fake

import org.gradle.api.Plugin
import org.gradle.api.Project

class FakePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.with {
            task ('runGradleTest') << {
                println 'Hello, FakePlugin'
            }
        }
    }
}