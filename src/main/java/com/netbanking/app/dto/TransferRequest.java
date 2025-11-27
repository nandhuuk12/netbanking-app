package com.netbanking.app.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for money transfer operations
 */
public class TransferRequest {

    @NotBlank(message = "From account number is required")
    private String fromAccountNumber;

    @NotBlank(message = "To account number is required")
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 255, message = "Narration must be less than 255 characters")
    private String narration;

    // Constructors
    public TransferRequest() {}

    public TransferRequest(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String narration) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.narration = narration;
    }

    // Getters and Setters
    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

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
