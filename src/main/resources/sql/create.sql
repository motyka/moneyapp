CREATE TABLE IF NOT EXISTS "ACCOUNTS"
(
    id             BIGINT auto_increment,
    record_version BIGINT,
    balance        DECIMAL,
    active         BOOLEAN
);

ALTER TABLE "ACCOUNTS"
    ADD CONSTRAINT IF NOT EXISTS "pk_account_id"
        PRIMARY KEY (id);

CREATE TABLE IF NOT EXISTS "TRANSFERS"
(
    id           BIGINT auto_increment,
    senderId     BIGINT,
    recipientId  BIGINT,
    amount       DECIMAL,
    errorMessage VARCHAR,
    succeed      BOOLEAN
);

ALTER TABLE "TRANSFERS"
    ADD CONSTRAINT IF NOT EXISTS "pk_transfer_id"
        PRIMARY KEY (id);

ALTER TABLE "TRANSFERS"
    ADD CONSTRAINT IF NOT EXISTS "fk_sender_id"
        FOREIGN KEY (senderId) REFERENCES "ACCOUNTS" (id);

ALTER TABLE "TRANSFERS"
    ADD CONSTRAINT IF NOT EXISTS "fk_recipient_id"
        FOREIGN KEY (recipientId) REFERENCES "ACCOUNTS" (id);
