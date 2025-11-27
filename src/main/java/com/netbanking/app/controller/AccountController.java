package com.netbanking.app.controller;

import com.banking.core.entity.Account;
import com.banking.core.entity.User;
import com.banking.core.enums.AccountType;
import com.banking.core.service.AccountService;
import com.banking.core.service.UserService;
import com.netbanking.app.dto.AccountDto;
import com.netbanking.app.dto.CreateAccountRequest;
import com.netbanking.app.dto.DepositRequest;
import com.netbanking.app.dto.TransferRequest;
import com.netbanking.app.dto.WithdrawalRequest;
import com.netbanking.app.security.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Account management controller
 */
@RestController
@RequestMapping("/accounts")
@Tag(name = "Account Management", description = "Account operations and management")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get user accounts", description = "Retrieve all accounts for the authenticated user")
    public ResponseEntity<List<AccountDto>> getUserAccounts(Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        User user = getCurrentUser(authentication);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Account> accounts = accountService.findByUser(user, pageable);
        
        List<AccountDto> accountDtos = accounts.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(accountDtos);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details", description = "Retrieve details of a specific account")
    public ResponseEntity<AccountDto> getAccountDetails(
            @Parameter(description = "Account number") @PathVariable String accountNumber,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        // Check if account belongs to user or user is admin
        if (!account.getUser().getId().equals(user.getId()) && !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(convertToDto(account));
    }

    @PostMapping
    @Operation(summary = "Create new account", description = "Create a new bank account for the user")
    public ResponseEntity<AccountDto> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        Account account = accountService.createAccount(
                user, 
                AccountType.valueOf(request.getAccountType()),
                request.getBranchIfsc(),
                request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO
        );
        
        return ResponseEntity.ok(convertToDto(account));
    }

    @PostMapping("/{accountNumber}/deposit")
    @Operation(summary = "Deposit money", description = "Deposit money to an account")
    public ResponseEntity<AccountDto> deposit(
            @Parameter(description = "Account number") @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        // Verify account ownership
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getUser().getId().equals(user.getId()) && !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Account updatedAccount = accountService.deposit(
                accountNumber,
                request.getAmount(),
                request.getNarration()
        );
        
        return ResponseEntity.ok(convertToDto(updatedAccount));
    }

    @PostMapping("/{accountNumber}/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraw money from an account")
    public ResponseEntity<AccountDto> withdraw(
            @Parameter(description = "Account number") @PathVariable String accountNumber,
            @Valid @RequestBody WithdrawalRequest request,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        // Verify account ownership
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getUser().getId().equals(user.getId()) && !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Account updatedAccount = accountService.withdraw(
                accountNumber,
                request.getAmount(),
                request.getNarration()
        );
        
        return ResponseEntity.ok(convertToDto(updatedAccount));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer money between accounts")
    public ResponseEntity<String> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        // Verify source account ownership
        Account fromAccount = accountService.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new RuntimeException("Source account not found"));
        
        if (!fromAccount.getUser().getId().equals(user.getId()) && !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        accountService.transfer(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount(),
                request.getNarration()
        );
        
        return ResponseEntity.ok("Transfer successful");
    }

    @GetMapping("/{accountNumber}/balance")
    @Operation(summary = "Get account balance", description = "Get current balance of an account")
    public ResponseEntity<BigDecimal> getBalance(
            @Parameter(description = "Account number") @PathVariable String accountNumber,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        // Verify account ownership
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getUser().getId().equals(user.getId()) && !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/summary")
    @Operation(summary = "Get account summary", description = "Get summary of all user accounts")
    public ResponseEntity<Object> getAccountSummary(Authentication authentication) {
        User user = getCurrentUser(authentication);
        Object summary = accountService.getAccountSummary(user);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/{accountNumber}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block account", description = "Block an account (Admin only)")
    public ResponseEntity<String> blockAccount(
            @Parameter(description = "Account number") @PathVariable String accountNumber,
            @RequestParam String reason) {
        
        accountService.blockAccount(accountNumber, reason);
        return ResponseEntity.ok("Account blocked successfully");
    }

    @PutMapping("/{accountNumber}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unblock account", description = "Unblock an account (Admin only)")
    public ResponseEntity<String> unblockAccount(
            @Parameter(description = "Account number") @PathVariable String accountNumber) {
        
        accountService.unblockAccount(accountNumber);
        return ResponseEntity.ok("Account unblocked successfully");
    }

    @PutMapping("/{accountNumber}/close")
    @Operation(summary = "Close account", description = "Close an account")
    public ResponseEntity<String> closeAccount(
            @Parameter(description = "Account number") @PathVariable String accountNumber,
            @RequestParam String reason,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        // Verify account ownership (or admin)
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!account.getUser().getId().equals(user.getId()) && !isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        accountService.closeAccount(accountNumber, reason);
        return ResponseEntity.ok("Account closed successfully");
    }

    // Helper methods
    private User getCurrentUser(Authentication authentication) {
        UserDetailsServiceImpl.UserPrincipal userPrincipal = 
                (UserDetailsServiceImpl.UserPrincipal) authentication.getPrincipal();
        
        return userService.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    private AccountDto convertToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountType(account.getAccountType().name());
        dto.setBalance(account.getBalance());
        dto.setCurrency(account.getCurrency());
        dto.setAvailableBalance(account.getAvailableBalance());
        dto.setOverdraftLimit(account.getOverdraftLimit());
        dto.setInterestRate(account.getInterestRate());
        dto.setStatus(account.getStatus().name());
        dto.setBranchName(account.getBranch().getName());
        dto.setBranchIfsc(account.getBranch().getIfsc());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());
        return dto;
    }
}
