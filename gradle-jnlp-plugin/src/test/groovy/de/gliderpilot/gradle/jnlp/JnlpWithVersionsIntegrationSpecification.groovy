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

class JnlpWithVersionsIntegrationSpecification extends AbstractJnlpIntegrationSpec {

    def jnlp
    ExecutionResult executionResult

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
                useVersions = true
                usePack200 = false
            }

            dependencies {
                compile 'org.codehaus.groovy:groovy-all:2.3.1'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            """.stripIndent()

        writeHelloWorld('de.gliderpilot.jnlp.test')
        executionResult = runTasksSuccessfully(':createWebstartDir')
        jnlp = jnlp()
    }

    @Unroll
    def '[gradle #gv] generateJnlp task is executed'() {
        given:
        gradleVersion = gv

        expect:
        executionResult.wasExecuted(':generateJnlp')

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] signJars task is executed'() {
        given:
        gradleVersion = gv

        expect:
        executionResult.wasExecuted(':signJars')

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jars entry is not empty'() {
        given:
        gradleVersion = gv

        when:
        def jars = jnlp.resources.jar

        then:
        !jars.isEmpty()

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] mandatory fields in information block are filled in the jnlp'() {
        given:
        gradleVersion = gv

        expect:
        jnlp.information.title.text() == moduleName
        jnlp.information.vendor.text() == moduleName

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
        jar.@version.text() == '2.3.1'

        and:
        jar.@href.text() == "lib/groovy-all.jar"

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
        jar.@version.text() == '1.0'

        and:
        jar.@href.text() == "lib/${moduleName}.jar"

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jar of project with SNAPSHOT version'() {
        given:
        gradleVersion = gv

        and:
        version = "1.0-SNAPSHOT"
        runTasksSuccessfully(':generateJnlp')
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
        runTasksSuccessfully(':generateJnlp')
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

    @Unroll
    def '[gradle #gv] property jnlp.versionEnabled is set to true'() {
        given:
        gradleVersion = gv

        when:
        def property = jnlp.resources.property.find { it.@name == 'jnlp.versionEnabled' }

        then:
        property.@value.text() == 'true'

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jars are copied'() {
        given:
        gradleVersion = gv

        expect:
        directory('build/jnlp/lib').list { file, name -> name.endsWith '.jar' }.sort() == [
                'groovy-all__V2.3.1.jar',
                "${moduleName}__V1.0.jar"
        ].sort()

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] main-class is set'() {
        given:
        gradleVersion = gv

        expect:
        jnlp.'application-desc'.@'main-class'.text() == 'de.gliderpilot.jnlp.test.HelloWorld'

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] main-jar is marked'() {
        given:
        gradleVersion = gv

        expect:
        jnlp.resources.jar.find { it.@href =~ "$moduleName" }.@main.text() == 'true'

        where:
        gv << gradleVersions
    }
}
