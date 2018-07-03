package com.bobble.dexter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DexterPlugin implements Plugin<Project>{

    @Override
    public void apply(Project project) {
        project.task("javaTask");
        project.getTasks().create("MultiDexterTask", DexterDefaultTask.class);

    }
}
