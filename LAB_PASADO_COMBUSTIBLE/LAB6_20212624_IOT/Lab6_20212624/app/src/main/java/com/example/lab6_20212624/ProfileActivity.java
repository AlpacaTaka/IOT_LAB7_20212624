package com.example.lab6_20212624;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final int RC_PICK_IMAGE = 2001;

    private ImageView ivProfile;
    private TextView tvName, tvEmail;
    private TextView tvDNI;
    private MaterialButton btnChoose, btnUpload;

    private Uri selectedImageUri;
    private CloudStorage cloudStorage;
    private AuthService authService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfile = findViewById(R.id.ivProfile);
        tvName = findViewById(R.id.tvName);
        tvDNI = findViewById(R.id.tvDNI);
        tvEmail = findViewById(R.id.tvEmail);
        btnChoose = findViewById(R.id.btnChooseImage);
        btnUpload = findViewById(R.id.btnUploadImage);

        cloudStorage = new CloudStorage();
        authService = new AuthService();

        FirebaseUser user = authService.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            // cargar nombre desde Firestore
            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            String fullName = doc.getString("fullName");
                            String photoUrl = doc.getString("photoUrl");
                            String dni = doc.getString("dni");
                            if (fullName != null) tvName.setText(fullName);
                            if (dni != null) tvDNI.setText(dni);
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Picasso.get().load(photoUrl).into(ivProfile);
                            }
                        }
                    });
        }

        btnChoose.setOnClickListener(v -> pickImage());
        btnUpload.setOnClickListener(v -> uploadImage());
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Seleccionar imagen"), RC_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivProfile.setImageURI(selectedImageUri);
        }
    }

    private void uploadImage() {
        FirebaseUser user = authService.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String remotePath = "profiles/" + user.getUid() + ".jpg";
        btnUpload.setEnabled(false);
        cloudStorage.uploadImage(this, selectedImageUri, remotePath, new CloudStorage.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                // guardar URL en Firestore
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                        .update("photoUrl", downloadUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, getString(R.string.upload_success, downloadUrl), Toast.LENGTH_LONG).show();
                            btnUpload.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ProfileActivity.this, e.getMessage() != null ? e.getMessage() : "Error saving photo url", Toast.LENGTH_LONG).show();
                            btnUpload.setEnabled(true);
                        });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                btnUpload.setEnabled(true);
            }
        });
    }
}
