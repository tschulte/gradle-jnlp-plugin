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

import org.gradle.api.file.DuplicatesStrategy
import spock.lang.Issue

@Issue("https://github.com/tschulte/gradle-jnlp-plugin/issues/37")
class SignJarsDuplicateEntriesSpec extends AbstractJnlpIntegrationSpec {

    def setup() {
        writeHelloWorld('de.gliderpilot.jnlp.test')
        file('src/main/resources/test')
        file('src/test/resources/test')
        buildFile << """\
            apply plugin: 'java'
            apply plugin: 'application'
            jnlp {
                usePack200 = false
                signJarRemovedNamedManifestEntries = ".*"
            }
            mainClassName = 'de.gliderpilot.jnlp.test.HelloWorld'
            jar {
                from sourceSets.test.output
            }
            """.stripIndent()
        enableJarSigner()
    }

    def 'cannot sign jar without DuplicatesStrategy'() {
        expect:
        runTasksWithFailure(':createWebstartDir')
    }

    def 'cannot sign jar with DuplicatesStrategy.INCLUDE'() {
        given:
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        expect:
        runTasksWithFailure(':createWebstartDir')
    }

    def 'cannot sign jar with DuplicatesStrategy.FAIL'() {
        given:
        duplicatesStrategy = DuplicatesStrategy.FAIL

        expect:
        runTasksWithFailure(':createWebstartDir')
    }

    def 'cannot sign jar with duplicatesStrategy.WARN'() {
        given:
        duplicatesStrategy = DuplicatesStrategy.WARN

        expect:
        runTasksWithFailure(':createWebstartDir')
    }

    def 'can sign jar with DuplicatesStrategy.EXCLUDE'() {
        given:
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        expect:
        runTasksSuccessfully(':createWebstartDir')
    }

    void setDuplicatesStrategy(DuplicatesStrategy duplicatesStrategy) {
        buildFile << """\
            tasks.signJars {
                duplicatesStrategy = DuplicatesStrategy.${duplicatesStrategy}
            }
            """.stripIndent()
    }

}
