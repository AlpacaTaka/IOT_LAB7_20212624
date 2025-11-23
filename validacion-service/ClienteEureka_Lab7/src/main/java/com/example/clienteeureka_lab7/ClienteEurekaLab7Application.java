package com.example.clienteeureka_lab7;

import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/validar")
public class ClienteEurekaLab7Application {

    @Autowired
    @Lazy
    private EurekaClient eurekaClient;

    @Value("${spring.application.name}")
    private String appName;

    public static void main(String[] args) {
        SpringApplication.run(ClienteEurekaLab7Application.class, args);
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Map<String, Object>> validarDni(@PathVariable String dni) {
        boolean valid = dni != null && dni.matches("\\d{8}");
        Map<String, Object> body = new HashMap<>();
        body.put("valid", valid);
        body.put("value", dni);
        body.put("type", "dni");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/correo/{correo}")
    public ResponseEntity<Map<String, Object>> validarCorreo(@PathVariable String correo) {
        boolean valid = correo != null && correo.toLowerCase().endsWith("@pucp.edu.pe");
        Map<String, Object> body = new HashMap<>();
        body.put("valid", valid);
        body.put("value", correo);
        body.put("type", "correo");
        return ResponseEntity.ok(body);
    }


}
