package com.mobilab.weather;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    EditText emailBox, passwordBox;
    Button signInBtn;
    TextView createAcTextView, forgotPassword, email, password;
    FirebaseAuth auth;
    ProgressDialog dialog;
    ImageView passwordEye, logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

      //  Objects.requireNonNull(getSupportActionBar()).hide();
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait...");

        auth = FirebaseAuth.getInstance();

        emailBox = findViewById(R.id.emailBox);
        passwordBox = findViewById(R.id.passwordBox);
        signInBtn = findViewById(R.id.SignInBtn);
        createAcTextView = findViewById(R.id.createAcTextView);
        forgotPassword = findViewById(R.id.forgotPasswordtv);
        passwordEye = findViewById(R.id.passwordEye);
        logo =findViewById(R.id.image_view);
        email = findViewById(R.id.txt_email);
        password = findViewById(R.id.txt_password);



        YoYo.with(Techniques.FlipInY).duration(1500).repeat(3).playOn(passwordEye);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(passwordBox);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(emailBox);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(signInBtn);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(createAcTextView);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(forgotPassword);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(logo);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(email);
        YoYo.with(Techniques.Landing).duration(2000).repeat(0).playOn(password);


        createAcTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                startActivity(new Intent(SignInActivity.this, ForgetPasswordActivity.class));
            }
        });


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);

                String email, password;
                email = emailBox.getText().toString().trim();
                password = passwordBox.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailBox.setError("Email cannot be empty");
                    emailBox.requestFocus();
                    YoYo.with(Techniques.Shake).duration(1200).repeat(0).playOn(signInBtn);
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailBox.setError("Please provide a valid email!");
                    emailBox.requestFocus();
                    YoYo.with(Techniques.Shake).duration(1200).repeat(0).playOn(signInBtn);
                } else if (TextUtils.isEmpty(password)) {
                    passwordBox.setError("Password cannot be empty!");
                    passwordBox.requestFocus();
                    YoYo.with(Techniques.Shake).duration(1200).repeat(0).playOn(signInBtn);
                } else if (password.length() < 6) {
                    passwordBox.setError("Min password length should be 6 characters!");
                    passwordBox.requestFocus();
                    YoYo.with(Techniques.Shake).duration(1200).repeat(0).playOn(signInBtn);
                } else {
                    dialog.show();
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                if (!firebaseUser.isEmailVerified()) {
                                    firebaseUser.sendEmailVerification();
                                    new android.app.AlertDialog.Builder(SignInActivity.this)
                                            .setIcon(R.drawable.baseline_email_24)
                                            .setTitle("Email Verification Needed")
                                            .setMessage("A link was send to your email previously. Please verify to log in.")
                                            .setPositiveButton("Verify Now", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = getPackageManager().
                                                            getLaunchIntentForPackage("com.google.android.gm");
                                                    startActivity(intent);
                                                    Toast.makeText(SignInActivity.this,
                                                            "Check in Spam Folder if not found", Toast.LENGTH_SHORT).show();
                                                }
                                            }).show();
                                } else {
                                    startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                    Toast.makeText(SignInActivity.this, "logged in!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(SignInActivity.this, Objects.requireNonNull(task.getException())
                                        .getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            reload();
        }
    }

    private void reload() {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null;
        if(user.isEmailVerified()) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
        }
    }

    public void ShowHidePass(View view) {
        if(passwordBox.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            ((ImageView)(view)).setImageResource(R.drawable.baseline_visibility_off_24);
            passwordBox.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            ((ImageView)(view)).setImageResource(R.drawable.baseline_visibility_24);
            passwordBox.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    private final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.baseline_exit_to_app_24)
                .setTitle("Exit App")
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                        finish();
                    }
                })
//                .setNeutralButton("", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

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