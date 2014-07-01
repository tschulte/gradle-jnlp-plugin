package de.gliderpilot.gradle.jnlp

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CopyJarsTask extends DefaultTask {

    @OutputDirectory
    File into

    @TaskAction
    void copy() {
        project.copy {
            from project.tasks.jar.outputs.files
            into into
            rename { "${project.name}__V${project.version}.jar" }
        }
        def resolvedJars = project.configurations.runtime.resolvedConfiguration.resolvedArtifacts.findAll { it.extension == 'jar' }
        resolvedJars.each { ResolvedArtifact artifact ->
            project.copy {
                from artifact.file
                into into
                rename { "${artifact.name}__V${artifact.moduleVersion.id.version}.jar" }
            }
        }
    }
}
