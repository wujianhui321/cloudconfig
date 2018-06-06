package com.wjh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableEurekaClient
 public class HelloServiceApplication {





    public static void main(String[] args) {
        SpringApplication.run(HelloServiceApplication.class,args);
    }

}
