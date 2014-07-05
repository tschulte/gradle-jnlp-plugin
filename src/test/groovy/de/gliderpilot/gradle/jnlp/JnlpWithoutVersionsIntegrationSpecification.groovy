package de.gliderpilot.gradle.jnlp

import spock.lang.Shared
import spock.lang.Unroll

@Unroll
class JnlpWithoutVersionsIntegrationSpecification extends AbstractPluginSpecification {

    @Shared
    def jnlp

    def setupSpec() {
        IntegrationTestProject.enhance(project())
        project.buildFile << """\
            apply plugin: 'groovy'
            apply plugin: 'application'
            apply plugin: 'de.gliderpilot.jnlp'
            //apply plugin: 'jetty'

            buildscript {
                dependencies {
                    classpath files('${new File('build/classes/main').absolutePath}')
                    classpath files('${new File('build/resources/main').absolutePath}')
                }
            }

            jnlp {
                useVersions = false
            }

            version = '1.0'
            targetCompatibility = '1.6'

            repositories {
                jcenter()
            }
            dependencies {
                compile 'org.codehaus.groovy:groovy-all:2.3.1'
            }
            mainClassName = 'de.gliderpilot.jnlp.test.Main'
        """.stripIndent()

        project.settingsFile << """\
            rootProject.name = 'test'
        """.stripIndent()
        project.file('src/main/groovy/de/gliderpilot/jnlp/test').mkdirs()
        project.file('src/main/groovy/de/gliderpilot/jnlp/test/Main.groovy') << """\
            package de.gliderpilot.jnlp.test
            class Main {
                static main(args) {
                    println "test"
                }
            }
        """.stripIndent()
        project.run ':generateJnlp', ':copyJars'
        def jnlpFile = project.file('build/tmp/jnlp/launch.jnlp')
        jnlp = new XmlSlurper().parse(jnlpFile)
    }

    def cleanupSpec() {
        project.copy {
            from project.file("build/tmp/jnlp/")
            into '/tmp/'
        }
    }

    def 'jars entry is not empty'() {
        when:
        def jars = jnlp.resources.jar

        then:
        !jars.isEmpty()
    }

    def 'j2se element is contains version information'() {
        expect:
        jnlp.resources.j2se.@version.text() == '1.6'
    }

    def 'jar #artifact has version #version'() {
        when:
        def jar = jnlp.resources.jar.find { it.@href =~ /$artifact/ }

        then:
        jar != null

        and:
        jar.@href.text() == "lib/${artifact}__V${version}.jar"

        where:
        artifact     | version
        'groovy-all' | '2.3.1'
        'test'       | '1.0'
    }
}
