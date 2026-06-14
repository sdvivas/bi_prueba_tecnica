package org.novobanco.account.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class BalanceResponse {

    private UUID accountId;
    private String accountNumber;
    private BigDecimal balance;
    private String currency;

    public BalanceResponse(UUID accountId, String accountNumber, BigDecimal balance, String currency) {
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
