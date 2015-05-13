package de.gliderpilot.gradle.jnlp.war

import jnlp.sample.jardiff.JarDiff
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Created by tobias on 5/10/15.
 */
class JarDiffTask extends DefaultTask {

    @InputFiles
    FileCollection oldVersions

    @InputFiles
    FileCollection newVersion

    @OutputDirectory
    File into

    void oldVersion(Configuration configuration) {
        if (oldVersions == null)
            oldVersions = configuration
        else
            oldVersions += configuration
    }

    void newVersion(Configuration configuration) {
        newVersion = configuration
    }

    void into(into) {
        this.into = project.file(into)
    }

    @TaskAction
    void createJardiffs() {
        def toMap = { [file: it, baseName: it.name - ~/__V.*$/, version: it.name.replaceAll(/.*?__V(.*?)\.jar.*/, '$1')] }
        def onlyVersionedJars = { it.name ==~ /.*__V.*\.jar(?:\.pack(?:\.gz)?)?/ }
        def oldFiles = oldVersions.collect {
            project.zipTree(it).filter(onlyVersionedJars).files
        }.flatten().collect(toMap)
        def newFiles = project.zipTree(newVersion.singleFile)
                .filter(onlyVersionedJars)
                .collect(toMap)
                .inject([:]) { map, file ->
                    map[file.baseName] = file
                    map
                }
        oldFiles.each { oldFile ->
            def newFile = newFiles[oldFile.baseName]
            if (newFile && newFile.version != oldFile.version)
                createJarDiff(oldFile, newFile)
        }
    }

    def createJarDiff(oldVersion, newVersion) {
        File oldFile = getJar(oldVersion.file)
        File newFile = getJar(newVersion.file)
        File diffJar = new File(into, "${oldVersion.baseName}__V${oldVersion.version}__V${newVersion.version}.diff.jar")
        diffJar.withOutputStream { os ->
            JarDiff.createPatch(oldFile.canonicalPath, newFile.canonicalPath, os, true)
        }
        if (newVersion.file.name.endsWith('.pack.gz')) {
            // new file is in pack200 format, therefore do also pack the jardiff
            File diffJarPacked = new File("${diffJar}.pack.gz")
            project.exec {
                commandLine "pack200", diffJarPacked, diffJar
            }
            project.delete(diffJar)
            if (diffJarPacked.size() >= newVersion.file.size())
                project.delete(diffJarPacked)
        } else if (diffJar.size() >= newVersion.file.size()) {
            // don't pack, but only keep if smaller than the full file
            project.delete(diffJar)
        }
    }

    File getJar(File file) {
        if (file.name.endsWith('.jar'))
            return file
        File jar = new File("$project.buildDir/tmp/jardiff", file.name - '.pack.gz')
        project.exec {
            commandLine "unpack200", file, jar
        }
        return jar
    }

}
