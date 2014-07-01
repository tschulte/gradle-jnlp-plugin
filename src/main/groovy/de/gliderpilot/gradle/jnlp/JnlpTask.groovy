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
        MarkupBuilder xml = new MarkupBuilder(output.newPrintWriter('UTF-8'))
        def extension = project.jnlp
        xml.jnlp(extension.jnlpParams) {
            if (extension.withXmlClosure) {
                delegate.with extension.withXmlClosure
            }
            xml.resources {
                j2se(version: project.targetCompatibility)
                jar(jarParams(project.name, project.version) + [main: 'true'])
                def resolvedJars = project.configurations.runtime.resolvedConfiguration.resolvedArtifacts.findAll { it.extension == 'jar' }
                resolvedJars.each { ResolvedArtifact artifact ->
                    jar(jarParams(artifact.name, artifact.moduleVersion.id.version))
                }
            }
            'application-desc'('main-class': "${project.mainClassName}")
        }
    }

    Map<String, String> jarParams(String name, String version) {
        if (project.jnlp.useVersions)
            [href: "lib/${name}.jar", version: "${version}"]
        else
            [href: "lib/${name}__V${version}.jar"]
    }
}
