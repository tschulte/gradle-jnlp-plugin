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

class JnlpWithoutJavaPluginIntegrationSpecification extends AbstractJnlpIntegrationSpec {

    def setup() {
        buildFile << """\
            jnlp {
                mainClassName = 'griffon.javafx.JavaFXGriffonApplication'
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

    def 'copyJars task is executed'() {
        expect:
        runTasksSuccessfully(':copyJars').standardOutput.contains(':copyJars')
    }

    def 'jars entry is not empty'() {
        when:
        runTasksSuccessfully('generateJnlp')

        then:
        !jnlp().resources.jar.isEmpty()
    }

    def 'mandatory fields in information block are filled in the jnlp'() {
        when:
        runTasksSuccessfully('generateJnlp')
        def jnlp = jnlp()

        then:
        jnlp.information.title.text() == moduleName
        jnlp.information.vendor.text() == moduleName
    }

    def 'main-class is set'() {
        when:
        runTasksSuccessfully('generateJnlp')

        then:
        jnlp().'application-desc'.@'main-class'.text() == 'griffon.javafx.JavaFXGriffonApplication'
    }

    def 'main-jar is marked'() {
        when:
        runTasksSuccessfully('generateJnlp')

        then:
        jnlp().resources.jar.find { it.@href =~ 'griffon-javafx' }.@main.text() == 'true'
    }

}
