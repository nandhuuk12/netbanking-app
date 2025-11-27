package com.netbanking.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new account
 */
public class CreateAccountRequest {

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "SAVINGS|CURRENT|SALARY|FIXED_DEPOSIT", message = "Invalid account type")
    private String accountType;

    @NotBlank(message = "Branch IFSC is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    private String branchIfsc;

    @PositiveOrZero(message = "Initial deposit must be zero or positive")
    private BigDecimal initialDeposit;

    // Constructors
    public CreateAccountRequest() {}

    public CreateAccountRequest(String accountType, String branchIfsc, BigDecimal initialDeposit) {
        this.accountType = accountType;
        this.branchIfsc = branchIfsc;
        this.initialDeposit = initialDeposit;
    }

    // Getters and Setters
    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getBranchIfsc() {
        return branchIfsc;
    }

    public void setBranchIfsc(String branchIfsc) {
        this.branchIfsc = branchIfsc;
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }
}
