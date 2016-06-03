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

import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarFile

class JnlpTask extends DefaultTask {

    @InputFiles
    Configuration from

    @Input
    boolean isUseVersions() {
        project.jnlp.useVersions
    }

    @Input
    boolean isUsePack200() {
        project.jnlp.usePack200
    }

    @Input
    def getJnlpParams() {
        project.jnlp.jnlpParams
    }

    @Input
    def getJ2seParams() {
        project.jnlp.j2seParams
    }

    @Input
    def getMainClassName() {
        project.jnlp.mainClassName
    }

    @OutputFile
    File getJnlpFile() {
        return new File(project.buildDir, "${project.jnlp.destinationPath}/${project.jnlp.jnlpParams.href ?: 'launch.jnlp'}")
    }

    @TaskAction
    void generateJnlp() {
        def outputFile = jnlpFile
        outputFile.parentFile.eachFileMatch(~/.*\.jnlp/) { it.delete() }
        MarkupBuilder xml = new MarkupBuilder(outputFile.newPrintWriter('UTF-8'))
        xml.mkp.xmlDeclaration(version: '1.0', encoding: 'UTF-8')
        xml.jnlp(jnlpParams) {
            delegate.with project.jnlp.withXmlClosure
            resources {
                j2se(j2seParams)
                from.resolve().findAll { it.name.endsWith('.jar') }.each { File jarFile ->
                    jar(jarParams(jarFile))
                }

                // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/avoidingUnnecessaryUpdateChecks.html
                if (useVersions)
                    property name: 'jnlp.versionEnabled', value: 'true'
                // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/reducingDownloadTime.html
                if (usePack200)
                    property name: 'jnlp.packEnabled', value: 'true'
            }
            delegate.with project.jnlp.desc
        }
    }

    Map<String, String> jarParams(File file) {
        ResolvedArtifact artifact = from.resolvedConfiguration.resolvedArtifacts.find {
            it.extension == 'jar' && it.file.name == file.name
        }
        Map<String, String> jarParams = artifact ? jarParams(artifact) : [href: "lib/${file.name}"]
        if (containsMainClass(file))
            jarParams.main = 'true'
        return jarParams
    }

    Map<String, String> jarParams(ResolvedArtifact artifact) {
        String version = artifact.moduleVersion.id.version
        if (useVersions && !version.endsWith("-SNAPSHOT"))
            if (artifact.classifier == null)
                [href: "lib/${artifact.name}.jar", version: "${version}${project.jnlp.versionAppendix.call()}"]
            else
                [href: "lib/${artifact.name}-${artifact.classifier}.jar", version: "${version}${project.jnlp.versionAppendix.call()}"]
        else if (artifact.classifier == null)
            [href: "lib/${artifact.name}__V${version}${project.jnlp.versionAppendix.call()}.jar"]
        else
            [href: "lib/${artifact.name}-${artifact.classifier}__V${version}${project.jnlp.versionAppendix.call()}.jar"]
    }

    boolean containsMainClass(File file) {
        if (mainClassName)
            new JarFile(file).getEntry(mainClassName.replace('.', '/') + '.class')
    }
}
