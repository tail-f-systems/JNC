package com.tailf.jnc.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin


class JncPyangPlugin implements Plugin<Project> {
    //@Override
    /**
     * Gradle plugin entry point.
     * Registers plugin `jncPyang` of `JncPyangTask` type.
     *
     * @param target
     * @see JncPyangTask
     */
    void apply(Project target) {
        target.task('jncPyang', type: JncPyangTask)
    }
}
