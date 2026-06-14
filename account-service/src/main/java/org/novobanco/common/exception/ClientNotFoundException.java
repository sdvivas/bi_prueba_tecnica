package org.novobanco.common.exception;

import java.util.UUID;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(UUID clientId) {
        super("Client not found: " + clientId);
    }
}
