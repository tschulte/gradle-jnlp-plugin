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

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

@ParallelizableTask
class SignJarTask extends DefaultTask {

    private ResolvedArtifact from

    void from(ResolvedArtifact from) {
        this.from = from
    }

    @InputFile
    File getInputFile() {
        from.file
    }

    @Input
    boolean isUsePack200() {
        project.jnlp.usePack200
    }

    @OutputFile
    File getOutputFile() {
        new File(project.buildDir, project.jnlp.destinationPath + '/lib' + newName(usePack200 ? '.jar.pack.gz' : '.jar'))
    }

    @TaskAction
    void signJar() {
        File jarToSign = copyUnsignAndAlterManifest(inputFile)
        if (usePack200) {
            project.exec {
                commandLine "pack200", "--repack", jarToSign
            }
        }

        def signJarParams = new HashMap(project.jnlp.signJarParams)
        if (signJarParams) {
            signJarParams.jar = jarToSign
            ant.signjar(signJarParams)
        }

        if (usePack200) {
            project.exec {
                commandLine "pack200", "${jarToSign}.pack.gz", jarToSign
            }
            project.delete(jarToSign)
        }
    }

    File copyUnsignAndAlterManifest(File input) {
        File output = new File(outputFile.parentFile, newName(".jar"))
        JarFile jarFile = new JarFile(input)
        Manifest manifest = jarFile.manifest ?: new Manifest()
        // ensure either Manifest-Version or Signature-Version is set, otherwise the manifest will not be written
        if (!manifest.mainAttributes.getValue('Manifest-Version') && !manifest.mainAttributes.getValue('Signature-Version'))
            manifest.mainAttributes.putValue('Manifest-Version', '1.0')
        def removeManifestEntries = { attributes ->
            def keysToRemove = attributes.keySet().findAll { key -> key.toString() ==~ "(?i)${project.jnlp.signJarRemovedManifestEntries}" }
            attributes.keySet().removeAll(keysToRemove)
        }
        removeManifestEntries(manifest.mainAttributes)
        manifest.entries.values().each(removeManifestEntries)
        project.jnlp.signJarAddedManifestEntries.each { key, value ->
            manifest.mainAttributes.putValue(key, value)
        }

        new JarOutputStream(output.newOutputStream(), manifest).withStream { os ->
            jarFile.entries().each { entry ->
                if (entry.name != JarFile.MANIFEST_NAME && !(entry.name ==~ "(?i)META-INF/${project.jnlp.signJarFilteredMetaInfFiles}")) {
                    os.putNextEntry(new JarEntry(entry.name))
                    os << jarFile.getInputStream(entry)
                    os.closeEntry()
                }
            }
        }
        return output
    }

    String newName(String extension) {
        if (from.classifier == null)
            "${from.name}__V${from.moduleVersion.id.version}${extension}"
        else
            "${from.name}-${from.classifier}__V${from.moduleVersion.id.version}${extension}"
    }

}
