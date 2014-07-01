package de.gliderpilot.gradle.jnlp

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class GradleJnlpPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'java'
        project.extensions.create('jnlp', GradleJnlpPluginExtension, this, project)

        project.tasks.create('generateJnlp', JnlpTask) {
            output new File(project.buildDir, 'tmp/jnlp/launch.jnlp')
        }
        project.tasks.create('copyJars', CopyJarsTask) {
            dependsOn project.jar
            into new File(project.buildDir, 'tmp/jnlp/lib')
        }
        project.tasks.create('generateVersionXml', VersionXmlTask) {
            output new File(project.buildDir, 'tmp/jnlp/lib/version.xml')
        }
        /*        project.tasks.create('signJars', SignJarsTask) {
         dependsOn project.copyJars
         from project.fileTree(new File(project.buildDir, 'tmp/jnlp/lib'))
         }
         */
    }
}
