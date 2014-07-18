[![Build Status](https://travis-ci.org/tschulte/gradle-jnlp-plugin.svg?branch=master)](https://travis-ci.org/tschulte/gradle-jnlp-plugin)

Gradle plugin to create webstart files
--------------------------------------

To use in a griffon 2 application add following to your build.gradle:

```groovy
buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath 'de.gliderpilot.gradle.jnlp:gradle-jnlp-plugin:+'
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

Than execute

./gradlew genkey

followed by

./gradlew createWebstart

which will create webstart files at build/tmp/jnlp.

To launch the application with webstart, call
javaws build/tmp/jnlp/launch.jnlp (you must first set your java security settings to medium, or use a real certificate).
