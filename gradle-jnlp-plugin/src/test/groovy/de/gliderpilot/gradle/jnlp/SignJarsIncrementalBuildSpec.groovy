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

import spock.lang.Shared
import spock.lang.Unroll

@Unroll
class SignJarsIncrementalBuildSpec extends AbstractPluginSpecification {

    def setupSpec() {
        IntegrationTestProject.enhance(project())
        project.settingsFile << """\
            rootProject.name = 'test'
        """.stripIndent()
        project.file('src/main/java/de/gliderpilot/jnlp/test').mkdirs()
        project.file('src/main/java/de/gliderpilot/jnlp/test/Main.java') << """\
            package de.gliderpilot.jnlp.test;
            public class Main {
                public static void main(String[] args) {
                    System.out.println("test");
                }
            }
        """.stripIndent()
    }

    def buildWithDependency(String dependency) {
        project.buildFile.text = """\
            apply plugin: 'java'
            apply plugin: 'application'
            apply plugin: 'de.gliderpilot.jnlp'

            buildscript {
                repositories {
                    jcenter()
                }
                dependencies {
                    classpath 'org.codehaus.gpars:gpars:1.2.1'
                    classpath files('${new File('build/classes/main').absoluteFile.toURI()}')
                    classpath files('${new File('build/resources/main').absoluteFile.toURI()}')
                }
            }

            jnlp {
                signJarParams = [alias: 'myalias', storepass: 'mystorepass',
                    keystore: 'file:keystore.ks']
            }

            version = '1.0'

            repositories {
                jcenter()
            }
            dependencies {
                ${dependency ? "compile '$dependency'" : ""}
            }
            mainClassName = 'de.gliderpilot.jnlp.test.Main'
            task genkey << {
                if (!file('keystore.ks').exists())
                    ant.genkey(alias: 'myalias', storepass: 'mystorepass', dname: 'CN=Ant Group, OU=Jakarta Division, O=Apache.org, C=US',
                               keystore: 'keystore.ks')
            }
        """.stripIndent()

        project.run ':genkey', ':createWebstartDir'
    }

    def 'when version of dependency changes, the old version is removed from the lib directory'() {
        when:
        buildWithDependency 'org.swinglabs:jxlayer:3.0.3'

        and:
        buildWithDependency 'org.swinglabs:jxlayer:3.0.4'

        then:
        new File(project.buildDir, 'jnlp/lib').list().findAll { it.startsWith('jxlayer') }.size() == 1
    }

    def 'when dependency is removed, the old version is removed from the lib directory'() {
        when:
        buildWithDependency 'org.swinglabs:jxlayer:3.0.3'

        and:
        buildWithDependency null

        then:
        !new File(project.buildDir, 'jnlp/lib').list().findAll { it.startsWith('jxlayer') }
    }

}
