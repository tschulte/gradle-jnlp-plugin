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

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory

abstract class AbstractCopyJarsTask extends DefaultTask {

    @InputFiles
    Configuration from

    @OutputDirectory
    File into

    String newName(String fileName) {
        ResolvedArtifact artifact = from.resolvedConfiguration.resolvedArtifacts.find { it.extension == 'jar' && it.file.name == fileName }
        artifact ? "${artifact.name}__V${artifact.moduleVersion.id.version}.jar" : fileName
    }
}
