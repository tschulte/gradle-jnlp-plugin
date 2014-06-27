package de.gliderpilot.gradle.jnlp

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class GradleJnlpPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('jnlp', GradleJnlpPluginExtension, this)
    }
}
