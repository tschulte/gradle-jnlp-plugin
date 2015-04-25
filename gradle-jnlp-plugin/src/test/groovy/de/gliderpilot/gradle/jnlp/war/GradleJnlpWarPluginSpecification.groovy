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
package de.gliderpilot.gradle.jnlp.war

import de.gliderpilot.gradle.jnlp.AbstractPluginSpecification

class GradleJnlpWarPluginSpecification extends AbstractPluginSpecification {

    def setupSpec() {
        project {
            apply plugin: 'de.gliderpilot.jnlp-war'
            repositories {
                jcenter()
            }
            jnlp {
                versions {
                    v1 'org.swinglabs:jxlayer:3.0.2'
                    v2 'org.swinglabs:jxlayer:3.0.3'
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
        project.configurations.v1.files.collect { it.name } == ['jxlayer-3.0.2.jar']
    }

    def "configuration v2 is created"() {
        expect:
        project.configurations.findByName('v2')
    }

    def "the configuration v2 contains the defined dependency"() {
        expect:
        project.configurations.v2.files.collect { it.name } == ['jxlayer-3.0.3.jar']
    }

}
