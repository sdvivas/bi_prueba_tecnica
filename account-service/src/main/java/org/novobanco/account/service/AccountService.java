package org.novobanco.account.service;

import org.novobanco.account.dto.AccountResponse;
import org.novobanco.account.dto.BalanceResponse;
import org.novobanco.account.dto.CreateAccountRequest;
import org.novobanco.account.entity.Account;
import org.novobanco.account.repository.AccountRepository;
import org.novobanco.client.entity.Client;
import org.novobanco.client.repository.ClientRepository;
import org.novobanco.common.exception.AccountNotFoundException;
import org.novobanco.common.exception.ClientNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    public AccountService(AccountRepository accountRepository, ClientRepository clientRepository) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ClientNotFoundException(request.getClientId()));

        Account account = new Account();
        account.setClient(client);
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setStatus("ACTIVE");

        accountRepository.save(account);

        return AccountResponse.fromEntity(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return AccountResponse.fromEntity(account);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return new BalanceResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency()
        );
    }

    private String generateAccountNumber() {
        long number = 1000000000L + ThreadLocalRandom.current().nextLong(9000000000L);
        return String.valueOf(number);
    }
}
