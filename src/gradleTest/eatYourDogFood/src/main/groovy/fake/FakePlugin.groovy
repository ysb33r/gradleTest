import org.gradle.api.Plugin
import org.gradle.api.Project

class FakePlugin implements Plugin<Project> {
    void apply(Project p) {
        project.with {
            task ('runGradleTest') << {
                println 'Hello, FakePlugin'
            }
        }
    }
}