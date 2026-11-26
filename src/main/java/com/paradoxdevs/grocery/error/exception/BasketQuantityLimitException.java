package com.paradoxdevs.grocery.error.exception;

import com.paradoxdevs.grocery.error.ErrorCode;

public class BasketQuantityLimitException extends ApiException {
    public BasketQuantityLimitException() {
        super(ErrorCode.BASKET_QUANTITY_LIMIT);
    }
}
