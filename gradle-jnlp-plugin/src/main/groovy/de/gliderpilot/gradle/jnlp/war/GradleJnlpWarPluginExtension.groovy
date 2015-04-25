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
package de.gliderpilot.gradle.jnlp.war

import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy

import javax.inject.Inject

class GradleJnlpWarPluginExtension {

    /* should allow following dsl:
    apply plugin: 'de.gliderpilot.jnlp-war'
    jnlp {
        versions {
            v1 'org.example:application:1.0:webstart@zip'
            v2 'org.example:application:2.0:webstart@zip'
            v3 'org.example:application:3.0:webstart@zip'
        }
        launchers {
            from(v2) {
                rename 'launch.jnlp', 'launch_v2.jnlp'
                jardiff {
                    from v1
                }
            }
            from(v3) {
                jardiff {
                    from v1, v2
                }
            }
        }
    }

    */

    private GradleJnlpWarPlugin plugin
    private Project project

    @Inject
    GradleJnlpWarPluginExtension(GradleJnlpWarPlugin plugin, Project project) {
        this.plugin = plugin
        this.project = project
        launchers = project.copySpec {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            includeEmptyDirs = false
        }
    }

    void versions(Closure closure) {
        closure.delegate = new Versions(project)
        closure()
    }

    private class Versions {
        private Project project

        Versions(Project project) {
            this.project = project
        }

        @Override
        Object invokeMethod(String name, Object args) {
            project.configurations.maybeCreate(name)
            return project.dependencies.invokeMethod(name, args)
        }
    }

}
