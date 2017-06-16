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
import nebula.test.functional.ExecutionResult
import spock.lang.Unroll

@Unroll
class AbstractJnlpIntegrationSpec extends IntegrationSpec {

    final static gradleVersions = ["2.4", "3.5", "4.0"]


    def setup() {
        classpathFilter = { url -> !url.path.contains("spock")}
        buildFile << '''\
            apply plugin: 'de.gliderpilot.jnlp'
            repositories {
                jcenter()
            }
            '''.stripIndent()
        version = "1.0"
    }

    def enableJarSigner() {
        buildFile << '''\
            jnlp {
                signJarParams = [alias: 'myalias', storepass: 'mystorepass',
                    keystore: 'file:keystore.ks']
            }
            if (!file('keystore.ks').exists())
                ant.genkey(alias: 'myalias', storepass: 'mystorepass', dname: 'CN=Ant Group, OU=Jakarta Division, O=Apache.org, C=US',
                       keystore: 'keystore.ks')

            '''.stripIndent()
    }

    def setVersion(String version) {
        file('gradle.properties').text = """\
            version=$version
            """.stripIndent()
    }

    def jnlp(String fileName = "launch.jnlp") {
        new XmlSlurper().parse(file("build/jnlp/$fileName"))
    }

}
