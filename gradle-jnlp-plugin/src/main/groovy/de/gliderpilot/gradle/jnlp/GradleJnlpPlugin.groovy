/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gliderpilot.gradle.jnlp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.component.Artifact
import org.gradle.api.distribution.Distribution
import org.gradle.model.ModelMap
import org.gradle.model.Mutate
import org.gradle.model.Path
import org.gradle.model.RuleSource

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class GradleJnlpPlugin implements Plugin<Project> {

    void apply(Project project) {
        def jnlp = project.extensions.create('jnlp', GradleJnlpPluginExtension, this, project)

        project.configurations.maybeCreate('jnlp')

        project.tasks.create('generateJnlp', JnlpTask) {
            from = project.configurations.jnlp
        }
        project.tasks.create("incrementalCleanupSignedJars", IncrementalCleanupSignedJarsTask) {
            from = project.configurations.jnlp
        }
        project.tasks.create('signJars') {
            dependsOn 'incrementalCleanupSignedJars'
        }
        project.tasks.create('createWebstartDir') {
            dependsOn 'generateJnlp', 'signJars'
            outputs.dir new File(project.buildDir, jnlp.destinationPath)
        }

        project.afterEvaluate {
            project.configurations.jnlp.resolvedConfiguration.resolvedArtifacts.each { artifact ->
                Task signTask = project.tasks.create("sign${artifact.file.name}", SignJarTask) {
                    from artifact
                }
                project.tasks.signJars.dependsOn signTask
                signTask.mustRunAfter 'incrementalCleanupSignedJars'
            }
        }

        project.plugins.withId('java') {
            // if plugin java is applied use the runtime configuration
            project.configurations.jnlp.extendsFrom project.configurations.runtime
            // plus the project itself
            project.dependencies.jnlp project
        }
        project.plugins.withId('distribution') {
            project.distributions.create('webstart') {
                contents {
                    from project.tasks.createWebstartDir
                }
            }
            project.configurations.create("webstartZip")
            project.artifacts {
                webstartZip project.tasks.webstartDistZip
            }
        }
    }

}
