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
class JnlpWithoutJavaPluginIntegrationSpecification extends IntegrationSpec {

    def setup() {
        buildFile << """\
            apply plugin: 'de.gliderpilot.jnlp'

            jnlp {
                mainClassName = 'griffon.javafx.JavaFXGriffonApplication'
            }

            version = '1.0'

            repositories {
                jcenter()
            }
            dependencies {
                jnlp 'org.codehaus.griffon:griffon-javafx:2.0.0.BETA3'
            }
        """.stripIndent()
    }

    def 'generateJnlp task is executed'() {
        expect:
        runTasksSuccessfully(':generateJnlp').standardOutput.contains(':generateJnlp')
    }

    def 'jars entry is not empty'() {
        expect:
        !createJnlp().resources.jar.isEmpty()
    }

    def 'mandatory fields in information block are filled in the jnlp'() {
        given:
        def jnlp = createJnlp()

        expect:
        jnlp.information.title.text() == moduleName
        jnlp.information.vendor.text() == moduleName
    }

    def 'main-class is set'() {
        expect:
        createJnlp().'application-desc'.@'main-class'.text() == 'griffon.javafx.JavaFXGriffonApplication'
    }

    def 'main-jar is marked'() {
        expect:
        createJnlp().resources.jar.find { it.@href =~ 'griffon-javafx' }.@main.text() == 'true'
    }

    private def createJnlp() {
        runTasksSuccessfully(':generateJnlp')
        def jnlpFile = file('build/jnlp/launch.jnlp')
        new XmlSlurper().parse(jnlpFile)
    }

}
