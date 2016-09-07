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

import nebula.test.functional.ExecutionResult
import spock.lang.Unroll

@Unroll
class JnlpWithPack200IntegrationSpecification extends AbstractJnlpIntegrationSpec {

    def jnlp
    ExecutionResult executionResult

    def setup() {
        buildFile << """\
            apply plugin: 'java'
            apply plugin: 'application'

            jnlp {
                usePack200 = true
            }

            dependencies {
                // jxlayer is already signed. This caused problems with usePack200
                compile 'org.swinglabs:jxlayer:3.0.4'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            """.stripIndent()
        enableJarSigner()
        writeHelloWorld('de.gliderpilot.jnlp.test')
        executionResult = runTasksSuccessfully(':createWebstartDir')
        jnlp = jnlp()
    }

    def 'generateJnlp task is executed'() {
        expect:
        executionResult.wasExecuted(':generateJnlp')
    }

    def 'signJars task is executed'() {
        expect:
        executionResult.wasExecuted(':signJars')
    }

    def 'property jnlp.packEnabled is set to true'() {
        when:
        def property = jnlp.resources.property.find { it.@name == 'jnlp.packEnabled' }

        then:
        property.@value.text() == 'true'
    }

    def 'jar file is packed with pack200'() {
        expect:
        directory("build/jnlp/lib").list().sort() == ['jxlayer__V3.0.4-myalias.jar.pack.gz', "${moduleName}__V1.0-myalias.jar.pack.gz"].sort()
    }
}
