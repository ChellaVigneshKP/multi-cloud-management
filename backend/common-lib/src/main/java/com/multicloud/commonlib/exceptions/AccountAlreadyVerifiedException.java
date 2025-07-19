package com.multicloud.commonlib.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountAlreadyVerifiedException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(AccountAlreadyVerifiedException.class);
    public AccountAlreadyVerifiedException(String message) {
        super(message);
        logger.error(message);
    }
}