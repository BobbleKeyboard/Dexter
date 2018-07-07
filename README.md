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

