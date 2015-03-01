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
class JnlpWithoutVersionsIntegrationSpecification extends AbstractPluginSpecification {

    @Shared
    def jnlp

    def setupSpec() {
        IntegrationTestProject.enhance(project())
        project.buildFile << """\
            apply plugin: 'groovy'
            apply plugin: 'application'
            apply plugin: 'de.gliderpilot.jnlp'
            //apply plugin: 'jetty'

            buildscript {
                dependencies {
                    classpath files('${new File('build/classes/main').absoluteFile.toURI()}')
                    classpath files('${new File('build/resources/main').absoluteFile.toURI()}')
                }
            }

            jnlp {
                useVersions = false
            }

            version = '1.0'
            targetCompatibility = '1.6'

            repositories {
                jcenter()
            }
            dependencies {
                compile 'org.codehaus.groovy:groovy-all:2.3.1'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.Main'
        """.stripIndent()

        project.settingsFile << """\
            rootProject.name = 'test'
        """.stripIndent()
        project.file('src/main/groovy/de/gliderpilot/jnlp/test').mkdirs()
        project.file('src/main/groovy/de/gliderpilot/jnlp/test/Main.groovy') << """\
            package de.gliderpilot.jnlp.test
            class Main {
                static main(args) {
                    println "test"
                }
            }
        """.stripIndent()
        project.run ':generateJnlp', ':copyJars'
        def jnlpFile = project.file('build/jnlp/launch.jnlp')
        jnlp = new XmlSlurper().parse(jnlpFile)
    }

    def 'jars entry is not empty'() {
        when:
        def jars = jnlp.resources.jar

        then:
        !jars.isEmpty()
    }

    def 'j2se element is contains version information'() {
        expect:
        jnlp.resources.j2se.@version.text() == '1.6'
    }

    def 'jar #artifact has version #version'() {
        when:
        def jar = jnlp.resources.jar.find { it.@href =~ /$artifact/ }

        then:
        jar != null

        and:
        jar.@href.text() == "lib/${artifact}__V${version}.jar"

        where:
        artifact     | version
        'groovy-all' | '2.3.1'
        'test'       | '1.0'
    }
}
