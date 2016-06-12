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

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Issue
import spock.lang.Unroll

@Issue("https://github.com/tschulte/gradle-jnlp-plugin/issues/32")
class JnlpPlusDependencyManagementIntegrationSpec extends IntegrationSpec {

    def setup() {
        buildFile << '''\
            plugins {
                id "java"
                id "io.spring.dependency-management" version "0.5.7.RELEASE"
            }
            apply plugin: "de.gliderpilot.jnlp"

            dependencyManagement {
                applyMavenExclusions false
            }
        '''.stripIndent()
    }

    def 'no exception when dependency-management plugin is applied'() {
        expect:
        runTasksSuccessfully('dependencies')
    }

}
