Gradle plugin to create webstart files
--------------------------------------

To use in a griffon 2 application:

cd gradle-jnlp-plugin

./gradlew publishToMavenLocal

cd griffon-app

add

```groovy
buildscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        classpath 'de.gliderpilot.gradle.jnlp:gradle-jnlp-plugin:+'
        classpath 'org.codehaus.gpars:gpars:1.2.1'
    }
}

apply plugin: 'de.gliderpilot.jnlp'

jnlp {
    useVersions = true
    withXml {
        information {
            title project.name
            vendor project.group ?: project.name
        }
        security {
            'all-permissions'()
        }
    }
    signJarParams = [alias: 'myalias', storepass: 'mystorepass']
}

task genkey << {
    ant.genkey(alias: 'myalias', storepass: 'mystorepass', dname: 'CN=Ant Group, OU=Jakarta Division, O=Apache.org, C=US')
}

```
to your build.gradle

execute

./gradlew genkey

./gradlew createWebstart

javaws build/tmp/jnlp/launch.jnlp (you must first set your java security settings to medium, or use a real certificate)
