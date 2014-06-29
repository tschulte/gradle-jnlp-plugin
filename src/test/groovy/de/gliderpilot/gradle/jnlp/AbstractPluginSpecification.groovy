package de.gliderpilot.gradle.jnlp

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Shared
import spock.lang.Specification


abstract class AbstractPluginSpecification extends Specification {

    @Shared
    Project project

    Project project(Closure<Project> configuration = null) {
        deleteProjectDir()
        project = ProjectBuilder.builder().build()
        if (configuration)
            project.with(configuration)
        return project
    }

    def cleanupSpec() {
        deleteProjectDir()
    }

    def deleteProjectDir() {
        project?.projectDir?.deleteDir()
    }
}
