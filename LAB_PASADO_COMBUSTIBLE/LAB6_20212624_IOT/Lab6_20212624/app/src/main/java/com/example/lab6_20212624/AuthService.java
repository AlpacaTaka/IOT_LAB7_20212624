package com.example.lab6_20212624;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class AuthService {

    private final FirebaseAuth mAuth;

    public AuthService() {
        mAuth = FirebaseAuth.getInstance();
    }

    // Interfaz callback para registro
    public interface RegistrationCallback {
        void onSuccess();
        void onFailure(String message);
    }


    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }


    public void signInWithEmailAndPassword(@NonNull String email, @NonNull String password, @NonNull Activity activity, @NonNull OnCompleteListener<AuthResult> listener) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(activity, listener);
    }

    // Registro: primero validar microservicio, luego crear usuario en Firebase y guardar datos en Firestore
    public void registerUser(@NonNull String fullName, @NonNull String dni, @NonNull String email, @NonNull String password, @NonNull Activity activity, @NonNull RegistrationCallback callback) {
        // Ejecutar la llamada HTTP en background
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.168.18.34:8080/registro");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("dni", dni);
                payload.put("correo", email);

                byte[] out = payload.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(out);
                }

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
                StringBuilder respBuilder = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        respBuilder.append(line);
                    }
                }

                String responseBody = respBuilder.toString();

                if (responseCode >= 200 && responseCode < 300) {
                    // microservicio aprobó; ahora crear usuario en Firebase
                    activity.runOnUiThread(() -> {
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(activity, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("fullName", fullName);
                                    userMap.put("dni", dni);
                                    userMap.put("email", user.getEmail());
                                    userMap.put("createdAt", System.currentTimeMillis());

                                    db.collection("users").document(user.getUid()).set(userMap)
                                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                                            .addOnFailureListener(e -> callback.onFailure(e.getMessage() != null ? e.getMessage() : "Error saving user data"));
                                } else {
                                    callback.onFailure("User created but not available");
                                }
                            } else {
                                String err = task.getException() != null ? task.getException().getMessage() : "Error creating user";
                                callback.onFailure(err);
                            }
                        });
                    });
                } else {
                    // microservicio devolvió error (ej: DNI duplicado), devolver su mensaje
                    String errMsg = !responseBody.isEmpty() ? responseBody : ("Microservice error: HTTP " + responseCode);
                    activity.runOnUiThread(() -> callback.onFailure(errMsg));
                }

            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Network error";
                activity.runOnUiThread(() -> callback.onFailure(msg));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }


    public void sendPasswordResetEmail(@NonNull String email, @NonNull Activity activity, @NonNull OnCompleteListener<Void> listener) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(activity, listener);
    }

    public void signOut() {
        mAuth.signOut();
    }
}
