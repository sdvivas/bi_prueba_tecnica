package org.novobanco.transaction.dto;

import org.novobanco.transaction.entity.Transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransferResponse {

    private UUID id;
    private String reference;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;

    public static TransferResponse fromEntity(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setReference(transfer.getReference());
        response.setSourceAccountId(transfer.getSourceAccount().getId());
        response.setDestinationAccountId(transfer.getDestinationAccount().getId());
        response.setAmount(transfer.getAmount());
        response.setStatus(transfer.getStatus());
        response.setCreatedAt(transfer.getCreatedAt());
        return response;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public UUID getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(UUID destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
