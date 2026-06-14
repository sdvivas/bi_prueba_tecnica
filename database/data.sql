-- ==========================================
-- CLIENTS
-- ==========================================

INSERT INTO clients (
    id,
    full_name,
    email
)
VALUES
(
    '11111111-1111-1111-1111-111111111111',
    'Juan Perez',
    'juan.perez@novobanco.com'
),
(
    '22222222-2222-2222-2222-222222222222',
    'Maria Garcia',
    'maria.garcia@novobanco.com'
),
(
    '33333333-3333-3333-3333-333333333333',
    'Carlos Rodriguez',
    'carlos.rodriguez@novobanco.com'
),
(
    '44444444-4444-4444-4444-444444444444',
    'Ana Martinez',
    'ana.martinez@novobanco.com'
);

-- ==========================================
-- ACCOUNTS
-- 2 cuentas por cliente
-- ==========================================

INSERT INTO accounts (
    id,
    client_id,
    account_number,
    account_type,
    currency,
    balance,
    status
)
VALUES
(
    'AAAAAAA1-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    '1000000001',
    'SAVINGS',
    'USD',
    2500.00,
    'ACTIVE'
),
(
    'AAAAAAA2-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    '1000000002',
    'CHECKING',
    'USD',
    1200.00,
    'ACTIVE'
),

(
    'BBBBBBB1-2222-2222-2222-222222222222',
    '22222222-2222-2222-2222-222222222222',
    '1000000003',
    'SAVINGS',
    'USD',
    3100.00,
    'ACTIVE'
),
(
    'BBBBBBB2-2222-2222-2222-222222222222',
    '22222222-2222-2222-2222-222222222222',
    '1000000004',
    'CHECKING',
    'USD',
    800.00,
    'ACTIVE'
),

(
    'CCCCCCC1-3333-3333-3333-333333333333',
    '33333333-3333-3333-3333-333333333333',
    '1000000005',
    'SAVINGS',
    'USD',
    4500.00,
    'ACTIVE'
),
(
    'CCCCCCC2-3333-3333-3333-333333333333',
    '33333333-3333-3333-3333-333333333333',
    '1000000006',
    'CHECKING',
    'USD',
    900.00,
    'ACTIVE'
),

(
    'DDDDDDD1-4444-4444-4444-444444444444',
    '44444444-4444-4444-4444-444444444444',
    '1000000007',
    'SAVINGS',
    'USD',
    1500.00,
    'ACTIVE'
),
(
    'DDDDDDD2-4444-4444-4444-444444444444',
    '44444444-4444-4444-4444-444444444444',
    '1000000008',
    'CHECKING',
    'USD',
    600.00,
    'ACTIVE'
),

(
    'EEEEEEE1-5555-5555-5555-555555555555',
    '11111111-1111-1111-1111-111111111111',
    '1000000009',
    'SAVINGS',
    'USD',
    500.00,
    'BLOCKED'
),
(
    'FFFFFFF1-6666-6666-6666-666666666666',
    '22222222-2222-2222-2222-222222222222',
    '1000000010',
    'CHECKING',
    'USD',
    0.00,
    'CLOSED'
);

-- ==========================================
-- TRANSFERS
-- ==========================================

INSERT INTO transfers (
    id,
    reference,
    source_account_id,
    destination_account_id,
    amount,
    status
)
VALUES
(
    '55555555-0000-0000-0000-000000000001',
    'TRX-20260614-0001',
    'AAAAAAA1-1111-1111-1111-111111111111',
    'BBBBBBB1-2222-2222-2222-222222222222',
    150.00,
    'SUCCESS'
),
(
    '55555555-0000-0000-0000-000000000002',
    'TRX-20260614-0002',
    'BBBBBBB1-2222-2222-2222-222222222222',
    'CCCCCCC1-3333-3333-3333-333333333333',
    250.00,
    'SUCCESS'
),
(
    '55555555-0000-0000-0000-000000000003',
    'TRX-20260614-0003',
    'CCCCCCC1-3333-3333-3333-333333333333',
    'DDDDDDD1-4444-4444-4444-444444444444',
    500.00,
    'SUCCESS'
),
(
    '55555555-0000-0000-0000-000000000004',
    'TRX-20260614-0004',
    'DDDDDDD1-4444-4444-4444-444444444444',
    'AAAAAAA1-1111-1111-1111-111111111111',
    100.00,
    'SUCCESS'
),
(
    '55555555-0000-0000-0000-000000000005',
    'TRX-20260614-0005',
    'AAAAAAA2-1111-1111-1111-111111111111',
    'CCCCCCC2-3333-3333-3333-333333333333',
    75.00,
    'SUCCESS'
);

-- ==========================================
-- TRANSACTIONS
-- (movimientos derivados de transferencias)
-- ==========================================

INSERT INTO transactions (
    id,
    account_id,
    reference,
    transaction_type,
    amount,
    status,
    description
)
VALUES

