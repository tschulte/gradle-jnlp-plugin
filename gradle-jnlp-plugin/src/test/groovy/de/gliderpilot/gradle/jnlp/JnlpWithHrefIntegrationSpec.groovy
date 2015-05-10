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
import nebula.test.functional.ExecutionResult
import spock.lang.Unroll

@Unroll
class JnlpWithHrefIntegrationSpec extends IntegrationSpec {

    ExecutionResult executionResult
    def jnlp

    def setup() {
        buildFile << '''\
            apply plugin: 'groovy'
            apply plugin: 'application'
            apply plugin: 'de.gliderpilot.jnlp'

            jnlp {
                href "launch_v${project.version}.jnlp"
            }

            repositories {
                jcenter()
            }
            dependencies {
                compile 'org.codehaus.groovy:groovy-all:2.3.1'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
        '''.stripIndent()

        version = '1.0'

        writeHelloWorld('de.gliderpilot.jnlp.test')
    }

    def setVersion(String version) {
        file('gradle.properties').text = """\
            version=$version
        """.stripIndent()
    }

    def 'target folder does only contain one jnlp after version change'() {
        when:
        runTasksSuccessfully('generateJnlp')
        version = '1.1'
        runTasksSuccessfully('generateJnlp')

        then:
        !fileExists('build/jnlp/launch_v1.0.jnlp')
        fileExists('build/jnlp/launch_v1.1.jnlp')
    }

}
