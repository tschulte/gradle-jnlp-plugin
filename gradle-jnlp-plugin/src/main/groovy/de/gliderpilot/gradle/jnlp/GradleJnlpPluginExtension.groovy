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

import org.gradle.api.JavaVersion
import groovy.xml.*

import javax.inject.Inject

import org.gradle.api.Project

class GradleJnlpPluginExtension {

    private GradleJnlpPlugin plugin
    private Project project

    String destinationPath = 'jnlp'

    // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/avoidingUnnecessaryUpdateChecks.html
    boolean useVersions = true

    // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/reducingDownloadTime.html
    boolean usePack200 = true

    // not needed, if application-plugin is applied
    String mainClassName



    Map<String, String> jnlpParams = [spec: '7.0', href: 'launch.jnlp']
    Map<String, String> j2seParams

    Map<String, String> signJarParams = [:]
    Map<String, String> signJarAddedManifestEntries
    String signJarFilteredMetaInfFiles = '(?:SIG-.*|.*[.](?:DSA|SF|RSA)|INDEX.LIST)'
    String signJarRemovedManifestEntries = '(?:Trusted-Only|Trusted-Library)'
    String signJarRemovedNamedManifestEntries = '(?:.*-Digest)'

    Closure withXmlClosure
    Closure desc

    @Inject
    GradleJnlpPluginExtension(GradleJnlpPlugin plugin, Project project) {
        this.plugin = plugin
        this.project = project
        if (project.version != 'unspecified') {
            jnlpParams.version = project.version
        }
        signJarAddedManifestEntries = [
            'Codebase': '*',
            'Permissions': 'all-permissions',
            'Application-Name': "${project.name}"
        ]
        withXmlClosure = {
            information {
                title project.name
                vendor project.group ?: project.name
            }
        }
        desc = {
            'application-desc'('main-class': "${project.jnlp.mainClassName}")
        }
    }

    String getMainClassName() {
        mainClassName ?: (project.plugins.hasPlugin('application') ? project.mainClassName : null)
    }

    Map<String, String> getJ2seParams() {
        j2seParams ?: [version: getJavaVersion()]
    }

    void href(String href) {
        jnlpParams.href = href
    }

    void codebase(String codebase) {
        jnlpParams.codebase = codebase
    }

    void spec(String spec) {
        jnlpParams.spec = spec
    }

    void withXml(Closure closure) {
        withXmlClosure = closure
    }

    private String getJavaVersion() {
        project.plugins.hasPlugin('java-base') ? project.targetCompatibility : JavaVersion.current()
    }
}