-- Transferencia 1
(
    NEWID(),
    'AAAAAAA1-1111-1111-1111-111111111111',
    'TRX-20260614-0001-OUT',
    'TRANSFER_OUT',
    150.00,
    'SUCCESS',
    'Transferencia enviada'
),
(
    NEWID(),
    'BBBBBBB1-2222-2222-2222-222222222222',
    'TRX-20260614-0001-IN',
    'TRANSFER_IN',
    150.00,
    'SUCCESS',
    'Transferencia recibida'
),

-- Transferencia 2
(
    NEWID(),
    'BBBBBBB1-2222-2222-2222-222222222222',
    'TRX-20260614-0002-OUT',
    'TRANSFER_OUT',
    250.00,
    'SUCCESS',
    'Transferencia enviada'
),
(
    NEWID(),
    'CCCCCCC1-3333-3333-3333-333333333333',
    'TRX-20260614-0002-IN',
    'TRANSFER_IN',
    250.00,
    'SUCCESS',
    'Transferencia recibida'
),

-- Transferencia 3
(
    NEWID(),
    'CCCCCCC1-3333-3333-3333-333333333333',
    'TRX-20260614-0003-OUT',
    'TRANSFER_OUT',
    500.00,
    'SUCCESS',
    'Transferencia enviada'
),
(
    NEWID(),
    'DDDDDDD1-4444-4444-4444-444444444444',
    'TRX-20260614-0003-IN',
    'TRANSFER_IN',
    500.00,
    'SUCCESS',
    'Transferencia recibida'
),

-- Transferencia 4
(
    NEWID(),
    'DDDDDDD1-4444-4444-4444-444444444444',
    'TRX-20260614-0004-OUT',
    'TRANSFER_OUT',
    100.00,
    'SUCCESS',
    'Transferencia enviada'
),
(
    NEWID(),
    'AAAAAAA1-1111-1111-1111-111111111111',
    'TRX-20260614-0004-IN',
    'TRANSFER_IN',
    100.00,
    'SUCCESS',
    'Transferencia recibida'
),

-- Transferencia 5
(
    NEWID(),
    'AAAAAAA2-1111-1111-1111-111111111111',
    'TRX-20260614-0005-OUT',
    'TRANSFER_OUT',
    75.00,
    'SUCCESS',
    'Transferencia enviada'
),
(
    NEWID(),
    'CCCCCCC2-3333-3333-3333-333333333333',
    'TRX-20260614-0005-IN',
    'TRANSFER_IN',
    75.00,
    'SUCCESS',
    'Transferencia recibida'
),

(
    NEWID(),
    'AAAAAAA1-1111-1111-1111-111111111111',
    'TRX-20260613-DEP001',
    'DEPOSIT',
    1000.00,
    'SUCCESS',
    'Deposito en ventanilla'
),
(
    NEWID(),
    'AAAAAAA1-1111-1111-1111-111111111111',
    'TRX-20260613-WIT001',
    'WITHDRAWAL',
    200.00,
    'SUCCESS',
    'Retiro en cajero'
),
(
    NEWID(),
    'BBBBBBB1-2222-2222-2222-222222222222',
    'TRX-20260612-DEP002',
    'DEPOSIT',
    500.00,
    'SUCCESS',
    'Deposito electronico'
),
(
    NEWID(),
    'BBBBBBB1-2222-2222-2222-222222222222',
    'TRX-20260612-WIT002',
    'WITHDRAWAL',
    100.00,
    'SUCCESS',
    'Retiro en ventanilla'
),
(
    NEWID(),
    'CCCCCCC1-3333-3333-3333-333333333333',
    'TRX-20260611-DEP003',
    'DEPOSIT',
    2000.00,
    'SUCCESS',
    'Deposito nomina'
),
(
    NEWID(),
    'CCCCCCC1-3333-3333-3333-333333333333',
    'TRX-20260611-WIT003',
    'WITHDRAWAL',
    350.00,
    'SUCCESS',
    'Retiro en cajero'
),
(
    NEWID(),
    'DDDDDDD1-4444-4444-4444-444444444444',
    'TRX-20260610-DEP004',
    'DEPOSIT',
    750.00,
    'SUCCESS',
    'Deposito en ventanilla'
),
(
    NEWID(),
    'DDDDDDD1-4444-4444-4444-444444444444',
    'TRX-20260610-WIT004',
    'WITHDRAWAL',
    50.00,
    'SUCCESS',
    'Retiro en cajero'
),
(
    NEWID(),
    'AAAAAAA2-1111-1111-1111-111111111111',
    'TRX-20260609-DEP005',
    'DEPOSIT',
    300.00,
    'SUCCESS',
    'Deposito electronico'
),
(
    NEWID(),
    'AAAAAAA2-1111-1111-1111-111111111111',
    'TRX-20260609-WIT005',
    'WITHDRAWAL',
    150.00,
    'SUCCESS',
    'Retiro en ventanilla'
);