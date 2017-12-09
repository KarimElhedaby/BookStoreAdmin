package com.hamza.alif.bookstore.Ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.hamza.alif.bookstore.R;
import com.hamza.alif.bookstore.Util.ActivityLancher;
import com.hamza.alif.bookstore.Util.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.emailET)
    EditText emailET;
    @BindView(R.id.passwordET)
    EditText passwordET;
    @BindView(R.id.loginB)
    Button LoginB;
    @BindView(R.id.registerB)
    Button registerB;
    @BindView(R.id.forget_passwordTV)
    TextView forgetpasswTV;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    ActivityLancher.openBooksActivity(LoginActivity.this);
                    finish();
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @OnClick(R.id.registerB)
    void openregisterActvity() {
        ActivityLancher.openRegisterActivity(LoginActivity.this);

    }

    @OnClick(R.id.loginB)
    void login() {
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        if (email.isEmpty()) {
            emailET.setError(getString(R.string.enter_email));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError(getString(R.string.email_not_formatted));
        } else if (password.isEmpty()) {
            passwordET.setError(getString(R.string.enter_password));
        } else if (password.length() < 6) {
            passwordET.setError(getString(R.string.password_length_error));
        } else {
            Utilities.showLoadingDialog(this, Color.GRAY);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new
                                                                                            OnCompleteListener<AuthResult>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<AuthResult> task) {
                    Utilities.dismissLoadingDialog();
                    if (!task.isSuccessful()) {
                        if (task.getException() instanceof FirebaseAuthInvalidUserException
                                || task.getException()
                                instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Wrong Email or Password",
                                    Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(LoginActivity.this, R.string.error_in_connection,
                                    Toast.LENGTH_SHORT).show();
                    }


                    }
                                                                                            });
        }
    }

     @OnClick(R.id.forget_passwordTV)
    void sendForgetPasswordEmail() {
        // TODO: VERIFY MAIL IN GMAIL USING FIREBASE
        String email = emailET.getText().toString();
        if (email.isEmpty()) {
            emailET.setError(getString(R.string.enter_email));
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError(getString(R.string.email_not_formatted));
        } else{
                Utilities.showLoadingDialog(this, Color.WHITE);
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Utilities.dismissLoadingDialog();
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.check_your_mail,
                                    Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(LoginActivity.this, R.string.user_not_found,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.error_in_connection,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }



    }

}
