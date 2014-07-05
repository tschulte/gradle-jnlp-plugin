package de.gliderpilot.gradle.jnlp

import groovy.xml.*

import javax.inject.Inject

import org.gradle.api.Project

class GradleJnlpPluginExtension {

    private GradleJnlpPlugin plugin
    private Project project

    // see http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/avoidingUnnecessaryUpdateChecks.html
    boolean useVersions

    // not needed, if application-plugin is applied
    String mainClassName

    // TODO: automatically pack jars using pack200
    // boolean usePack200

    Map<String, String> jnlpParams = [spec: '7.0', href: 'launch.jnlp']

    Closure withXmlClosure

    @Inject
    GradleJnlpPluginExtension(GradleJnlpPlugin plugin, Project project) {
        this.plugin = plugin
        this.project = project
        if (project.version != 'unspecified') {
            jnlpParams.version = project.version
        }
        withXmlClosure = {
            information {
                title project.name
                vendor project.group ?: project.name
            }
        }
    }

    String getMainClassName() {
        mainClassName ?: (project.plugins.hasPlugin('application') ? project.mainClassName : null)
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
}
