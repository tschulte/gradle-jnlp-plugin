package de.gliderpilot.gradle.jnlp

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GradleJnlpPluginTest {
    @Test
    void pluginIsApplied() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'de.gliderpilot.jnlp'
    }
}
