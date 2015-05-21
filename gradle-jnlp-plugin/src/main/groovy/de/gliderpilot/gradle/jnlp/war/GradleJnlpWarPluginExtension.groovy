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
package de.gliderpilot.gradle.jnlp.war

import groovy.transform.PackageScope
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.archive.ZipFileTree

import javax.inject.Inject
import java.util.regex.Matcher

class GradleJnlpWarPluginExtension {

    /* should allow following dsl:
    apply plugin: 'de.gliderpilot.jnlp-war'
    jnlp {
        versions {
            v1 'org.example:application:1.0:webstart@zip'
            v2 'org.example:application:2.0:webstart@zip'
            v3 'org.example:application:3.0:webstart@zip'
        }
        launchers {
            v2 {
                rename 'launch.jnlp', 'launch_v2.jnlp'
                jardiff {
                    from v1
                }
            }
            v3 {
                jardiff {
                    from v1, v2
                }
            }
        }
    }

    */

    Project project

    CopySpec launchersSpec

    String href = '$$name'
    String codebase = '$$codebase'

    Map<Configuration, Launcher> launchers = [:].withDefault {
        new Launcher(it)
    }

    // key is the launcher, value is the jardiff task
    Map<Configuration, Task> jardiffTasks = [:].withDefault { configuration ->
        def task = project.task("create${configuration.name.capitalize()}Jardiffs", type: JarDiffTask) {
            into "$project.buildDir/tmp/jardiff/$configuration.name"
            newVersion configuration
        }
        project.war {
            from(task.outputs.files) {
                into 'lib'
            }
        }
        task
    }

    @Inject
    GradleJnlpWarPluginExtension(Project project) {
        this.project = project
        launchersSpec = project.copySpec { CopySpec copySpec ->
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            includeEmptyDirs = false
        }
        project.war {
            with launchersSpec
            doFirst {
                launchers.values()*.resolve()
                launchersSpec.eachFile {
                    it.path = it.path.replaceAll('.*?/(.*)', '$1')
                }
            }
        }
    }

    void from(Project jnlpProject) {
        versions {
            "${jnlpProject.version}" project.dependencies.project(path: jnlpProject.path, configuration: 'webstartZip')
        }
        launchers {
            "${jnlpProject.version}"()
        }
        def webstartZipTask = jnlpProject.tasks.getByName('webstartDistZip')
        project.war.dependsOn webstartZipTask
    }

    void versions(Closure closure) {
        closure.delegate = new Versions()
        closure()
    }

    void launchers(Closure closure) {
        closure.delegate = new Launchers()
        closure()
    }

    private class Versions {

        @Override
        Object invokeMethod(String name, Object args) {
            project.configurations.maybeCreate(name)
            return project.dependencies.invokeMethod(name, args)
        }
    }

    private class Launchers {

        @Override
        Object invokeMethod(String name, Object args) {
            Launcher launcher = launchers[project.configurations.getByName(name)]

            if (args) {
                Closure closure = args[0]
                closure.delegate = launcher
                closure()
            }
            return null
        }
    }

    private class Launcher {

        Configuration configuration

        String oldJnlpFileName
        String newJnlpFileName

        def filterJnlpFiles = { line ->
            if (line.startsWith('<jnlp')) {
                line = line.replaceAll(/href=["'].*?["']/, Matcher.quoteReplacement("href='${href ?: newJnlpFileName}'"))
                if (codebase) {
                    line = line.replaceAll(/codebase=["'].*?["']/, Matcher.quoteReplacement("codebase='${codebase}'"))
                    if (!line.contains('codebase='))
                        line = line.replace('<jnlp', "<jnlp codebase='${codebase}'")
                }
            }
            line.contains('jnlp.versionEnabled') || line.contains('jnlp.packEnabled') ? '' : line
        }

        Launcher(Configuration configuration) {
            this.configuration = configuration
            project.war.inputs.files configuration
        }

        void rename(String from, String to) {
            oldJnlpFileName = from
            newJnlpFileName = to
        }

        void jardiff(Closure closure) {
            closure.delegate = new JarDiff(configuration)
            closure()
        }

        void resolve() {
            def zipTree = project.zipTree(configuration.singleFile)
            launchersSpec.from(zipTree) {
                include '**/*.jnlp'
                filter filterJnlpFiles
                if (oldJnlpFileName && newJnlpFileName)
                    rename oldJnlpFileName, newJnlpFileName
            }
            launchersSpec.from(zipTree) {
                exclude '**/*.jnlp'
            }
        }

    }

    private class JarDiff {
        Configuration configuration

        JarDiff(Configuration configuration) {
            this.configuration = configuration
        }

        void from(String... versions) {
            from(versions.collect { project.configurations."$it" } as Configuration[])
        }

        void from(Configuration... versions) {
            versions.each {
                jardiffTasks[configuration].oldVersion it
            }
        }

    }

}
