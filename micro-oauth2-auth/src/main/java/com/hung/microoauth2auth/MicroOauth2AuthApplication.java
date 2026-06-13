package com.hung.microoauth2auth;

import com.hung.microoauth2auth.log.annotation.EnableLogRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Hung
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableLogRecord(tenantId = "com.hung.auth")
public class MicroOauth2AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroOauth2AuthApplication.class, args);
    }

}
