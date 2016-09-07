package de.gliderpilot.gradle.jnlp

import org.gradle.api.Project
import org.gradle.process.ExecResult

/**
 * Created by tobias on 9/7/16.
 */
class JavaHomeAware {

    static ExecResult exec(Project project, String command, Object... arguments) {
        String javaHome = System.getenv("JAVA_HOME")
        if (!javaHome)
            javaHome = System.getProperty("java.home")?.replaceAll(/[\\\/]jre$/, '')


        if (javaHome)
            command = "$javaHome/bin/$command"

        project.exec {
            commandLine command
            args arguments
        }
    }
}
