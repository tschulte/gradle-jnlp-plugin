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
import spock.lang.Shared
import spock.lang.Unroll

@Unroll
class JnlpWithPack200IntegrationSpecification extends IntegrationSpec {

    def jnlp
    ExecutionResult executionResult

    def setup() {
        buildFile << """\
            apply plugin: 'java'
            apply plugin: 'application'
            apply plugin: 'de.gliderpilot.jnlp'

            jnlp {
                usePack200 = true
                signJarParams = [alias: 'myalias', storepass: 'mystorepass',
                    keystore: 'file:keystore.ks']
            }

            version = '1.0'

            repositories {
                jcenter()
            }
            dependencies {
                // jxlayer is already signed. This caused problems with usePack200
                compile 'org.swinglabs:jxlayer:3.0.4'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            ant.genkey(alias: 'myalias', storepass: 'mystorepass', dname: 'CN=Ant Group, OU=Jakarta Division, O=Apache.org, C=US',
                       keystore: 'keystore.ks')
        """.stripIndent()

        writeHelloWorld('de.gliderpilot.jnlp.test')
        executionResult = runTasksSuccessfully(':createWebstartDir')
        def jnlpFile = file('build/jnlp/launch.jnlp')
        jnlp = new XmlSlurper().parse(jnlpFile)
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
        directory("build/jnlp/lib").list().sort() == ['jxlayer__V3.0.4.jar.pack.gz', "${moduleName}__V1.0.jar.pack.gz"].sort()
    }
}
