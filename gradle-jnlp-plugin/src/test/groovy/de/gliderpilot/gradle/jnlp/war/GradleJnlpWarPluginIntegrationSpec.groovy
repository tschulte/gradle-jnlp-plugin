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
package de.gliderpilot.gradle.jnlp.war

import nebula.test.IntegrationSpec
import spock.lang.Ignore

class GradleJnlpWarPluginIntegrationSpec extends IntegrationSpec {

    File warBuildFile

    def setup() {
        writeHelloWorld('de.gliderpilot.gradle.jnlp.test')
        buildFile << '''\
            apply plugin: 'application'
            apply plugin: 'de.gliderpilot.jnlp'
            apply plugin: 'maven-publish'
            group = 'de.gliderpilot.gradle.jnlp.test'
            mainClassName = 'de.gliderpilot.gradle.jnlp.test.HelloWorld'
            publishing {
                repositories {
                    maven {
                        url "repo"
                    }
                }
                publications {
                    mavenJava(MavenPublication) {
                        from components.java
                        artifact webstartDistZip {
                            classifier "webstart"
                        }
                    }
                }
            }
            jnlp {
                signJarParams = [alias: 'myalias', storepass: 'mystorepass',
                    keystore: 'file:keystore.ks']
            }
            project(':war') { warProject ->
                apply plugin: 'de.gliderpilot.jnlp-war'
                repositories {
                    maven {
                        url "$rootDir/repo"
                    }
                    mavenLocal()
                    jcenter()
                }
                jnlpWar {
                    from rootProject
                }
                task unzipWar(type: Sync) {
                    from zipTree(war.outputs.files.singleFile)
                    into "build/tmp/warContent"
                }
                war.finalizedBy unzipWar
            }
            if (!file('keystore.ks').exists())
                ant.genkey(alias: 'myalias', storepass: 'mystorepass', dname: 'CN=Ant Group, OU=Jakarta Division, O=Apache.org, C=US',
                           keystore: 'keystore.ks')
        '''.stripIndent()
        warBuildFile = new File(addSubproject('war'), 'build.gradle')
        version = "1.0"
    }

    def setVersion(String version) {
        file('gradle.properties').text = """\
            version=$version
        """.stripIndent()
    }

    def "war contains jnlp-servlet"() {
        when:
        runTasksSuccessfully('build')

        then:
        file("war/build/tmp/warContent/WEB-INF/lib/").listFiles({ it.name.startsWith('jnlp-servlet-') } as FileFilter)
    }

    def 'codebase and href are set to $$codebase and $$name'() {
        when:
        runTasksSuccessfully('build')
        def jnlp = new XmlSlurper().parse(file('war/build/tmp/warContent/launch.jnlp'))

        then:
        jnlp.@href.text() == '$$name'
        and:
        jnlp.@codebase.text() == '$$codebase'
    }

    def 'codebase is changed to $$codebase'() {
        when:
        buildFile << '''\
            jnlp {
                codebase 'http://example.com'
            }
        '''.stripIndent()
        runTasksSuccessfully('build')
        def jnlp = new XmlSlurper().parse(file('war/build/tmp/warContent/launch.jnlp'))

        then:
        jnlp.@codebase.text() == '$$codebase'
    }

    def 'codebase and href can be set to different values'() {
        when:
        warBuildFile << '''\
            jnlpWar {
                codebase = '$$context'
                href = '$$codebase$$name'
            }
        '''.stripIndent()
        runTasksSuccessfully('build')
        def jnlp = new XmlSlurper().parse(file('war/build/tmp/warContent/launch.jnlp'))

        then:
        jnlp.@href.text() == '$$codebase$$name'
        and:
        jnlp.@codebase.text() == '$$context'
    }

    def 'codebase is changed to different value'() {
        when:
        buildFile << '''\
            jnlp {
                codebase 'http://example.com'
            }
        '''.stripIndent()
        warBuildFile << '''\
            jnlpWar {
                codebase = '$$context'
                href = '$$codebase$$name'
            }
        '''.stripIndent()
        runTasksSuccessfully('build')
        def jnlp = new XmlSlurper().parse(file('war/build/tmp/warContent/launch.jnlp'))

        then:
        jnlp.@codebase.text() == '$$context'
    }

    def "war contains webstart files from project"() {
        expect:
        runTasksSuccessfully("build")
        fileExists("war/build/libs/war-1.0.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0.jar.pack.gz")
    }

