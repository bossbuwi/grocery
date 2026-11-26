package com.paradoxdevs.grocery.error.exception;

import com.paradoxdevs.grocery.error.ErrorCode;

public class BasketClosedException extends ApiException {
    public BasketClosedException() {
        super(ErrorCode.BASKET_CLOSED);
    }
}
