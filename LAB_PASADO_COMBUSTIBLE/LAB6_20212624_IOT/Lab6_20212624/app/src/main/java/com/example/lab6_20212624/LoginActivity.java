package com.example.lab6_20212624;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private AuthService authService;

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar AuthService
        authService = new AuthService();

        // Views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        View tvRegister = findViewById(R.id.tvRegister);
        if (tvRegister != null) tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        View tvForgot = findViewById(R.id.tvForgotPassword);
        if (tvForgot != null) tvForgot.setOnClickListener(v -> showForgotPasswordDialog());

        btnLogin.setOnClickListener(v -> doEmailPasswordLogin());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            // ya esta logeado
            goToMain();
        }
    }

    private void doEmailPasswordLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        authService.signInWithEmailAndPassword(email, password, this, (com.google.android.gms.tasks.Task<AuthResult> task) -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(LoginActivity.this, task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showForgotPasswordDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
        final TextInputEditText etDialogEmail = dialogView.findViewById(R.id.etDialogEmail);
        final ProgressBar pb = dialogView.findViewById(R.id.pbDialog);
        final TextView tvHelper = dialogView.findViewById(R.id.tvHelper);

        etDialogEmail.setHint(getString(R.string.email));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.forgot_password)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.send, null); // set later to prevent auto-dismiss

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String email = etDialogEmail.getText() != null ? etDialogEmail.getText().toString().trim() : "";
                if (TextUtils.isEmpty(email)) {
                    etDialogEmail.setError(getString(R.string.error_empty_fields));
                    return;
                }

                // show progress
                pb.setVisibility(View.VISIBLE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                tvHelper.setText("");

                authService.sendPasswordResetEmail(email, LoginActivity.this, task -> {

                    pb.setVisibility(View.GONE);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, R.string.reset_email_sent, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : getString(R.string.error_occurred);
                        tvHelper.setText(msg);
                    }
                });
            });
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // no hay Google Sign-In; mantener para compatibilidad
    }

    private void goToMain() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
