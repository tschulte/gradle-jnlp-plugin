package de.gliderpilot.gradle.jnlp

import java.util.jar.JarFile
import groovy.xml.MarkupBuilder

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class AbstractCopyJarsTask extends DefaultTask {

    @InputFiles
    Configuration from

    @OutputDirectory
    File into

    String newName(String fileName) {
        ResolvedArtifact artifact = from.resolvedConfiguration.resolvedArtifacts.find { it.extension == 'jar' && it.file.name == fileName }
        artifact ? "${artifact.name}__V${artifact.moduleVersion.id.version}.jar" : fileName
    }
}
