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

import de.gliderpilot.gradle.jnlp.AbstractJnlpIntegrationSpec
import spock.lang.Unroll

class GradleJnlpWarPluginIntegrationSpec extends AbstractJnlpIntegrationSpec {

    File warBuildFile

    def setup() {
        writeHelloWorld('de.gliderpilot.gradle.jnlp.test')
        buildFile << '''\
            apply plugin: 'application'
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
            '''.stripIndent()
        enableJarSigner()
        warBuildFile = new File(addSubproject('war'), 'build.gradle')
        version = "1.0"
    }

    @Unroll
    def '[gradle #gv] war contains jnlp-servlet'() {
        given:
        gradleVersion = gv

        when:
        runTasksSuccessfully('build')

        then:
        file("war/build/tmp/warContent/WEB-INF/lib/").listFiles({ it.name.startsWith('jnlp-servlet-') } as FileFilter)

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] codebase and href are set to $$codebase and $$name'() {
        given:
        gradleVersion = gv

        when:
        runTasksSuccessfully('build')
        def jnlp = new XmlSlurper().parse(file('war/build/tmp/warContent/launch.jnlp'))

        then:
        jnlp.@href.text() == '$$name'
        and:
        jnlp.@codebase.text() == '$$codebase'

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] codebase is changed to $$codebase'() {
        given:
        gradleVersion = gv

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

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] codebase and href can be set to different values'() {
        given:
        gradleVersion = gv

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

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] codebase is changed to different value'() {
        given:
        gradleVersion = gv

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

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] war contains webstart files from project'() {
        given:
        gradleVersion = gv

        expect:
        runTasksSuccessfully("build")
        fileExists("war/build/libs/war-1.0.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0-myalias.jar.pack.gz")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] war can also contain old version and rename works'() {
        given:
        gradleVersion = gv

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
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0-myalias.jar.pack.gz")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.1-myalias.jar.pack.gz")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] incremental build works'() {
        given:
        gradleVersion = gv

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
        !fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0-myalias.jar.pack.gz")
        !fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.1-myalias.jar.pack.gz")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.2-myalias.jar.pack.gz")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.3-myalias.jar.pack.gz")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] launcher for project can be further refined'() {
        given:
        gradleVersion = gv

        and:
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
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0-myalias.jar.pack.gz")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jardiffs with pack200 are created'() {
        given:
        gradleVersion = gv

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
        def result = runTasksSuccessfully("build")

        then:
        !result.standardOutput.contains("failed to pack")
        fileExists("war/build/libs/war-1.1.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0-myalias__V1.1-myalias.diff.jar.pack.gz")
        // but no diff.jar
        !fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0-myalias__V1.1-myalias.diff.jar")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jardiffs without pack200 are created'() {
        given:
        gradleVersion = gv

        when:
        buildFile << "jnlp.usePack200 = false\n"
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
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0-myalias__V1.1-myalias.diff.jar")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] no exception with jardiff and new dependency'() {
        given:
        gradleVersion = gv

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
        fileExists("war/build/tmp/warContent/lib/sub__V1.1-myalias.jar.pack.gz")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] no exception with jardiff and removed dependency'() {
        given:
        gradleVersion = gv

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
        fileExists("war/build/tmp/warContent/lib/sub__V1.1-myalias.jar.pack.gz")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jardiff from xalan 2.7.1 to 2.7.2 with pack200'() {
        given:
        gradleVersion = gv

        when:
        buildFile << "repositories { jcenter() }\n"
        buildFile << "dependencies { runtime 'xalan:xalan:2.7.1' }\n"
        runTasksSuccessfully('build', 'publish')
        buildFile << "dependencies { runtime 'xalan:xalan:2.7.2' }\n"
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
        def result = runTasksSuccessfully("build")

        then:
        fileExists("war/build/libs/war-1.1.war")
        fileExists("war/build/tmp/warContent/launch.jnlp")
        // somehow pack200 of the xalan jardiff does not work with default params
        // does not even work with java 7
        System.getProperty("java.specification.version") == "1.7" &&
            result.standardOutput.contains("failed to create xalan__V2.7.1-myalias__V2.7.2-myalias.diff.jar") ||
            result.standardOutput.contains("failed to pack xalan__V2.7.1-myalias__V2.7.2-myalias.diff.jar using default params -- retrying with param --effort=0") &&
            // but the pack200.gz file using --effort=0 is not smaller than the new version .pack.gz
            result.standardOutput.contains("xalan__V2.7.1-myalias__V2.7.2-myalias.diff.jar.pack.gz is not smaller than xalan__V2.7.2-myalias.jar.pack.gz")
        // therefore we don't have the diff.jar.pack.gz nor the diff.jar
        !fileExists("war/build/tmp/warContent/lib/xalan__V2.7.1-myalias__V2.7.2-myalias.diff.jar.pack.gz")
        !fileExists("war/build/tmp/warContent/lib/xalan__V2.7.1-myalias__V2.7.2-myalias.diff.jar")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] jardiff from xalan 2.7.1 to 2.7.2 without pack200'() {
        given:
        gradleVersion = gv

        when:
        buildFile << "jnlp.usePack200 = false\n"
        buildFile << "repositories { jcenter() }\n"
        buildFile << "dependencies { runtime 'xalan:xalan:2.7.1' }\n"
        runTasksSuccessfully('build', 'publish')
        buildFile << "dependencies { runtime 'xalan:xalan:2.7.2' }\n"
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
        // diff.jar is smaller than v2.7.2.jar
        fileExists("war/build/tmp/warContent/lib/xalan__V2.7.1-myalias__V2.7.2-myalias.diff.jar")

        where:
        gv << gradleVersions
    }

    @Unroll
    def '[gradle #gv] incremental jardiff build works'() {
        given:
        gradleVersion = gv

        when:
        runTasksSuccessfully('build', 'publish')

        version = '1.1'
        runTasksSuccessfully('build', 'publish')

        version = '1.2'
        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.0" "$rootProject.group:$rootProject.name:1.0:webstart@zip"
                }
                launchers {
                    "1.2" {
                        jardiff {
                            from "1.0"
                        }
                    }
                }
            }
            '''
        runTasksSuccessfully("build")
        warBuildFile.text = '''\
            jnlpWar {
                versions {
                    "1.1" "$rootProject.group:$rootProject.name:1.1:webstart@zip"
                }
                launchers {
                    "1.2" {
                        jardiff {
                            from "1.1"
                        }
                    }
                }
            }
            '''
        runTasksSuccessfully("build")

        then:
        !fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.0-myalias__V1.2-myalias.diff.jar.pack.gz")
        fileExists("war/build/tmp/warContent/lib/${moduleName}__V1.1-myalias__V1.2-myalias.diff.jar.pack.gz")

        where:
        gv << gradleVersions
    }

}
