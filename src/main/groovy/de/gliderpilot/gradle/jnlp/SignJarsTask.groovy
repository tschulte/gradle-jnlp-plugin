package de.gliderpilot.gradle.jnlp

import groovyx.gpars.GParsPool

import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.java.archives.Manifest as GradleManifest
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import java.util.jar.Manifest as JUJManifest


class SignJarsTask extends DefaultTask {

    @InputFiles
    FileCollection from

    @Input
    String storepass

    @Optional
    @Input
    GradleManifest manifest

    @Optional
    @InputFile
    File keystore

    @Optional
    @Input
    String keypass

    @Optional
    @Input
    String tsaurl

    @Optional
    @Input
    String tsacert

    @OutputDirectory
    File into

    void setProject(Project project) {
        super.setProject(project)
        manifest = project.manifest {
            attributes 'Codebase': '*'
            attributes 'Permissions': 'all-permissions'
            attributes 'Application-Name': "${project.name}"
        }
    }

    void manifest(GradleManifest manifest) {
        this.manifest.from manifest
    }

    @TaskAction
    void signJars(IncrementalTaskInputs inputs) {
        def jarsToSign = []
        inputs.outOfDate {
            jarsToSign << it.file
        }
        inputs.removed {
            project.delete(new File(into, it.name))
        }
        GParsPool.withPool(threadCount()) {
            jarsToSign.eachParallel { jarToSign ->
                jarToSign = copyUnsignAndAlterManifest(jarToSign)
                // AntBuilder is not thread-safe, therefore we need to create
                // a new one for each file
                AntBuilder ant = project.createAntBuilder()
                ant.signjar(jar: jarToSign, alias: alias, storepass: storepass,
                    keystore: keystore, keypass: keypass, tsaurl: tsaurl, tsacert: tsacert,
                    verbose: true)
            }
        }
    }

    private File copyUnsignAndAlterManifest(File input) {
        new JarInputStream(input.newInputStream()).withStream { is ->
            JUJManifest manifest = is.manifest ?: new JUJManifest()
            // ensure either Manifest-Version or Signature-Version is set, otherwise the manifest will not be written
            if (!manifest.mainAttributes.getValue('Manifest-Version') && !manifest.mainAttributes.getValue('Signature-Version'))
                manifest.mainAttributes.putValue('Manifest-Version', '1.0')
            this.attributes.each { key, value ->
                manifest.mainAttributes.putValue(key, value)
            }
            File output = new File(into, input.name)
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
    }

    private int threadCount() {
        int threadCount = project.gradle.startParameter.parallelThreadCount
        if (threadCount == -1)
            return Runtime.runtime.availableProcessors()
        if (threadCount == 0)
            return 1
        return threadCount
    }

}
