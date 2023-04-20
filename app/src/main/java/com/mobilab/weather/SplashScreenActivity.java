package com.mobilab.weather;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.Objects;

public class SplashScreenActivity extends AppCompatActivity {

    ImageView mobilab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mobilab = findViewById(R.id.mobilabLogo);

        YoYo.with(Techniques.Shake).duration(700).playOn(mobilab);

        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep(700);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashScreenActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            }
        }; thread.start();

    }
}