package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountNotVerifiedException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(AccountNotVerifiedException.class);
    public AccountNotVerifiedException(String message) {
        super(message);
        logger.error("Account not verified");
    }
}
