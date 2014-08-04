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
package de.gliderpilot.gradle.jnlp

import spock.lang.Shared
import spock.lang.Unroll

@Unroll
class JnlpWithoutJavaPluginIntegrationSpecification extends AbstractPluginSpecification {

    @Shared
    def jnlp

    def setupSpec() {
        IntegrationTestProject.enhance(project())
        project.buildFile << """\
            apply plugin: 'de.gliderpilot.jnlp'

            buildscript {
                dependencies {
                    classpath files('${new File('build/classes/main').absoluteFile.toURI()}')
                    classpath files('${new File('build/resources/main').absoluteFile.toURI()}')
                }
            }

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

        project.settingsFile << """\
            rootProject.name = 'test'
        """.stripIndent()
        project.run ':generateJnlp', ':copyJars'
        def jnlpFile = project.file('build/tmp/jnlp/launch.jnlp')
        jnlp = new XmlSlurper().parse(jnlpFile)
    }

    def 'generateJnlp task is executed'() {
        expect:
        project.wasExecuted(':generateJnlp')
    }

    def 'copyJars task is executed'() {
        expect:
        project.wasExecuted(':copyJars')
    }

    def 'jars entry is not empty'() {
        when:
        def jars = jnlp.resources.jar

        then:
        !jars.isEmpty()
    }

    def 'mandatory fields in information block are filled in the jnlp'() {
        expect:
        jnlp.information.title.text() == project.name
        jnlp.information.vendor.text() == project.name
    }

    def 'main-class is set'() {
        expect:
        jnlp.'application-desc'.@'main-class'.text() == 'griffon.javafx.JavaFXGriffonApplication'
    }

    def 'main-jar is marked'() {
        expect:
        jnlp.resources.jar.find { it.@href =~ 'griffon-javafx' }.@main.text() == 'true'
    }

}
