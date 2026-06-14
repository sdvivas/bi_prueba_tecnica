-- ==========================================
-- CLIENTS
-- ==========================================

CREATE TABLE clients (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

-- ==========================================
-- ACCOUNTS
-- ==========================================

CREATE TABLE accounts (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,

    client_id UNIQUEIDENTIFIER NOT NULL,

    account_number VARCHAR(20) NOT NULL UNIQUE,

    account_type VARCHAR(20) NOT NULL,

    currency VARCHAR(3) NOT NULL DEFAULT 'USD',

    balance DECIMAL(18,2) NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT fk_accounts_client
        FOREIGN KEY (client_id)
        REFERENCES clients(id),

    CONSTRAINT chk_account_type
        CHECK (account_type IN ('SAVINGS', 'CHECKING')),

    CONSTRAINT chk_account_status
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED')),

    CONSTRAINT chk_account_balance
        CHECK (balance >= 0)
);

CREATE INDEX idx_accounts_client_id
ON accounts(client_id);

-- ==========================================
-- TRANSACTIONS
-- ==========================================

CREATE TABLE transactions (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,

    account_id UNIQUEIDENTIFIER NOT NULL,

    reference VARCHAR(100) NOT NULL,

    transaction_type VARCHAR(20) NOT NULL,

    amount DECIMAL(18,2) NOT NULL,

    status VARCHAR(20) NOT NULL,

    description VARCHAR(500) NULL,

    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT uq_transaction_reference
        UNIQUE(reference),

    CONSTRAINT chk_transaction_type
        CHECK (
            transaction_type IN (
                'DEPOSIT',
                'WITHDRAWAL',
                'TRANSFER_IN',
                'TRANSFER_OUT'
            )
        ),

    CONSTRAINT chk_transaction_status
        CHECK (
            status IN (
                'SUCCESS',
                'FAILED',
                'REVERSED'
            )
        ),

    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_transactions_account_created
ON transactions(account_id, created_at DESC);

CREATE INDEX idx_transactions_created_at
ON transactions(created_at);

-- ==========================================
-- TRANSFERS
-- ==========================================

CREATE TABLE transfers (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,

    reference VARCHAR(100) NOT NULL UNIQUE,

    source_account_id UNIQUEIDENTIFIER NOT NULL,

    destination_account_id UNIQUEIDENTIFIER NOT NULL,

    amount DECIMAL(18,2) NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT fk_transfer_source_account
        FOREIGN KEY (source_account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_transfer_destination_account
        FOREIGN KEY (destination_account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_transfer_amount
        CHECK (amount > 0),

    CONSTRAINT chk_transfer_status
        CHECK (
            status IN (
                'SUCCESS',
                'FAILED',
                'REVERSED'
            )
        )
);

CREATE INDEX idx_transfer_source
ON transfers(source_account_id);

CREATE INDEX idx_transfer_destination
ON transfers(destination_account_id);

CREATE INDEX idx_transfer_created_at
ON transfers(created_at);