package orp.ardnahcimor.ultidex_sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import orp.ardnahcimor.ultidex.PrimaryDex;

@PrimaryDex(extras = {"android/support/v7/app/AppCompatActivity", "android/os/Bundle"})
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
