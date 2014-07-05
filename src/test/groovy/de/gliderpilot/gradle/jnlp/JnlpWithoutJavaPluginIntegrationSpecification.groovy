package de.gliderpilot.gradle.jnlp

import spock.lang.Shared

class JnlpWithoutJavaPluginIntegrationSpecification extends AbstractPluginSpecification {

    @Shared
    def jnlp

    def setupSpec() {
        IntegrationTestProject.enhance(project())
        project.buildFile << """\
            apply plugin: 'de.gliderpilot.jnlp'

            buildscript {
                dependencies {
                    classpath files('${new File('build/classes/main').absolutePath}')
                    classpath files('${new File('build/resources/main').absolutePath}')
                }
            }

            jnlp {
                mainClassName = 'griffon.javafx.JavaFXGriffonApplication'
            }

            version = '1.0'

            repositories {
                jcenter()
            }
            dependencies {
                jnlp 'org.codehaus.griffon:griffon-javafx:2.0.0.BETA3'
            }
        """.stripIndent()

        project.settingsFile << """\
            rootProject.name = 'test'
        """.stripIndent()
        project.run ':generateJnlp', ':copyJars'
        def jnlpFile = project.file('build/tmp/jnlp/launch.jnlp')
        jnlp = new XmlSlurper().parse(jnlpFile)
    }

    def 'generateJnlp task is executed'() {
        expect:
        project.wasExecuted(':generateJnlp')
    }

    def 'copyJars task is executed'() {
        expect:
        project.wasExecuted(':copyJars')
    }

    def 'jars entry is not empty'() {
        when:
        def jars = jnlp.resources.jar

        then:
        !jars.isEmpty()
    }

    def 'mandatory fields in information block are filled in the jnlp'() {
        expect:
        jnlp.information.title.text() == project.name
        jnlp.information.vendor.text() == project.name
    }

    def 'main-class is set'() {
        expect:
        jnlp.'application-desc'.@'main-class'.text() == 'griffon.javafx.JavaFXGriffonApplication'
    }

    def 'main-jar is marked'() {
        expect:
        jnlp.resources.jar.find { it.@href =~ 'griffon-javafx' }.@main.text() == 'true'
    }

}
