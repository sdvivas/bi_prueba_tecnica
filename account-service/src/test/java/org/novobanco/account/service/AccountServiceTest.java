package org.novobanco.account.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.novobanco.account.dto.AccountResponse;
import org.novobanco.account.dto.BalanceResponse;
import org.novobanco.account.dto.CreateAccountRequest;
import org.novobanco.account.entity.Account;
import org.novobanco.account.repository.AccountRepository;
import org.novobanco.client.entity.Client;
import org.novobanco.client.repository.ClientRepository;
import org.novobanco.common.exception.AccountNotFoundException;
import org.novobanco.common.exception.ClientNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AccountService accountService;

    private Client client;
    private Account account;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(UUID.randomUUID());
        client.setFullName("Juan Perez");
        client.setEmail("juan@novobanco.com");
        client.setCreatedAt(LocalDateTime.now());

        account = new Account();
        account.setId(UUID.randomUUID());
        account.setClient(client);
        account.setAccountNumber("1000000001");
        account.setAccountType("SAVINGS");
        account.setCurrency("USD");
        account.setBalance(new BigDecimal("2500.00"));
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createAccount_withValidClient_shouldReturnAccountResponse() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setClientId(client.getId());
        request.setAccountType("SAVINGS");

        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("SAVINGS", response.getAccountType());
        assertEquals("ACTIVE", response.getStatus());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_withInvalidClient_shouldThrowException() {
        UUID invalidId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest();
        request.setClientId(invalidId);
        request.setAccountType("SAVINGS");

        when(clientRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () -> accountService.createAccount(request));
    }

    @Test
    void getAccount_withValidId_shouldReturnAccount() {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(account.getId());

        assertNotNull(response);
        assertEquals(account.getAccountNumber(), response.getAccountNumber());
        assertEquals(account.getBalance(), response.getBalance());
    }

    @Test
    void getAccount_withInvalidId_shouldThrowException() {
        UUID invalidId = UUID.randomUUID();
        when(accountRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(invalidId));
    }

    @Test
    void getBalance_withValidId_shouldReturnBalance() {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance(account.getId());

        assertNotNull(response);
        assertEquals(account.getBalance(), response.getBalance());
        assertEquals("USD", response.getCurrency());
    }

    @Test
    void getBalance_withInvalidId_shouldThrowException() {
        UUID invalidId = UUID.randomUUID();
        when(accountRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(invalidId));
    }
}
