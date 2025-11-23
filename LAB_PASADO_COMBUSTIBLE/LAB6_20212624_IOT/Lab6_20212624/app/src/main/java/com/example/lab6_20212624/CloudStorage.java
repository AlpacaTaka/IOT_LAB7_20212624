package com.example.lab6_20212624;

import android.app.Activity;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class CloudStorage {

    private final FirebaseStorage storage;

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(String errorMessage);
    }

    public CloudStorage() {
        storage = FirebaseStorage.getInstance();
    }


    public StorageReference getReference() {
        return storage.getReference();
    }

    public void uploadImage(@NonNull Activity activity, @NonNull Uri localFileUri, @NonNull String remotePath, @NonNull UploadCallback callback) {
        try {
            StorageReference ref = storage.getReference().child(remotePath);

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

            UploadTask uploadTask = ref.putFile(localFileUri, metadata);
            uploadTask.addOnSuccessListener(activity, taskSnapshot -> {
                // Obtener URL de descarga
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    callback.onSuccess(uri.toString());
                }).addOnFailureListener(e -> callback.onFailure(e.getMessage() != null ? e.getMessage() : "Error getting download URL"));
            }).addOnFailureListener(activity, e -> {
                callback.onFailure(e.getMessage() != null ? e.getMessage() : "Upload failed");
            });
        } catch (Exception e) {
            callback.onFailure(e.getMessage() != null ? e.getMessage() : "Upload exception");
        }
    }


    public void getDownloadUrl(@NonNull String remotePath, @NonNull OnSuccessListener<Uri> successListener, @NonNull OnFailureListener failureListener) {
        StorageReference ref = storage.getReference().child(remotePath);
        ref.getDownloadUrl().addOnSuccessListener(successListener).addOnFailureListener(failureListener);
    }
}

