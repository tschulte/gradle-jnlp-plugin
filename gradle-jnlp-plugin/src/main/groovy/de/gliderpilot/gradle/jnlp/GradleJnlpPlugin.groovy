/*
 * Copyright 2014 the original author or authors.
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

    String destinationPath = 'jnlp'

    void apply(Project project) {
        project.extensions.create('jnlp', GradleJnlpPluginExtension, this, project)

        project.configurations.maybeCreate('jnlp')

        project.tasks.create('generateJnlp', JnlpTask) {
            from = project.configurations.jnlp
            output new File(project.buildDir, destinationPath + '/launch.jnlp')
        }
        project.tasks.create('copyJars', CopyJarsTask) {
            onlyIf { !project.jnlp.signJarParams }
            from = project.configurations.jnlp
            into new File(project.buildDir, destinationPath + '/lib')
        }
        project.tasks.create('signJars', SignJarsTask) {
            onlyIf { project.jnlp.signJarParams }
            from = project.configurations.jnlp
            into new File(project.buildDir, destinationPath + '/lib')
        }
        project.tasks.create('createWebstartDir') {
            dependsOn 'generateJnlp', 'copyJars', 'signJars'
            outputs.dir new File(project.buildDir, destinationPath)
        }
        project.plugins.withId('java') {
            // if plugin java is applied use the runtime configuration
            project.configurations.jnlp.extendsFrom project.configurations.runtime
            // plus the project itself
            project.dependencies.jnlp project
        }
    }
}
