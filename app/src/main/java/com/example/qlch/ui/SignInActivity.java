package com.example.qlch.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.qlch.MainActivity;
import com.example.qlch.R;
import com.example.qlch.databinding.ActivitySignInBinding;
import com.example.qlch.model.Token;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;


public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding = null;
    GoogleSignInClient googleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(getColor(R.color.brown_120));

        setBindingAnimation();
        binding.tvForgotPass.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });

        binding.btnLogin.setOnClickListener(v -> {
            if (validate()) {
                onClickSignIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(SignInActivity.this, gso);
        binding.cavGoogle.setOnClickListener(btn -> {
            Intent intent = googleSignInClient.getSignInIntent();
            someActivityResultLauncher.launch(intent);

        });

    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e("zzzz", "ResultCode " + result.getResultCode());
                    Log.e("zzzz", "ResultOk " + Activity.RESULT_OK);
                    Log.e("zzzz", "Data " + result.getData());
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                            fireBaseAuthWithGoogle(account);
                        } catch (Exception e) {
                            Toast.makeText(SignInActivity.this, "Lỗi!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


    private void onClickSignIn() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String strEmail = binding.email.getText().toString().trim();
        String strPass = binding.password.getText().toString().trim();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutLogin.setAlpha(0.2f);

        mAuth.signInWithEmailAndPassword(strEmail, strPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            getToken();
                            Toast.makeText(SignInActivity.this, getString(R.string.notifi_login_success), Toast.LENGTH_SHORT).show();
                            binding.progressBar.setVisibility(View.VISIBLE);
                            finishAffinity();
                        } else {
                            Toast.makeText(SignInActivity.this, getString(R.string.notifi_login_fail), Toast.LENGTH_SHORT).show();
                            binding.progressBar.setVisibility(View.GONE);
                            binding.layoutLogin.setAlpha(1f);
                        }
                    }
                });

    }


    private void fireBaseAuthWithGoogle(GoogleSignInAccount account) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(SignInActivity.this, "Thành công!", Toast.LENGTH_SHORT).show();
                //Tạo code ngầm đăng đăng kí tài khoản
                getToken();
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignInActivity.this, "Thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getToken() {
        String firebaseAuth = FirebaseAuth.getInstance().getUid();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                //If task is failed then
                if (!task.isSuccessful()) {
                    Log.d("TAG", "onComplete: Failed to get the Token");
                }
                //Token
                String token = task.getResult();
                Log.d("TAG", "onComplete: " + token);
                Token token1 = new Token(token);
                Log.d("TAG", "onComplete: "+firebaseAuth);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child("Tokens")
                        .child(firebaseAuth)
                        .setValue(token1).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });


            }
        });
    }

    private boolean validate() {
        String strEmail = binding.email.getText().toString().trim();
        String strPass = binding.password.getText().toString().trim();
        if (TextUtils.isEmpty(strEmail)) {
            binding.email.setError(getString(R.string.error_email_1), null);
            binding.email.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
            binding.email.setError(getString(R.string.error_email_2), null);
            binding.email.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(strPass)) {
            binding.password.setError(getString(R.string.error_pass_1), null);
            binding.password.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public void setBindingAnimation() {
        viewAnimation(binding.icLogo, "translationY", -400f, 0f);
        viewAnimation(binding.tvIcon, "translationY", -400f, 0f);
        viewAnimation(binding.email, "translationX", -300f, 0f);
        viewAnimation(binding.tilPassword, "translationX", 300f, 0f);
        viewAnimation(binding.tvForgotPass, "translationY", -400f, 0f);
        viewAnimation(binding.cavButton, "translationY", 400f, 0f);
        viewAnimation(binding.tvContent, "translationX", -200f, 0f);
        viewAnimation(binding.tvSignUp, "translationX", 200f, 0f);
        viewAnimation(binding.line1, "translationX", -200f, 0f);
        viewAnimation(binding.line2, "translationX", 200f, 0f);
        viewAnimation(binding.tvContent2, "translationY", 300f, 0f);
        viewAnimation(binding.cavGoogle, "translationY", 300f, 0f);

    }


    public void viewAnimation(View view, String ani, float... values) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, ani, values);
        animator.setDuration(1800);
        animator.start();
    }


}