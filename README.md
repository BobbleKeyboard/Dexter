# Dexter

A utility for managing your MultiDex enabled projects. Ever got <b>java.lang.NoClassDefFoundError</b> while using MultiDex features. This happens due to the reason that the classes needed for starting your app not ends up in your Primary Dex. The ones your app uses on startup time needs to be in the first one. If it’s not – you get that error. 
<h3>What is the solution?</h3>
So for having your classes you need for starting up you create a <b>multidex.keep</b> file in your project’s root directory. After creating this file, you add the class names as shown following and the your app finally launches well.
<pre>
orp/ardnahcimor/ultidex_sample/TestNoClass
orp/ardnahcimor/ultidex_sample/MyClass
</pre>

<h3>What Dexter do?</h3>
Dexter helps in following to major things:<br>
<ul>
<li>Manage the multidex.keep file</li>
<li>To validate what goes in your Primary Dex. You can have look to list of whole classes which the dex contains.</li>
</ul>

<h3>Validating the Dex Files</h3>
For validation, Dexter consist of a gradle plugin which gives you an insight to the list of classes going in your multiple dex files. Configuration is very simple:<br>
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
Now for applying plugin:
<pre>apply plugin: 'com.bobble.dexter’</pre><br>
Sync your project and run a <b>assembleDebug</b> task of gradle.
Now write the following task in your <b>build.grade</b> for the debug apk produced to see the magic:<br>
<pre>
import com.bobble.dexter.DexterDefaultTask
import com.bobble.dexter.core.Dexter
import com.bobble.dexter.core.Dexter.BuildVariant


task validationTask(type:DexterDefaultTask){
    Dexter.configure().setBuildVariant(BuildVariant.DEBUG)
}
</pre>

Run this task by <code>./gradlew validationTask</code> command. You will see the total number of class defs, strings and type IDs read in your different Dex files:<br>

![alt text](https://user-images.githubusercontent.com/12881364/42413902-c847875a-8247-11e8-9870-4fa156b1610c.png)

Now go to your project root directory and <b>app/build/outputs</b> you will see a directory created by name of <b>dexter</b>. Here you can find a text file by name of <b>DexClasses of classes.dex</b> which corresponds to your primary dex. If you open this you can see the list of classes in your primary dex where you can validate if any class has entered your primary dex or not.<br>

![alt text](https://user-images.githubusercontent.com/12881364/42414081-6598f770-824b-11e8-906e-727d94387c3c.png)

You can also see the rest dex Files and there respective text files. You can open the remaining text files to have an insight in the classes entering in them.

<h2>Configuring for a custom path</h2>
<pre>
task validationTask(type:DexterDefaultTask){
  Dexter.configure().setApkPath("/Users/amanjeetsingh150/Desktop/app-debug.apk")
}
</pre>

You can see the output in dexter folder as:
![alt text](https://user-images.githubusercontent.com/12881364/42414254-0686e0cc-824f-11e8-8edc-193e6b475ae8.png)








