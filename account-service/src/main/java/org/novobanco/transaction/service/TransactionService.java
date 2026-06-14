package org.novobanco.transaction.service;

import org.novobanco.account.entity.Account;
import org.novobanco.account.repository.AccountRepository;
import org.novobanco.common.exception.AccountNotActiveException;
import org.novobanco.common.exception.AccountNotFoundException;
import org.novobanco.common.exception.InsufficientFundsException;
import org.novobanco.transaction.dto.*;
import org.novobanco.transaction.entity.Transaction;
import org.novobanco.transaction.entity.Transfer;
import org.novobanco.transaction.repository.TransactionRepository;
import org.novobanco.transaction.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        validateAccountActive(account);

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        log.info("Deposit successful - Account: {}, Amount: {}, New Balance: {}",
                account.getAccountNumber(), request.getAmount(), account.getBalance());

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setReference(generateReference());
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(request.getDescription());

        transactionRepository.save(transaction);

        return TransactionResponse.fromEntity(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request) {
        Account account = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        validateAccountActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Withdrawal rejected - Account: {}, Requested: {}, Available: {}",
                    account.getAccountNumber(), request.getAmount(), account.getBalance());
            throw new InsufficientFundsException();
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        log.info("Withdrawal successful - Account: {}, Amount: {}, New Balance: {}",
                account.getAccountNumber(), request.getAmount(), account.getBalance());

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setReference(generateReference());
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(request.getDescription());

        transactionRepository.save(transaction);

        return TransactionResponse.fromEntity(transaction);
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new IllegalArgumentException("Source and destination accounts must be different");
        }

        UUID firstLock = request.getSourceAccountId().compareTo(request.getDestinationAccountId()) < 0
                ? request.getSourceAccountId() : request.getDestinationAccountId();
        UUID secondLock = request.getSourceAccountId().compareTo(request.getDestinationAccountId()) < 0
                ? request.getDestinationAccountId() : request.getSourceAccountId();

        Account firstAccount = accountRepository.findByIdForUpdate(firstLock)
                .orElseThrow(() -> new AccountNotFoundException(firstLock));
        Account secondAccount = accountRepository.findByIdForUpdate(secondLock)
                .orElseThrow(() -> new AccountNotFoundException(secondLock));

        Account source = firstLock.equals(request.getSourceAccountId()) ? firstAccount : secondAccount;
        Account destination = firstLock.equals(request.getSourceAccountId()) ? secondAccount : firstAccount;

        validateAccountActive(source);
        validateAccountActive(destination);

        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException();
        }

        source.setBalance(source.getBalance().subtract(request.getAmount()));
        destination.setBalance(destination.getBalance().add(request.getAmount()));
        accountRepository.save(source);
        accountRepository.save(destination);

        log.info("Transfer successful - From: {} To: {}, Amount: {}, Source Balance: {}, Destination Balance: {}",
                source.getAccountNumber(), destination.getAccountNumber(), request.getAmount(),
                source.getBalance(), destination.getBalance());

        String transferReference = generateReference();

        Transfer transfer = new Transfer();
        transfer.setReference(transferReference);
        transfer.setSourceAccount(source);
        transfer.setDestinationAccount(destination);
        transfer.setAmount(request.getAmount());
        transfer.setStatus("SUCCESS");
        transferRepository.save(transfer);

        Transaction outTransaction = new Transaction();
        outTransaction.setAccount(source);
        outTransaction.setReference(transferReference + "-OUT");
        outTransaction.setTransactionType("TRANSFER_OUT");
        outTransaction.setAmount(request.getAmount());
        outTransaction.setStatus("SUCCESS");
        outTransaction.setDescription(request.getDescription());
        transactionRepository.save(outTransaction);

        Transaction inTransaction = new Transaction();
        inTransaction.setAccount(destination);
        inTransaction.setReference(transferReference + "-IN");
        inTransaction.setTransactionType("TRANSFER_IN");
        inTransaction.setAmount(request.getAmount());
        inTransaction.setStatus("SUCCESS");
        inTransaction.setDescription(request.getDescription());
        transactionRepository.save(inTransaction);

        return TransferResponse.fromEntity(transfer);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(UUID accountId, int page, int size) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(page, size))
                .map(TransactionResponse::fromEntity);
    }

    private void validateAccountActive(Account account) {
        if (!account.isActive()) {
            throw new AccountNotActiveException(account.getId());
        }
    }

    private String generateReference() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String unique = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TRX-" + date + "-" + unique;
    }
}
