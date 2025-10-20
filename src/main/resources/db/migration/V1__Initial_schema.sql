-- Users table
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(100) NOT NULL,
    address_line2 VARCHAR(100),
    address_line3 VARCHAR(100),
    address_town VARCHAR(50) NOT NULL,
    address_county VARCHAR(50) NOT NULL,
    address_postcode VARCHAR(10) NOT NULL,
    created_timestamp TIMESTAMP NOT NULL,
    updated_timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_email ON users(email);

-- Bank accounts table
CREATE TABLE accounts (
    account_number VARCHAR(8) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    sort_code VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_timestamp TIMESTAMP NOT NULL,
    updated_timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- Transactions table
CREATE TABLE transactions (
    id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(8) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    type VARCHAR(20) NOT NULL,
    reference VARCHAR(200),
    created_timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_transactions_account_number ON transactions(account_number);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);