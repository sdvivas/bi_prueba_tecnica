package org.novobanco.transaction.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.novobanco.account.entity.Account;
import org.novobanco.account.repository.AccountRepository;
import org.novobanco.client.entity.Client;
import org.novobanco.common.exception.AccountNotActiveException;
import org.novobanco.common.exception.AccountNotFoundException;
import org.novobanco.common.exception.InsufficientFundsException;
import org.novobanco.transaction.dto.*;
import org.novobanco.transaction.entity.Transaction;
import org.novobanco.transaction.entity.Transfer;
import org.novobanco.transaction.repository.TransactionRepository;
import org.novobanco.transaction.repository.TransferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account activeAccount;
    private Account blockedAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setFullName("Juan Perez");

        activeAccount = new Account();
        activeAccount.setId(UUID.randomUUID());
        activeAccount.setClient(client);
        activeAccount.setAccountNumber("1000000001");
        activeAccount.setAccountType("SAVINGS");
        activeAccount.setCurrency("USD");
        activeAccount.setBalance(new BigDecimal("2500.00"));
        activeAccount.setStatus("ACTIVE");
        activeAccount.setCreatedAt(LocalDateTime.now());

        blockedAccount = new Account();
        blockedAccount.setId(UUID.randomUUID());
        blockedAccount.setClient(client);
        blockedAccount.setAccountNumber("1000000009");
        blockedAccount.setAccountType("SAVINGS");
        blockedAccount.setCurrency("USD");
        blockedAccount.setBalance(new BigDecimal("500.00"));
        blockedAccount.setStatus("BLOCKED");
        blockedAccount.setCreatedAt(LocalDateTime.now());

        Client client2 = new Client();
        client2.setId(UUID.randomUUID());
        client2.setFullName("Maria Garcia");

        destinationAccount = new Account();
        destinationAccount.setId(UUID.randomUUID());
        destinationAccount.setClient(client2);
        destinationAccount.setAccountNumber("1000000003");
        destinationAccount.setAccountType("SAVINGS");
        destinationAccount.setCurrency("USD");
        destinationAccount.setBalance(new BigDecimal("3100.00"));
        destinationAccount.setStatus("ACTIVE");
        destinationAccount.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void deposit_withActiveAccount_shouldIncreaseBalance() {
        DepositRequest request = new DepositRequest();
        request.setAccountId(activeAccount.getId());
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Deposito de prueba");

        when(accountRepository.findByIdForUpdate(activeAccount.getId())).thenReturn(Optional.of(activeAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        TransactionResponse response = transactionService.deposit(request);

        assertNotNull(response);
        assertEquals("DEPOSIT", response.getTransactionType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("2600.00"), activeAccount.getBalance());
        verify(accountRepository).save(activeAccount);
    }

    @Test
    void deposit_withBlockedAccount_shouldThrowException() {
        DepositRequest request = new DepositRequest();
        request.setAccountId(blockedAccount.getId());
        request.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(blockedAccount.getId())).thenReturn(Optional.of(blockedAccount));

        assertThrows(AccountNotActiveException.class, () -> transactionService.deposit(request));
    }

    @Test
    void deposit_withNonExistentAccount_shouldThrowException() {
        UUID invalidId = UUID.randomUUID();
        DepositRequest request = new DepositRequest();
        request.setAccountId(invalidId);
        request.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(invalidId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionService.deposit(request));
    }

    @Test
    void withdraw_withSufficientFunds_shouldDecreaseBalance() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId(activeAccount.getId());
        request.setAmount(new BigDecimal("500.00"));
        request.setDescription("Retiro de prueba");

        when(accountRepository.findByIdForUpdate(activeAccount.getId())).thenReturn(Optional.of(activeAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        TransactionResponse response = transactionService.withdraw(request);

        assertNotNull(response);
        assertEquals("WITHDRAWAL", response.getTransactionType());
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("2000.00"), activeAccount.getBalance());
    }

    @Test
    void withdraw_withInsufficientFunds_shouldThrowException() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId(activeAccount.getId());
        request.setAmount(new BigDecimal("99999.00"));

        when(accountRepository.findByIdForUpdate(activeAccount.getId())).thenReturn(Optional.of(activeAccount));

        assertThrows(InsufficientFundsException.class, () -> transactionService.withdraw(request));
    }

    @Test
    void withdraw_withBlockedAccount_shouldThrowException() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAccountId(blockedAccount.getId());
        request.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(blockedAccount.getId())).thenReturn(Optional.of(blockedAccount));

        assertThrows(AccountNotActiveException.class, () -> transactionService.withdraw(request));
    }

    @Test
    void transfer_withValidAccounts_shouldMoveBalance() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(activeAccount.getId());
        request.setDestinationAccountId(destinationAccount.getId());
        request.setAmount(new BigDecimal("200.00"));
        request.setDescription("Transferencia de prueba");

        UUID firstLock = activeAccount.getId().compareTo(destinationAccount.getId()) < 0
                ? activeAccount.getId() : destinationAccount.getId();
        UUID secondLock = activeAccount.getId().compareTo(destinationAccount.getId()) < 0
                ? destinationAccount.getId() : activeAccount.getId();

        Account first = firstLock.equals(activeAccount.getId()) ? activeAccount : destinationAccount;
        Account second = firstLock.equals(activeAccount.getId()) ? destinationAccount : activeAccount;

        when(accountRepository.findByIdForUpdate(firstLock)).thenReturn(Optional.of(first));
        when(accountRepository.findByIdForUpdate(secondLock)).thenReturn(Optional.of(second));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(UUID.randomUUID());
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        TransferResponse response = transactionService.transfer(request);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("2300.00"), activeAccount.getBalance());
        assertEquals(new BigDecimal("3300.00"), destinationAccount.getBalance());
    }

    @Test
    void transfer_toSameAccount_shouldThrowException() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(activeAccount.getId());
        request.setDestinationAccountId(activeAccount.getId());
        request.setAmount(new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(request));
    }

    @Test
    void transfer_withInsufficientFunds_shouldThrowException() {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId(activeAccount.getId());
        request.setDestinationAccountId(destinationAccount.getId());
        request.setAmount(new BigDecimal("99999.00"));

        UUID firstLock = activeAccount.getId().compareTo(destinationAccount.getId()) < 0
                ? activeAccount.getId() : destinationAccount.getId();
        UUID secondLock = activeAccount.getId().compareTo(destinationAccount.getId()) < 0
                ? destinationAccount.getId() : activeAccount.getId();

        Account first = firstLock.equals(activeAccount.getId()) ? activeAccount : destinationAccount;
        Account second = firstLock.equals(activeAccount.getId()) ? destinationAccount : activeAccount;

        when(accountRepository.findByIdForUpdate(firstLock)).thenReturn(Optional.of(first));
        when(accountRepository.findByIdForUpdate(secondLock)).thenReturn(Optional.of(second));

        assertThrows(InsufficientFundsException.class, () -> transactionService.transfer(request));
    }

    @Test
    void getTransactionHistory_withValidAccount_shouldReturnPage() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccount(activeAccount);
        transaction.setReference("TRX-TEST-001");
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(LocalDateTime.now());

        Page<Transaction> page = new PageImpl<>(List.of(transaction), PageRequest.of(0, 20), 1);

        when(accountRepository.findById(activeAccount.getId())).thenReturn(Optional.of(activeAccount));
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(activeAccount.getId(), PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<TransactionResponse> result = transactionService.getTransactionHistory(activeAccount.getId(), 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("DEPOSIT", result.getContent().get(0).getTransactionType());
    }

    @Test
    void getTransactionHistory_withInvalidAccount_shouldThrowException() {
        UUID invalidId = UUID.randomUUID();
        when(accountRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> transactionService.getTransactionHistory(invalidId, 0, 20));
    }
}
