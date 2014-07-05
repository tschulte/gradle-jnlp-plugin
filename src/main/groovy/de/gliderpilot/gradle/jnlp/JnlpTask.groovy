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
        xml.jnlp(project.jnlp.jnlpParams) {
            if (project.jnlp.withXmlClosure) {
                delegate.with project.jnlp.withXmlClosure
            }
            xml.resources {
                j2se(project.jnlp.j2seParams)
                // TODO: search, which jar contains the main class
                if (project.plugins.hasPlugin('java'))
                    jar(jarParams(project.name, project.version) + [main: 'true'])
                def resolvedJars = project.configurations.jnlp.resolvedConfiguration.resolvedArtifacts.findAll { it.extension == 'jar' }
                resolvedJars.each { ResolvedArtifact artifact ->
                    jar(jarParams(artifact.name, artifact.moduleVersion.id.version))
                }

                // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/avoidingUnnecessaryUpdateChecks.html
                if (project.jnlp.useVersions)
                    property name: 'jnlp.versionEnabled', value: 'true'
            }
            'application-desc'('main-class': "${project.jnlp.mainClassName}")
        }
    }

    Map<String, String> jarParams(String name, String version) {
        if (project.jnlp.useVersions)
            [href: "lib/${name}.jar", version: "${version}"]
        else
            [href: "lib/${name}__V${version}.jar"]
    }
}
