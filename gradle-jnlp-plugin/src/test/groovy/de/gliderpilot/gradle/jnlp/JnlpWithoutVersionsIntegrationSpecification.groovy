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

import spock.lang.Unroll

class JnlpWithoutVersionsIntegrationSpecification extends AbstractJnlpIntegrationSpec {

    def jnlp

    def setup() {
        buildFile << """\
            allprojects {
                apply plugin: 'groovy'
                sourceCompatibility = '1.6'
                targetCompatibility = '1.6'
                dependencies {
                    compile 'org.codehaus.groovy:groovy-all:2.3.1'
                }
            }
            apply plugin: 'application'

            jnlp {
                useVersions = false
            }

            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            """.stripIndent()

        writeHelloWorld('de.gliderpilot.jnlp.test')
        runTasksSuccessfully('generateJnlp')
        jnlp = jnlp()
    }

    @Unroll
    def '[gradle #gv] jars entry is not empty'() {
        given:
        gradleVersion = gv

        expect:
        !jnlp.resources.jar.isEmpty()

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] j2se element is contains version information'() {
        given:
        gradleVersion = gv

        expect:
        jnlp.resources.j2se.@version.text() == '1.6'

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jar groovy-all has version 2.3.1'() {
        given:
        gradleVersion = gv

        and:
        def jar = jnlp.resources.jar.find { it.@href =~ /groovy-all/ }

        expect:
        jar.@href.text() == "lib/groovy-all__V2.3.1.jar"

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jar of project has version 1.0'() {
        given:
        gradleVersion = gv

        and:
        def jar = jnlp.resources.jar.find { it.@href =~ /$moduleName/ }

        expect:
        jar.@href.text() == "lib/${moduleName}__V1.0.jar"

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jar of project with SNAPSHOT version'() {
        given:
        gradleVersion = gv

        and:
        version = "1.0-SNAPSHOT"
        runTasksSuccessfully(':generateJnlp', ':copyJars')
        jnlp = jnlp()

        when:
        def jar = jnlp.resources.jar.find { it.@href =~ /$moduleName/ }

        then:
        jar.@version.text() == ''

        and:
        jar.@href.text() == "lib/${moduleName}__V1.0-SNAPSHOT.jar"

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jar of subproject with SNAPSHOT version'() {
        given:
        gradleVersion = gv

        and:
        addSubproject("subproject")
        buildFile << "dependencies { compile project(':subproject') }"
        version = "1.0-SNAPSHOT"
        runTasksSuccessfully(':generateJnlp', ':copyJars')
        jnlp = jnlp()

        when:
        def jar = jnlp.resources.jar.find { it.@href.text().startsWith('lib/subproject') }

        then:
        jar.@version.text() == ''

        and:
        jar.@href.text() == "lib/subproject__V1.0-SNAPSHOT.jar"

        where:
        gv << gradleVersions
    }

}
