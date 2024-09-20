CREATE DATABASE multi_cloud;

\c multi_cloud;

CREATE TABLE IF NOT EXISTS users ( id BIGSERIAL PRIMARY KEY, username VARCHAR(255) UNIQUE NOT NULL, email VARCHAR(255) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, first_name VARCHAR(255), last_name VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, verification_code VARCHAR(255), verification_expiration TIMESTAMP, enabled BOOLEAN NOT NULL);
