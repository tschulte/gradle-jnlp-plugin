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

import nebula.test.IntegrationSpec
import spock.lang.Shared
import spock.lang.Unroll

@Unroll
class JnlpWithoutVersionsIntegrationSpecification extends AbstractJnlpIntegrationSpec {

    def jnlp

    def setup() {
        buildFile << """\
            apply plugin: 'groovy'
            apply plugin: 'application'

            jnlp {
                useVersions = false
            }

            sourceCompatibility = '1.6'
            targetCompatibility = '1.6'

            dependencies {
                compile 'org.codehaus.groovy:groovy-all:2.3.1'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            """.stripIndent()

        writeHelloWorld('de.gliderpilot.jnlp.test')
        runTasksSuccessfully('generateJnlp')
        jnlp = jnlp()
    }

    def 'jars entry is not empty'() {
        expect:
        !jnlp.resources.jar.isEmpty()
    }

    def 'j2se element is contains version information'() {
        expect:
        jnlp.resources.j2se.@version.text() == '1.6'
    }

    def 'jar groovy-all has version 2.3.1'() {
        given:
        def jar = jnlp.resources.jar.find { it.@href =~ /groovy-all/ }

        expect:
        jar.@href.text() == "lib/groovy-all__V2.3.1.jar"
    }

    def 'jar of project has version 1.0'() {
        given:
        def jar = jnlp.resources.jar.find { it.@href =~ /$moduleName/ }

        expect:
        jar.@href.text() == "lib/${moduleName}__V1.0.jar"
    }

}
