package com.example.servereureka_lab7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServerEurekaLab7Application {

    public static void main(String[] args) {
        SpringApplication.run(ServerEurekaLab7Application.class, args);
    }

}
