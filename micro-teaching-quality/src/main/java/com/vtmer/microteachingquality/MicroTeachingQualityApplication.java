package com.vtmer.microteachingquality;

import com.vtmer.microteachingquality.log.annotation.EnableLogRecord;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Hung
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("mapper")
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableLogRecord(tenantId = "com.hung.test")
public class MicroTeachingQualityApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroTeachingQualityApplication.class, args);
    }

}
