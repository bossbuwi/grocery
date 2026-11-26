package com.paradoxdevs.grocery.error.exception;

import com.paradoxdevs.grocery.error.ErrorCode;

public class BasketItemNotPricedException extends ApiException {
    public BasketItemNotPricedException() {
        super(ErrorCode.BASKET_ITEM_NOT_PRICED);
    }
}
