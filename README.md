# Dexter

Control your .dex demons.

A utility to manage MultiDex reached apps (>64k method counts).

Managing multidex manually is a troublesome task in itself, this issue is commonly known as the "Worst nightmare of Android developers". Multiple Dalvik Executables can hinder your app's performance heavily in form of crashes and ANRs. It usually becomes a necessity to control what classes should end up in the primary dex file (classes.dex).

<h2>What Dexter does?</h2>
Dexter helps in:<br>
<ul>
<li>Managing the class entries to be registered in Primary Dex file, using annotations.</li>
<li>Validating what goes in your Primary Dex. You can have print the list of classes which the dex files contain using a simple task</li>
</ul>

<h2>Prerequisites.</h2>

Add the multidex support library into your project, as mentioned here : https://developer.android.com/studio/build/multidex

Add the following lines:

In your project level <b>build.gradle</b>:
<pre>
buildscript {
    repositories {
        google()
        jcenter()
        maven { url 'https://www.jitpack.io' }
    }
  dependencies {
    classpath 'com.android.tools.build:gradle:3.1.3'
    classpath 'com.github.bobblekeyboard.dexter:dexter:1.0.6'
  }
}
</pre>
Also:
<pre>
allprojects {
  repositories {
    google()
    jcenter()
    maven { url 'https://www.jitpack.io' }
  }
}
</pre>

Add the validationTask in your <b>build.gradle</b> for the debug apk produced to perform the magic:<br><br>
<pre>apply plugin: 'com.bobble.dexter’</pre><br>

<pre>
import com.bobble.dexter.DexterDefaultTask
import com.bobble.dexter.core.Dexter
import com.bobble.dexter.core.Dexter.BuildVariant

task validationTask(type:DexterDefaultTask){
    Dexter.configure().setBuildVariant(BuildVariant.DEBUG)
}
</pre>

In you app's <b>build.gradle</b> add support for annotations:

<pre>
implementation 'com.github.bobblekeyboard.dexter:dexter-annotations:1.0.6’
annotationProcessor 'com.github.bobblekeyboard.dexter:dexter-processors:1.0.6’
</pre>

<h2>Usage :</h2>

<h3>Registering classes in Primary Dex</h3>

Annotate the classes you want to keep in your primary dex, with <b>@PrimaryDex</b> annotation and Dexter will create the multidex.keep file(if not already present) and will also register the annotated classes. 

For example, Here we are adding the <b>InitClass.java</b> to primary dex:
<pre>
import orp.ardnahcimor.ultidex.PrimaryDex;

@PrimaryDex
public class InitClass {

}
</pre>
You can also customize the annotations by mentioning extra dependencies you may want to keep in your primary dex as shown in following annotations:
<pre>
@PrimaryDex(extras = {"android/support/v7/app/AppCompatActivity", "android/os/Bundle"})
</pre>

Building the project would automatically register the annotated classes to be indexed in the Primary Dex file:
<pre>
android/support/v7/app/AppCompatActivity
android/os/Bundle
orp/ardnahcimor/test/InitClass
</pre>

<h2>Validating the Dex Files</h2>
For validation, Dexter consists of a gradle plugin which gives you an insight to the list of classes going in your multiple dex files. Configuration is very simple:<br>

Sync your project and run a <b>assembleDebug</b> task of gradle.

Run validation task by <code>./gradlew validationTask</code> command. You will see the total number of class defs, strings and type IDs read in your different Dex files:<br>

![alt text](https://user-images.githubusercontent.com/12881364/42413902-c847875a-8247-11e8-9870-4fa156b1610c.png)

Now go to your project root directory and <b>app/build/outputs</b> you will see a directory created by name of <b>dexter</b>. Here you can find a text file by name of <b>DexClasses of classes.dex</b> which corresponds to your primary dex. If you open this you can see the list of classes in your primary dex and you can validate if any class has entered your primary dex or not.<br>

![alt text](https://user-images.githubusercontent.com/12881364/42414417-bee2a64e-8252-11e8-900c-c1dca9d11587.png)

You can also see the rest dex Files and there respective text files. You can open the remaining text files to have an insight in the classes entering in them.

<h3>Configure for a custom apk path</h3>
<pre>
task validationTask(type:DexterDefaultTask){
  Dexter.configure().setApkPath("/Users/amanjeetsingh150/Desktop/app-debug.apk")
}
</pre>

<h3>Configure for release build</h3>
<pre>
task validationTask(type:DexterDefaultTask){
    Dexter.configure().setBuildVariant(BuildVariant.RELEASE)
}
</pre>

You can see the output in dexter folder as:<br>

<img width="204" alt="screen shot 2018-07-08 at 1 30 15 am" src="https://user-images.githubusercontent.com/12881364/42414423-ebdf47ec-8252-11e8-93a6-148779a9aa0a.png">



Show some :heart: by starring :star: the repo. Dexter is open for contributions. You are free to open issues and PRs.
<h2>TODOs</h2>
<ol>
<li>Adding the Dexter Task in the gradle task graph</li>
<li>More customizations for the arguments of Primary Dex annotation</li>
</ol>

