package org.novobanco.common.exception;

import java.util.UUID;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(UUID accountId) {
        super("Account is not active: " + accountId);
    }
}
