-- V2: store transaction amounts as exact decimals instead of binary floating point.
-- DOUBLE PRECISION cannot represent values like 0.1 exactly, so SQL SUM() over it
-- drifts (0.1 + 0.2 = 0.30000000000000004). Existing values are rounded to 2 places.
ALTER TABLE transactions
    ALTER COLUMN amount TYPE NUMERIC(19, 2) USING ROUND(amount::NUMERIC, 2);

ALTER TABLE transactions
    ADD CONSTRAINT chk_transactions_amount_positive CHECK (amount > 0);
