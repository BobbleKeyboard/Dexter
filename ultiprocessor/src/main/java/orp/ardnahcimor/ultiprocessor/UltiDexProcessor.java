package orp.ardnahcimor.ultiprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import orp.ardnahcimor.ultidex.PrimaryDex;

public class UltiDexProcessor extends AbstractProcessor {

    private static final String FILE_NAME = "multidex.keep";
    private static boolean mInit = true;
    Messager mMessager;
    Filer mFiler;

    public UltiDexProcessor() {
        super();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(
                PrimaryDex.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotationMirror, ExecutableElement executableElement, String s) {
        return super.getCompletions(element, annotationMirror, executableElement, s);
    }

    @Override
    protected synchronized boolean isInitialized() {
        return super.isInitialized();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsToBind =
                roundEnvironment.getElementsAnnotatedWith(PrimaryDex.class);

        for (Element element : elementsToBind) {
            Element enclosing = element;
            if (enclosing.getKind() != ElementKind.PACKAGE) {
                enclosing = enclosing.getEnclosingElement();
            }
            PackageElement packageElement = (PackageElement) enclosing;

            String name = packageElement.getQualifiedName().toString() + "." + element.getSimpleName().toString();
            name = name.replaceAll("\\.", "/");
            mMessager.printMessage(Diagnostic.Kind.NOTE, "R_DEBUG : Class found " + name);
            writeToFile(name);
            PrimaryDex primaryDex = element.getAnnotation(PrimaryDex.class);
            if (primaryDex != null) {
                String[] extras = primaryDex.extras();
                if (extras.length > 0) {
                    for (int i = 0; i < extras.length; i++) {
                        mMessager.printMessage(Diagnostic.Kind.NOTE, "R_DEBUG : Extras found " + extras[i]);
                        writeToFile(extras[i]);
                    }
                }
            }
        }
        return false;
    }

    private void writeToFile(String text) {
        try {
            File file = new File(FILE_NAME);

            if (!file.exists()) {
                file.createNewFile();
            }

            if (mInit) {
                file.delete();
                file = new File(FILE_NAME);
                mInit = false;
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true));

            writer.append(text);
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
