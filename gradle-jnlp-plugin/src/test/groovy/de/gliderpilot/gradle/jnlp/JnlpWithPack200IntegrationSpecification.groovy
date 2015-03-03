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
class JnlpWithPack200IntegrationSpecification extends AbstractPluginSpecification {

    @Shared
    def jnlp

    def setupSpec() {
        IntegrationTestProject.enhance(project())
        project.buildFile << """\
            apply plugin: 'java'
            apply plugin: 'application'
            apply plugin: 'de.gliderpilot.jnlp'

            //apply plugin: 'jetty'

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
                usePack200 = true
                signJarParams = [alias: 'myalias', storepass: 'mystorepass',
                    keystore: 'file:keystore.ks']
            }

            version = '1.0'

            repositories {
                jcenter()
            }
            dependencies {
                // jxlayer is already signed. This caused problems with usePack200
                compile 'org.swinglabs:jxlayer:3.0.4'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.Main'
            task genkey << {
                ant.genkey(alias: 'myalias', storepass: 'mystorepass', dname: 'CN=Ant Group, OU=Jakarta Division, O=Apache.org, C=US',
                           keystore: 'keystore.ks')
            }
        """.stripIndent()

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
        project.run ':genkey', ':createWebstartDir'
        def jnlpFile = project.file('build/jnlp/launch.jnlp')
        jnlp = new XmlSlurper().parse(jnlpFile)
    }

    def 'generateJnlp task is executed'() {
        expect:
        project.wasExecuted(':generateJnlp')
    }

    def 'signJars task is executed'() {
        expect:
        project.wasExecuted(':signJars')
    }

    def 'property jnlp.packEnabled is set to true'() {
        when:
        def property = jnlp.resources.property.find { it.@name == 'jnlp.packEnabled' }

        then:
        property.@value.text() == 'true'

    }

    def 'jar file is packed with pack200'() {
        expect:
        new File(project.buildDir, "jnlp/lib").list().sort() == ['jxlayer__V3.0.4.jar.pack.gz', 'test__V1.0.jar.pack.gz']
    }
}
