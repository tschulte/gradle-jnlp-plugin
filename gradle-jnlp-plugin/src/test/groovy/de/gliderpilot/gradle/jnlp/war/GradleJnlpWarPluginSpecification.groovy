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
package de.gliderpilot.gradle.jnlpWar.war

import de.gliderpilot.gradle.jnlp.war.GradleJnlpWarPlugin
import nebula.test.PluginProjectSpec

class GradleJnlpWarPluginSpecification extends PluginProjectSpec {

    @Override
    String getPluginName() {
        return 'de.gliderpilot.jnlp-war'
    }

    def setup() {
        project.with {
            apply plugin: pluginName
            jnlpWar {
                versions {
                    v1 files(new File('build/resources/application-0.1.0.zip').absoluteFile.toURI())
                    v2 files(new File('build/resources/application-0.1.1.zip').absoluteFile.toURI())
                }
            }
        }
    }

    def "plugin was applied"() {
        expect:
        project.plugins.hasPlugin('de.gliderpilot.jnlp-war')

        and:
        project.plugins['de.gliderpilot.jnlp-war'].class == GradleJnlpWarPlugin
    }

    def "configuration v1 is created"() {
        expect:
        project.configurations.findByName('v1')
    }

    def "the configuration v1 contains the defined dependency"() {
        expect:
        project.configurations.v1.singleFile.name == 'application-0.1.0.zip'
    }

    def "configuration v2 is created"() {
        expect:
        project.configurations.findByName('v2')
    }

    def "the configuration v2 contains the defined dependency"() {
        expect:
        project.configurations.v2.singleFile.name == 'application-0.1.1.zip'
    }

    def "launchers are empty if not configured"() {
        expect:
        !project.jnlpWar.launchers.hasSource()
    }

    def "launchers can be defined with a method named after the version"() {
        when:
        project.jnlpWar.launchers {
            v1()
            v2()
        }

        then:
        project.jnlpWar.launchers.hasSource()
    }

    def "launchers can be further configured using closures"() {
        when:
        project.jnlpWar.launchers {
            v1 {
                rename 'launch.jnlp', 'launch_v1.jnlp'
            }
            v2 {
                rename 'launch.jnlp', 'launch_v2.jnlp'
            }
        }

        then:
        project.jnlpWar.launchers.hasSource()
    }

    def "launchers can be further refined after creation"() {
        when:
        project.jnlpWar.launchers {
            v1()
            v1 {
                rename 'launch.jnlp', 'launch_v1.jnlp'
            }
        }

        then:
        project.jnlpWar.launchers.hasSource()
    }

    def "extension.from is alias for versions and launchers with version"() {
        when:
        project.version = '1.0'
        addSubproject('application').with {
            version = '1.0'
            apply plugin: 'de.gliderpilot.jnlp'
            apply plugin: 'application'
            mainClassName = 'Main'
        }
        project.jnlpWar {
            from project(':application')
        }

        then:
        project.configurations.findByName("1.0")
        project.jnlpWar.launchers.hasSource()
    }

}
