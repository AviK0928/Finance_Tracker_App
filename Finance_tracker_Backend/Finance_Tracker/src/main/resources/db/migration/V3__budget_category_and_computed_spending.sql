-- V3: budgets track real spending.
-- * category: optional; NULL means the budget covers all expenses in its date range.
-- * spent_amount is dropped: it was never updated by transactions. Spending is now
--   computed on read with SUM() over the user's expense transactions.
-- * end_date may not precede start_date.
ALTER TABLE budgets ADD COLUMN category VARCHAR(255);

ALTER TABLE budgets DROP COLUMN spent_amount;

ALTER TABLE budgets
    ADD CONSTRAINT chk_budgets_date_range CHECK (end_date >= start_date);
