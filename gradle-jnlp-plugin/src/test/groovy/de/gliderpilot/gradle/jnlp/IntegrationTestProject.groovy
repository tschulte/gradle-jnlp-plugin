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
