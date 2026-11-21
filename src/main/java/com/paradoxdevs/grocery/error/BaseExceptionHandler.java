package com.paradoxdevs.grocery.error;

import com.paradoxdevs.grocery.error.exception.InvalidCredentialsException;
import com.paradoxdevs.grocery.error.strategy.ExceptionHandlingStrategy;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.ParameterizedType;

@RequiredArgsConstructor
public abstract class BaseExceptionHandler<T extends Exception> implements ExceptionHandlingStrategy {
    protected final ErrorResponseBuilder errorResponseBuilder;

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Exception> getSupportedException() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }
}
