package de.gliderpilot.gradle.jnlp

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import spock.lang.Specification


class GradleJnlpPluginSpecification extends AbstractPluginSpecification {

    def setupSpec() {
        project {
            apply plugin: 'de.gliderpilot.jnlp'
        }
    }

    def "plugin was applied"() {
        expect:
        project.plugins.hasPlugin('de.gliderpilot.jnlp')

        and:
        project.plugins['de.gliderpilot.jnlp'].class == GradleJnlpPlugin
    }

}
