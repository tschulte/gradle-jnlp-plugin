[![Build Status](https://api.travis-ci.com/tschulte/gradle-jnlp-plugin.svg?branch=master)](https://travis-ci.com/github/tschulte/gradle-jnlp-plugin)
[![Coverage Status](https://coveralls.io/repos/tschulte/gradle-jnlp-plugin/badge.png?branch=master)](https://coveralls.io/r/tschulte/gradle-jnlp-plugin?branch=master)

## ⚠️ Project discontinued

This project is no longer maintained and no longer builds. It will not be
revived. Reasons:

- **Java Web Start is dead.** Oracle removed it entirely as of Java 11. There
  is no browser plugin and no `javaws` launcher anymore. The only remaining
  option is the community-maintained [OpenWebStart](https://openwebstart.com/),
  which is a niche legacy-support tool, not an active ecosystem.
- **Pack200 is gone.** The `Pack200`/`Unpack200` tools and the
  `java.util.jar.Pack200` API, which this plugin relies on, were removed from
  the JDK in Java 14 (JEP 367).
- **The build toolchain has rotted away.** The project publishes to JCenter
  and Bintray, both shut down in 2021/2022, and depends on a long list of
  unmaintained plugins and libraries.
- The plugin targets `sourceCompatibility 1.6`, which current JDKs can no
  longer even compile.

Reviving this would amount to a rewrite rather than a fix: dropping Pack200
support, rebasing JNLP generation on OpenWebStart (if still desired at all),
moving publishing off JCenter/Bintray, and raising the Java baseline. Given
that Java Web Start itself is obsolete, that effort doesn't seem worthwhile.

If you still need to package a legacy Java Web Start application, consider
using [OpenWebStart](https://openwebstart.com/) directly, or fork this
repository at your own risk.

---

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

which will create webstart files at build/jnlp.

To launch the application with webstart, call javaws build/jnlp/launch.jnlp (you must first set your java
security settings to medium, or use a real certificate).
