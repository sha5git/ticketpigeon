package com.sha5.ticketpigeon.theater;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.sha5.ticketpigeon"})
public class TheaterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TheaterServiceApplication.class, args);
    }
}
