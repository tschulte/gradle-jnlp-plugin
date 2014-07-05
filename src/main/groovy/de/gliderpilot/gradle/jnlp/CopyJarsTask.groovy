package de.gliderpilot.gradle.jnlp

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CopyJarsTask extends AbstractCopyJarsTask {


    @TaskAction
    void copy() {
        project.copy {
            from from
            into into
            rename { String fileName ->
                newName(fileName)
            }
        }
    }
}
