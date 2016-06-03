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
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

@ParallelizableTask
class IncrementalCleanupSignedJarsTask extends DefaultTask {

    @InputFiles
    FileCollection from

    @OutputDirectory
    File getLibDir() {
        new File(project.buildDir, project.jnlp.destinationPath + '/lib')
    }

    @TaskAction
    void cleanup(IncrementalTaskInputs inputs) {
        if (!inputs.incremental)
            project.delete(libDir.listFiles())
        inputs.outOfDate {
            // nothing to do
        }
        inputs.removed {
            deleteOutputFile(it.file.name)
        }
    }

    void deleteOutputFile(String fileName) {
        libDir.listFiles().find {
            fileName == (it.name - '.pack.gz').replace('__V', '-')
        }?.delete()
    }

}
