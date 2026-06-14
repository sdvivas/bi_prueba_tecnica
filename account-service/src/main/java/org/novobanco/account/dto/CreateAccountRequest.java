package org.novobanco.account.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public class CreateAccountRequest {

    @NotNull(message = "Client ID is required")
    private UUID clientId;

    @NotNull(message = "Account type is required")
    @Pattern(regexp = "SAVINGS|CHECKING", message = "Account type must be SAVINGS or CHECKING")
    private String accountType;

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
