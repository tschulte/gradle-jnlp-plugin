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

import groovyx.gpars.GParsPool
import org.gradle.api.file.DuplicateFileCopyingException
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class SignJarsTask extends AbstractCopyJarsTask {

    @Input
    DuplicatesStrategy duplicatesStrategy = DuplicatesStrategy.INCLUDE

    @TaskAction
    void signJars(IncrementalTaskInputs inputs) {
        if (!inputs.incremental)
            project.delete(into.listFiles())

        def jarsToSign = []
        inputs.outOfDate {
            jarsToSign << it.file
        }
        inputs.removed {
            deleteOutputFile(it.file.name)
        }
        GParsPool.withPool(threadCount()) {
            jarsToSign.eachParallel { jarToSign ->
                jarToSign = copyUnsignAndAlterManifest(jarToSign)
                if (project.jnlp.usePack200) {
                    JavaHomeAware.exec(project, "pack200", "--repack", jarToSign)
                }

                // AntBuilder is not thread-safe, therefore we need to create
                // a new one for each file
                AntBuilder ant = project.createAntBuilder()
                def signJarParams = new HashMap(project.jnlp.signJarParams)
                signJarParams.jar = jarToSign
                ant.signjar(signJarParams)

                if (project.jnlp.usePack200) {
                    JavaHomeAware.exec(project, "pack200", "${jarToSign}.pack.gz", jarToSign)
                    project.delete(jarToSign)
                }
            }
        }
    }

    File copyUnsignAndAlterManifest(File input) {
        File output = new File(into, newName(input.name))
        logger.info("Copying " + input + " to " + output)
        JarFile jarFile = new JarFile(input)
        Manifest manifest = jarFile.manifest ?: new Manifest()
        def removeManifestEntries = { pattern, attributes ->
            def keysToRemove = attributes.keySet().findAll { key -> key.toString() ==~ "(?i)${pattern}" }
            attributes.keySet().removeAll(keysToRemove)
        }
        def removeMainEntries = removeManifestEntries.curry(project.jnlp.signJarRemovedManifestEntries)
        def removeNamedEntries = removeManifestEntries.curry(project.jnlp.signJarRemovedNamedManifestEntries)
        removeMainEntries(manifest.mainAttributes)
        manifest.entries.with {
            values().each { removeNamedEntries(it) }
            // remove all entries without attributes
            keySet().removeAll(entrySet().findAll { it.value.isEmpty() }*.key)
        }
        project.jnlp.signJarAddedManifestEntries.each { key, value ->
            manifest.mainAttributes.putValue(key, value)
        }
        // ensure either Manifest-Version or Signature-Version is set, otherwise the manifest will not be written
        if (!manifest.mainAttributes.getValue('Manifest-Version') && !manifest.mainAttributes.getValue('Signature-Version'))
            manifest.mainAttributes.putValue('Manifest-Version', '1.0')

        new JarOutputStream(output.newOutputStream(), manifest).withStream { os ->
            def entries = [] as Set
            jarFile.entries().each { entry ->
                def duplicate = !entries.add(entry.name)
                if (duplicate && duplicatesStrategy == DuplicatesStrategy.WARN) {
                    logger.warn("Duplicate entry found in jar: " + output + " entry: " + entry.name)
                }
                if (duplicate && duplicatesStrategy == DuplicatesStrategy.FAIL) {
                    throw new DuplicateFileCopyingException("Duplicate entry found in jar: " + output + " entry: " + entry.name)
                }
                if (duplicate && duplicatesStrategy == DuplicatesStrategy.EXCLUDE) {
                    logger.debug("Ignoring duplicate entry in jar: " + output + " entry: " + entry.name)
                } else if (entry.name == JarFile.MANIFEST_NAME || entry.name ==~ "(?i)META-INF/${project.jnlp.signJarFilteredMetaInfFiles}") {
                    logger.debug("Ignoring entry jar: " + output + " entry: " + entry.name)
                } else {
                    logger.debug("copying jar: " + input.name + " entry: " + entry.name)
                    os.putNextEntry(new JarEntry(entry.name))
                    os << jarFile.getInputStream(entry)
                    os.closeEntry()
                }
            }
        }
        return output
    }

    void deleteOutputFile(String fileName) {
        into.listFiles().find {
            def fileParts = (it.name - '.pack.gz').split('__V')
            fileParts.size() == 2 ? fileName - fileParts[0] - (fileParts[1] - project.jnlp.versionAppendix.call()) == '-' : false
        }?.delete()
    }


    int threadCount() {
        return project.gradle.startParameter.maxWorkerCount
    }

}
