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

class SignJarsSpec extends AbstractJnlpIntegrationSpec {

    boolean useVersions = true
    boolean usePack200 = false

    def setup() {
        writeHelloWorld('de.gliderpilot.jnlp.test')
    }

    def buildWithDependency(String dependency) {
        buildFile.text = """\
            apply plugin: 'java'
            apply plugin: 'application'
            apply plugin: 'de.gliderpilot.jnlp'

            jnlp {
                useVersions = $useVersions
                usePack200 = $usePack200
            }
            repositories {
                jcenter()
            }
            dependencies {
                ${dependency ? "compile '$dependency'" : ""}
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            """.stripIndent()
        enableJarSigner()

        runTasksSuccessfully(':createWebstartDir')
    }

    @Unroll
    def '[gradle #gv] when version of dependency changes, the old version is removed from the lib directory'() {
        given:
        gradleVersion = gv

        when:
        buildWithDependency 'org.swinglabs:jxlayer:3.0.3'

        and:
        buildWithDependency 'org.swinglabs:jxlayer:3.0.4'

        then:
        directory('build/jnlp/lib').list().findAll { it.startsWith('jxlayer') }.size() == 1

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] when dependency is removed, the old version is removed from the lib directory'() {
        given:
        gradleVersion = gv

        when:
        buildWithDependency 'org.swinglabs:jxlayer:3.0.3'

        and:
        buildWithDependency null

        then:
        !directory('build/jnlp/lib').list().findAll { it.startsWith('jxlayer') }

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] snapshot version with useVersions'() {
        given:
        gradleVersion = gv

        when:
        version = "1.0-SNAPSHOT"
        buildWithDependency(null)

        then:
        directory("build/jnlp/lib").list { file, name -> name.startsWith(moduleName) }.first() == "${moduleName}__V1.0-SNAPSHOT-myalias.jar"

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] snapshot version without useVersions'() {
        given:
        gradleVersion = gv

        when:
        useVersions = false
        version = "1.0-SNAPSHOT"
        buildWithDependency(null)

        then:
        directory("build/jnlp/lib").list { file, name -> name.startsWith(moduleName) }.first() == "${moduleName}__V1.0-SNAPSHOT-myalias.jar"

        where:
        gv << gradleVersions
    }

}
