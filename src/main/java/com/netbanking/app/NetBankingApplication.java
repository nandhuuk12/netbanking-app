package com.netbanking.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for NetBanking Application
 */
@SpringBootApplication(scanBasePackages = {"com.netbanking.app", "com.banking.core"})
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.netbanking.app.repository", "com.banking.core.repository"})
@EntityScan(basePackages = {"com.netbanking.app.entity", "com.banking.core.entity"})
public class NetBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetBankingApplication.class, args);
    }
}
