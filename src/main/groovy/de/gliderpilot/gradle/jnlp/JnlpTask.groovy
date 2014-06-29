package de.gliderpilot.gradle.jnlp

import groovy.xml.MarkupBuilder

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class JnlpTask extends DefaultTask {

    @OutputFile
    File output

    @TaskAction
    void generateJnlp() {
        MarkupBuilder xml = new MarkupBuilder(output.newPrintWriter())
        def extension = project.jnlp
        xml.jnlp(extension.jnlpParams) {
            if (extension.withXmlClosure)
                delegate.with extension.withXmlClosure
            resources {
                j2se(version: project.targetCompatibility)
                jar(href: "lib/${project.name}.jar", version: "${project.version}", main: 'true')
                def resolvedJars = project.configurations.runtime.resolvedConfiguration.resolvedArtifacts.findAll { it.extension == 'jar' }
                resolvedJars.each { ResolvedArtifact artifact ->
                    jar(href: "lib/${artifact.name}.jar", version: "${artifact.moduleVersion.id.version}")
                }
            }
            'application-desc'('main-class': "${project.mainClassName}")
        }
    }
}