    def "war can also contain old version and rename works"() {
        when:
        runTasksSuccessfully('build', 'publish')
        version = '1.1'
        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.0" "$rootProject.group:$rootProject.name:1.0:webstart@zip"
                }
                launchers {
                    "1.0" {
                        rename "launch.jnlp", "launch-1.0.jnlp"
                    }
                }
            }
        '''
        runTasksSuccessfully("build")

        then:
        fileExists("war/build/libs/war-1.1.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        fileExists("war/build/tmp/warContent/launch-1.0.jnlp")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0.jar.pack.gz")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.1.jar.pack.gz")
    }

    def "incremental build works"() {
        when:
        runTasksSuccessfully('build', 'publish')

        version = '1.1'
        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.0" "$rootProject.group:$rootProject.name:1.0:webstart@zip"
                }
                launchers {
                    "1.0" {
                        rename "launch.jnlp", "launch-1.0.jnlp"
                    }
                }
            }
        '''
        runTasksSuccessfully('build', 'publish')

        version = '1.2'
        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.1" "$rootProject.group:$rootProject.name:1.1:webstart@zip"
                }
                launchers {
                    "1.1" {
                        rename "launch.jnlp", "launch-1.1.jnlp"
                    }
                }
            }
        '''
        runTasksSuccessfully("build", 'publish')

        version = '1.3'
        runTasksSuccessfully('build', 'publish')

        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.2" "$rootProject.group:$rootProject.name:1.2:webstart@zip"
                }
                launchers {
                    "1.2" {
                        rename "launch.jnlp", "launch-1.2.jnlp"
                    }
                }
            }
        '''
        runTasksSuccessfully("build", 'publish')

        then:
        fileExists("war/build/libs/war-1.3.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        !fileExists("war/build/tmp/warContent/launch-1.0.jnlp")
        !fileExists("war/build/tmp/warContent/launch-1.1.jnlp")
        fileExists("war/build/tmp/warContent/launch-1.2.jnlp")
        !fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0.jar.pack.gz")
        !fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.1.jar.pack.gz")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.2.jar.pack.gz")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.3.jar.pack.gz")
    }

    def "launcher for project can be further refined"() {
        setup:
        runTasksSuccessfully('build', 'publish')
        setVersion("1.1")

        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.0" "${rootProject.group}:${rootProject.name}:1.0:webstart@zip"
                }
                launchers {
                    "1.0" {
                        rename "launch.jnlp", "launch-1.0.jnlp"
                    }
                    "1.1" {
                        rename "launch.jnlp", "launch-1.1.jnlp"
                    }
                }
            }
        '''.stripIndent()

        expect:
        runTasksSuccessfully("build")
        fileExists("war/build/libs/war-1.1.war")
        fileExists("war/build/tmp/warContent/launch-1.0.jnlp")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0.jar.pack.gz")
    }

    def "jardiffs are created"() {
        when:
        runTasksSuccessfully('build', 'publish')
        version = '1.1'
        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.0" "$rootProject.group:$rootProject.name:1.0:webstart@zip"
                }
                launchers {
                    "1.1" {
                        jardiff {
                            from "1.0"
                        }
                    }
                }
            }
        '''
        runTasksSuccessfully("build")

        then:
        fileExists("war/build/libs/war-1.1.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0__V1.1.diff.jar.pack.gz")
    }

    def "no exception with jardiff and new dependency"() {
        when:
        runTasksSuccessfully('build', 'publish')
        version = '1.1'
        new File(addSubproject('sub'), 'build.gradle') << 'apply plugin: "java"'
        buildFile << 'dependencies { compile project(":sub") }'
        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.0" "$rootProject.group:$rootProject.name:1.0:webstart@zip"
                }
                launchers {
                    "1.1" {
                        jardiff {
                            from "1.0"
                        }
                    }
                }
            }
        '''
        runTasksSuccessfully("build")

        then:
        fileExists("war/build/libs/war-1.1.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        fileExists("war/build/tmp/warContent/lib/sub__V1.1.jar.pack.gz")
    }

    def "no exception with jardiff and removed dependency"() {
        when:
        runTasksSuccessfully('build', 'publish')
        version = '1.1'
        new File(addSubproject('sub'), 'build.gradle') << 'apply plugin: "java"'
        buildFile << 'dependencies { compile project(":sub") }'
        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.0" "$rootProject.group:$rootProject.name:1.0:webstart@zip"
                }
                launchers {
                    "1.0" {
                        jardiff {
                            from "1.1"
                        }
                    }
                }
            }
        '''
        runTasksSuccessfully("build")

        then:
        fileExists("war/build/libs/war-1.1.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        fileExists("war/build/tmp/warContent/lib/sub__V1.1.jar.pack.gz")
    }
}
