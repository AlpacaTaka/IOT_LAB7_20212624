package com.example.clienteurekafeign_lab7;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RegistroController {

    @Autowired
    private ServiceValidationClient serviceValidationClient;

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrarUsuario(@RequestBody RegistroRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Validar que se recibieron ambos campos
        if (request.getDni() == null || request.getCorreo() == null) {
            response.put("error", "DNI y correo son requeridos");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Validar DNI usando Feign Client
            String dniValidationResult = serviceValidationClient.validarDni(request.getDni());
            boolean dniValido = parseValidationResult(dniValidationResult);

            // Validar correo usando Feign Client
            String correoValidationResult = serviceValidationClient.validarCorreo(request.getCorreo());
            boolean correoValido = parseValidationResult(correoValidationResult);

            if (dniValido && correoValido) {
                response.put("message", "Registro exitoso");
                response.put("dni", request.getDni());
                response.put("correo", request.getCorreo());
                return ResponseEntity.ok(response);
            } else {
                StringBuilder errorMessage = new StringBuilder("Errores de validación: ");

                if (!dniValido) {
                    errorMessage.append("El DNI no tiene un formato válido. ");
                }
                if (!correoValido) {
                    errorMessage.append("El correo debe ser del dominio @pucp.edu.pe.");
                }

                response.put("error", errorMessage.toString().trim());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("error", "Error al conectar con el servicio de validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private boolean parseValidationResult(String validationResult) {
        try {
            // Simple parsing - en una implementación real podrías usar ObjectMapper
            return validationResult.contains("\"valid\":true");
        } catch (Exception e) {
            return false;
        }
    }

    public static class RegistroRequest {
        private String dni;
        private String correo;

        // Getters y Setters
        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public String getCorreo() {
            return correo;
        }

        public void setCorreo(String correo) {
            this.correo = correo;
        }
    }
}