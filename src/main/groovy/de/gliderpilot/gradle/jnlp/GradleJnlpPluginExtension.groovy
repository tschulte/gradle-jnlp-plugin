package de.gliderpilot.gradle.jnlp

import groovy.xml.*

import javax.inject.Inject

import org.gradle.api.Project

class GradleJnlpPluginExtension {

    private GradleJnlpPlugin plugin

    boolean useVersions

    Map<String, String> jnlpParams = [spec: '7.0', href: 'launch.jnlp']

    Closure withXmlClosure

    @Inject
    GradleJnlpPluginExtension(GradleJnlpPlugin plugin, Project project) {
        this.plugin = plugin
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
