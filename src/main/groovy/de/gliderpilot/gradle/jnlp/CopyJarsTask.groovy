package de.gliderpilot.gradle.jnlp

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CopyJarsTask extends DefaultTask {

    @InputFiles
    Configuration from

    @OutputDirectory
    File into

    @TaskAction
    void copy() {
        def resolvedJars = from.resolvedConfiguration.resolvedArtifacts.findAll { it.extension == 'jar' }
        resolvedJars.each { ResolvedArtifact artifact ->
            project.copy {
                from artifact.file
                into into
                rename { "${artifact.name}__V${artifact.moduleVersion.id.version}.jar" }
            }
        }
    }
}
