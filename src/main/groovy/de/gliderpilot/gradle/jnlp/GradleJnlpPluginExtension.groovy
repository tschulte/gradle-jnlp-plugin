package de.gliderpilot.gradle.jnlp

import org.gradle.api.JavaVersion
import groovy.xml.*

import javax.inject.Inject

import org.gradle.api.Project

class GradleJnlpPluginExtension {

    private GradleJnlpPlugin plugin
    private Project project

    // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/avoidingUnnecessaryUpdateChecks.html
    boolean useVersions

    // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/reducingDownloadTime.html
    boolean usePack200

    // not needed, if application-plugin is applied
    String mainClassName



    Map<String, String> jnlpParams = [spec: '7.0', href: 'launch.jnlp']
    Map<String, String> j2seParams

    Map<String, String> signJarParams = [:]
    Map<String, String> signJarAddedManifestEntries
    List<String> signJarRemovedManifestEntries = ['Trusted-Only', 'Trusted-Library']

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
