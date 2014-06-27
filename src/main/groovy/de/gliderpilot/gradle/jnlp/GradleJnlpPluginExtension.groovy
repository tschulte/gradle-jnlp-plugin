package de.gliderpilot.gradle.jnlp

import javax.inject.Inject

class GradleJnlpPluginExtension {

    private GradleJnlpPlugin plugin

    @Inject
    GradleJnlpPluginExtension(GradleJnlpPlugin plugin) {
        this.plugin = plugin
    }

    void sign(Closure closure) {

    }
}
