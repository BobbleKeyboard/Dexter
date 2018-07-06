package com.bobble.dexter;

import com.android.annotations.NonNull;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.platform.base.Variant;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexterPlugin implements Plugin<Project> {

    private Project appProject;

    @Override
    public void apply(Project project) {
        project.task("javaTask");
        project.getTasks().create("MultiDexterTask", DexterDefaultTask.class);
    }

}
