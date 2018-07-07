package com.bobble.dexter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DexterPlugin implements Plugin<Project> {

    public static final String TASK_NAME = "MultiDexterTask";

    @Override
    public void apply(Project project) {
        project.task("dexterTask");
        project.getTasks().create(TASK_NAME, DexterDefaultTask.class);
    }

}
