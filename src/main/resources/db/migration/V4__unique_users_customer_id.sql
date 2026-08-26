CREATE UNIQUE INDEX uk_users_customer_id ON users (customer_id)
    WHERE customer_id IS NOT NULL;
