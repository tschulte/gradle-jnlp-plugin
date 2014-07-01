package de.gliderpilot.gradle.jnlp

import org.gradle.api.Project

import org.gradle.tooling.*
import org.gradle.api.Task


class IntegrationTestProject {

    static Project enhance(Project project) {
        IntegrationTestProject intTest = new IntegrationTestProject(project)
        project.metaClass.getExecutedTasks = { -> intTest.executedTasks }
        project.metaClass.run = intTest.&run
        project.metaClass.createFile = intTest.&createFile
        project.metaClass.getBuildFile = intTest.&getBuildFile
        project.metaClass.getSettingsFile = intTest.&getSettingsFile
        project.metaClass.wasExecuted = intTest.&wasExecuted
        project.metaClass.wasUpToDate = intTest.&wasUpToDate
        project
    }

    private Project project
    private List<String> executedTasks = []

    private IntegrationTestProject(Project project) {
        this.project = project
    }

    void run(String... args) {
        assert buildFile.exists()
        executedTasks.clear()
        GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(project.projectDir)
        ProjectConnection connection = connector.connect()
        try {
            BuildLauncher buildLauncher = connection.newBuild()
            buildLauncher.addProgressListener({
                if (it.description.startsWith('Execute :'))
                    executedTasks.addAll(Arrays.asList((it.description - 'Execute ').split()))
            } as ProgressListener)
            buildLauncher.forTasks(args).run()
        } finally {
            println executedTasks
            connection.close()
        }
    }

    File createFile(String path) {
        File file = project.file(path)
        if (!file.exists()) {
            assert file.parentFile.mkdirs() || file.parentFile.exists()
            file.createNewFile()
        }
        file
    }

    File getBuildFile() {
        project.buildscript.sourceFile ?: project.file("build.gradle")
    }

    File getSettingsFile() {
        project.file("settings.gradle")
    }

    boolean wasExecuted(String taskPath) {
        executedTasks.any { it == taskPath }
    }

}
