package de.gliderpilot.gradle.jnlp

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import spock.lang.Specification


class GradleJnlpPluginSpecification extends Specification {

    def "plugin can be applied"() {
		given:
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'de.gliderpilot.jnlp'

		expect:
		project.plugins.hasPlugin('de.gliderpilot.jnlp')

		and:
		project.plugins['de.gliderpilot.jnlp'].class == GradleJnlpPlugin
    }
}
