package com.netbanking.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for withdrawal operations
 */
public class WithdrawalRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 255, message = "Narration must be less than 255 characters")
    private String narration;

    // Constructors
    public WithdrawalRequest() {}

    public WithdrawalRequest(BigDecimal amount, String narration) {
        this.amount = amount;
        this.narration = narration;
    }

    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }
}
