package de.gliderpilot.gradle.jnlp

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class GradleJnlpPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('jnlp', GradleJnlpPluginExtension, this, project)

        project.configurations.maybeCreate('jnlp')

        project.tasks.create('generateJnlp', JnlpTask) {
            output new File(project.buildDir, 'tmp/jnlp/launch.jnlp')
        }
        project.tasks.create('copyJars', CopyJarsTask) {
            into new File(project.buildDir, 'tmp/jnlp/lib')
        }
        project.plugins.withId('java') {
            // if plugin java is applied, we don't need to do anything, just use runtime
            project.configurations.jnlp.extendsFrom project.configurations.runtime
            project.tasks.copyJars.dependsOn project.tasks.jar
        }
        /*        project.tasks.create('signJars', SignJarsTask) {
         dependsOn project.copyJars
         from project.fileTree(new File(project.buildDir, 'tmp/jnlp/lib'))
         }
         */
    }
}
