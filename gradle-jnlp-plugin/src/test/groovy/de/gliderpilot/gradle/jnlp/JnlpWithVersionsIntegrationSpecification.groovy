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
class JnlpWithVersionsIntegrationSpecification extends AbstractJnlpIntegrationSpec {

    def jnlp
    ExecutionResult executionResult

    def setup() {
        buildFile << """\
            apply plugin: 'groovy'
            apply plugin: 'application'

            jnlp {
                useVersions = true
            }

            dependencies {
                compile 'org.codehaus.groovy:groovy-all:2.3.1'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            """.stripIndent()

        writeHelloWorld('de.gliderpilot.jnlp.test')
        executionResult = runTasksSuccessfully(':generateJnlp', ':copyJars')
        jnlp = jnlp()
    }

    def 'generateJnlp task is executed'() {
        expect:
        executionResult.wasExecuted(':generateJnlp')
    }

    def 'copyJars task is executed'() {
        expect:
        executionResult.wasExecuted(':copyJars')
    }

    def 'jars entry is not empty'() {
        when:
        def jars = jnlp.resources.jar

        then:
        !jars.isEmpty()
    }

    def 'mandatory fields in information block are filled in the jnlp'() {
        expect:
        jnlp.information.title.text() == moduleName
        jnlp.information.vendor.text() == moduleName
    }

    def 'jar groovy-all has version 2.3.1'() {
        given:
        def jar = jnlp.resources.jar.find { it.@href =~ /groovy-all/ }

        expect:
        jar.@version.text() == '2.3.1'

        and:
        jar.@href.text() == "lib/groovy-all.jar"
    }

    def 'jar of project has version 1.0'() {
        given:
        def jar = jnlp.resources.jar.find { it.@href =~ /$moduleName/ }

        expect:
        jar.@version.text() == '1.0'

        and:
        jar.@href.text() == "lib/${moduleName}.jar"
    }

    def 'property jnlp.versionEnabled is set to true'() {
        when:
        def property = jnlp.resources.property.find { it.@name == 'jnlp.versionEnabled' }

        then:
        property.@value.text() == 'true'

    }

    def 'jars are copied'() {
        expect:
        directory('build/jnlp/lib').list { file, name -> name.endsWith '.jar' }.sort() == [
            'groovy-all__V2.3.1.jar',
            "${moduleName}__V1.0.jar"
        ]
    }

    def 'main-class is set'() {
        expect:
        jnlp.'application-desc'.@'main-class'.text() == 'de.gliderpilot.jnlp.test.HelloWorld'
    }

    def 'main-jar is marked'() {
        expect:
        jnlp.resources.jar.find { it.@href =~ "$moduleName" }.@main.text() == 'true'
    }
}
