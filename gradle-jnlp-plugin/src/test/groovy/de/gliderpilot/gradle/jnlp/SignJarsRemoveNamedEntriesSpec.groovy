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

import spock.lang.Issue
import spock.lang.Unroll

import java.util.jar.JarFile

@Issue("https://github.com/tschulte/gradle-jnlp-plugin/issues/34")
class SignJarsRemoveNamedEntriesSpec extends AbstractJnlpIntegrationSpec {

    def setup() {
        writeHelloWorld('de.gliderpilot.jnlp.test')
        buildFile << """\
            apply plugin: 'java'
            apply plugin: 'application'

            jnlp {
                usePack200 = false
                signJarRemovedNamedManifestEntries = ".*"
            }

            dependencies {
                compile 'commons-httpclient:commons-httpclient:3.1'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            """.stripIndent()
        enableJarSigner()
        runTasksSuccessfully(':createWebstartDir')
    }

    @Unroll
    def '[gradle #gv] manifest has no named entry "org/apache/commons/httpclient"'() {
        given:
        gradleVersion = gv

        and:
        def manifest = new JarFile(file('build/jnlp/lib/commons-httpclient__V3.1-myalias.jar')).manifest

        expect:
        !manifest.entries.containsKey("org/apache/commons/httpclient")

        where:
        gv << gradleVersions
    }

}
