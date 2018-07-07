package com.bobble.dexter;

import com.bobble.dexter.core.Dexter;
import org.gradle.internal.impldep.org.junit.Rule;
import org.gradle.internal.impldep.org.junit.Test;
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder;
import java.io.BufferedWriter;
import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import java.io.FileWriter;
import java.io.IOException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static com.bobble.dexter.DexterPlugin.TASK_NAME;


public class DexterPluginTest {

    @Rule
    private final TemporaryFolder testProjectDir = new TemporaryFolder();

    @Test
    public void testForDebugAPK() throws Exception{
        setupConfiguration();
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath()
                .withArguments(TASK_NAME, "--stacktrace")
                .build();

        assertThat(result.task(":" + TASK_NAME).getOutcome(), equalTo(SUCCESS));
    }

    private void setupConfiguration() throws IOException {
        createBuildFile();
        Dexter.configure().setBuildVariant(Dexter.BuildVariant.DEBUG);
    }

    private void createBuildFile() throws IOException {
        File buildFile = testProjectDir.newFile("build.gradle");
        writeFile(buildFile, "plugins { id 'com.bobble.dexter' }");
    }

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }


}
