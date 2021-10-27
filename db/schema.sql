CREATE TABLE account
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR NOT NULL,
    email    VARCHAR NOT NULL UNIQUE,
    phone    VARCHAR NOT NULL UNIQUE
);

CREATE TABLE ticket
(
    id         SERIAL,
    session_id INTEGER NOT NULL,
    row        INTEGER NOT NULL,
    cell       INTEGER NOT NULL,
    account_id INTEGER NOT NULL
        CONSTRAINT ticket_account_id_fkey
            REFERENCES account,
    CONSTRAINT ticket_pk
        PRIMARY KEY (session_id, row, cell)
);
