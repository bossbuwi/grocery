package com.paradoxdevs.grocery.error.exception;

import com.paradoxdevs.grocery.error.ErrorCode;

public class ResourceHasChildrenException extends ApiException {
    public ResourceHasChildrenException(long childCount, String childEntity) {
        super(
                ErrorCode.RESOURCE_HAS_CHILDREN,
                ErrorCode.RESOURCE_HAS_CHILDREN.formatMessage(childCount, childEntity)
        );
    }
}
