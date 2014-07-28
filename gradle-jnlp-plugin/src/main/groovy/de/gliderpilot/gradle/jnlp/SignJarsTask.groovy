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
package de.gliderpilot.gradle.jnlp

import groovyx.gpars.GParsPool

import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import java.util.jar.Manifest
import java.util.jar.Attributes


class SignJarsTask extends AbstractCopyJarsTask {


    @TaskAction
    void signJars(IncrementalTaskInputs inputs) {
        def jarsToSign = []
        inputs.outOfDate {
            jarsToSign << it.file
        }
        inputs.removed {
            project.delete(new File(into, newName(it.name)))
        }
        GParsPool.withPool(threadCount()) {
            jarsToSign.eachParallel { jarToSign ->
                jarToSign = copyUnsignAndAlterManifest(jarToSign)
                // AntBuilder is not thread-safe, therefore we need to create
                // a new one for each file
                AntBuilder ant = project.createAntBuilder()
                def signJarParams = new HashMap(project.jnlp.signJarParams)
                signJarParams.jar = jarToSign
                ant.signjar(signJarParams)
            }
        }
    }

    File copyUnsignAndAlterManifest(File input) {
        File output = new File(into, newName(input.name))
        new JarInputStream(input.newInputStream()).withStream { is ->
            Manifest manifest = is.manifest ?: new Manifest()
            // ensure either Manifest-Version or Signature-Version is set, otherwise the manifest will not be written
            if (!manifest.mainAttributes.getValue('Manifest-Version') && !manifest.mainAttributes.getValue('Signature-Version'))
                manifest.mainAttributes.putValue('Manifest-Version', '1.0')

            project.jnlp.signJarRemovedManifestEntries.each { key ->
                manifest.mainAttributes.remove(new Attributes.Name(key))
            }
            project.jnlp.signJarAddedManifestEntries.each { key, value ->
                manifest.mainAttributes.putValue(key, value)
            }
            new JarOutputStream(output.newOutputStream(), manifest).withStream { os ->
                JarEntry entry = null
                while((entry = is.nextEntry) != null) {
                    if (entry.name ==~ '(?i)META-INF/.*[.](?:DSA|SF|RSA)')
                        continue
                    os.putNextEntry(new JarEntry(entry.name))
                    os << is
                    os.closeEntry()
                }
            }
        }
        return output
    }

    int threadCount() {
        int threadCount = project.gradle.startParameter.parallelThreadCount
        if (threadCount == -1)
            return Runtime.runtime.availableProcessors()
        if (threadCount == 0)
            return 1
        return threadCount
    }

}
