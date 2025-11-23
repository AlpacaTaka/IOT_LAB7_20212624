package com.example.clienteurekafeign_lab7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ClientEurekaFeignLab7Application {

    public static void main(String[] args) {
        SpringApplication.run(ClientEurekaFeignLab7Application.class, args);
    }

}
