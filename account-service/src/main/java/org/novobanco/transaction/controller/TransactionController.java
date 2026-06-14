package org.novobanco.transaction.controller;

import jakarta.validation.Valid;
import org.novobanco.transaction.dto.*;
import org.novobanco.transaction.service.TransactionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public TransactionResponse deposit(@Valid @RequestBody DepositRequest request) {
        return transactionService.deposit(request);
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(@Valid @RequestBody WithdrawRequest request) {
        return transactionService.withdraw(request);
    }

    @PostMapping("/transfer")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }
}
