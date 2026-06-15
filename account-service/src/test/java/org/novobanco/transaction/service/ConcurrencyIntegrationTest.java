package org.novobanco.transaction.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.novobanco.account.entity.Account;
import org.novobanco.account.repository.AccountRepository;
import org.novobanco.client.entity.Client;
import org.novobanco.client.repository.ClientRepository;
import org.novobanco.transaction.dto.WithdrawRequest;
import org.novobanco.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
class ConcurrencyIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private UUID testClientId;
    private UUID testAccountId;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setFullName("Test Concurrency Client");
        client.setEmail("concurrency-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com");
        clientRepository.save(client);
        testClientId = client.getId();

        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setClient(client);
        account.setAccountNumber("TEST" + System.currentTimeMillis());
        account.setAccountType("SAVINGS");
        account.setCurrency("USD");
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus("ACTIVE");
        accountRepository.save(account);
        testAccountId = account.getId();
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll(
                transactionRepository.findByAccountIdOrderByCreatedAtDesc(testAccountId,
                        org.springframework.data.domain.PageRequest.of(0, 100)).getContent()
        );
        accountRepository.deleteById(testAccountId);
        clientRepository.deleteById(testClientId);
    }

    @Test
    void concurrentWithdrawals_shouldRespectBalance() throws InterruptedException {
        // Cuenta con saldo = 1000
        // 10 hilos intentan retirar 200 al mismo tiempo
        // Solo 5 deben tener exito (5 x 200 = 1000)
        // Los otros 5 deben fallar por saldo insuficiente

        int threads = 10;
        BigDecimal withdrawAmount = new BigDecimal("200.00");
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    WithdrawRequest request = new WithdrawRequest();
                    request.setAccountId(testAccountId);
                    request.setAmount(withdrawAmount);
                    request.setDescription("Retiro concurrente");

                    transactionService.withdraw(request);
                    successes.incrementAndGet();
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        Account finalAccount = accountRepository.findById(testAccountId).orElseThrow();

        assertEquals(5, successes.get(), "Exactly 5 withdrawals should succeed");
        assertEquals(5, failures.get(), "Exactly 5 withdrawals should fail");
        assertEquals(0, finalAccount.getBalance().compareTo(BigDecimal.ZERO),
                "Final balance should be exactly 0");
    }
}
