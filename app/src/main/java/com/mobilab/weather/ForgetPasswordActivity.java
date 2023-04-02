package com.mobilab.weather;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgetPasswordActivity extends AppCompatActivity {

    Button resetPasswordBtn;
    EditText emailBox;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        //Objects.requireNonNull(getSupportActionBar()).hide();
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        emailBox = findViewById(R.id.emailBox);
        auth = FirebaseAuth.getInstance();

        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }
    private void resetPassword() {
        String email = emailBox.getText().toString().trim();

        if (email.isEmpty()){
            emailBox.setError("Email is required");
            emailBox.requestFocus();
            YoYo.with(Techniques.Shake).duration(1200).repeat(0).playOn(resetPasswordBtn);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailBox.setError("Please provide a valid email!");
            emailBox.requestFocus();
            YoYo.with(Techniques.Shake).duration(1200).repeat(0).playOn(resetPasswordBtn);
        } else {
            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        new AlertDialog.Builder(ForgetPasswordActivity.this)
                                .setIcon(R.drawable.baseline_email_24)
                                .setTitle("Check your Email")
                                .setMessage("A Link has been send to your email for resetting the password." +
                                        " Kindly reset it & revert back to Sign-In Screen.")
                                .setPositiveButton("Reset Now", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = getPackageManager().
                                                getLaunchIntentForPackage("com.google.android.gm");
                                        startActivity(intent);
                                        Toast.makeText(ForgetPasswordActivity.this,
                                                "Check in Spam Folder if not found", Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
                        Toast.makeText(ForgetPasswordActivity.this, "Check your email to reset your password", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ForgetPasswordActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        }
    }
}