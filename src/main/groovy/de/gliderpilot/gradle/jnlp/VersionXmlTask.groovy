package de.gliderpilot.gradle.jnlp

import groovy.xml.MarkupBuilder

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class VersionXmlTask extends DefaultTask {

    @OutputFile
    File output

    @TaskAction
    void generateVersionXml() {
        MarkupBuilder xml = new MarkupBuilder(output.newPrintWriter('UTF-8'))
        xml.'jnlp-versions' {
            resource {
                pattern {
                    xml.name "${project.name}.jar"
                    "version-id" project.version
                }
                file "${project.name}__V${project.version}.jar"
            }
            def resolvedJars = project.configurations.runtime.resolvedConfiguration.resolvedArtifacts.findAll { it.extension == 'jar' }
            resolvedJars.each { ResolvedArtifact artifact ->
                resource {
                    pattern {
                        xml.name "${artifact.name}.jar"
                        "version-id" artifact.moduleVersion.id.version
                    }
                    file "${artifact.name}__V${artifact.moduleVersion.id.version}.jar"
                }
            }
        }
    }
}
