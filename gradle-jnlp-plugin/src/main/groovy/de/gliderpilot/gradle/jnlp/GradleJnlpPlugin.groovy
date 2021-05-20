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

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class GradleJnlpPlugin implements Plugin<Project> {

    void apply(Project project) {
        def jnlp = project.extensions.create('jnlp', GradleJnlpPluginExtension, this, project)

        project.configurations.maybeCreate('jnlp')

        project.tasks.create('generateJnlp', JnlpTask) {
            group = 'jnlp'
            description = 'Generate the jnlp file'
            from = project.configurations.jnlp
        }
        project.tasks.create('copyJars', CopyJarsTask) {
            group = 'jnlp'
            description = 'Copy the jars to the lib folder'
            onlyIf { !project.jnlp.signJarParams }
            from = project.configurations.jnlp
            into new File(project.buildDir, jnlp.destinationPath + '/lib')
        }
        project.tasks.create('signJars', SignJarsTask) {
            group = 'jnlp'
            description = 'Sign the jar files'
            onlyIf { project.jnlp.signJarParams }
            from = project.configurations.jnlp
            into new File(project.buildDir, jnlp.destinationPath + '/lib')
        }
        project.tasks.create('createWebstartDir') {
            group = 'jnlp'
            description = 'Generate the complete webstart directory structure'
            dependsOn 'generateJnlp', 'copyJars', 'signJars'
            outputs.dir new File(project.buildDir, jnlp.destinationPath)
        }
        project.plugins.withId('java') {
            // if plugin java is applied use the runtime configuration
            def configurationNames = project.configurations.names
            if (configurationNames.contains('runtimeOnly')) {
                project.configurations.jnlp.extendsFrom project.configurations.runtimeOnly
            } else if (configurationNames.contains('runtime')) {
                project.configurations.jnlp.extendsFrom project.configurations.runtime
            }
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
