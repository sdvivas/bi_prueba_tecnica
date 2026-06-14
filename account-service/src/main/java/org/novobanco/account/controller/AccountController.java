package org.novobanco.account.controller;

import jakarta.validation.Valid;
import org.novobanco.account.dto.AccountResponse;
import org.novobanco.account.dto.BalanceResponse;
import org.novobanco.account.dto.CreateAccountRequest;
import org.novobanco.account.service.AccountService;
import org.novobanco.transaction.dto.TransactionResponse;
import org.novobanco.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable UUID id) {
        return accountService.getAccount(id);
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable UUID id) {
        return accountService.getBalance(id);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<TransactionResponse> transactions = transactionService.getTransactionHistory(id, page, size);
        return ResponseEntity.ok(transactions);
    }
}
