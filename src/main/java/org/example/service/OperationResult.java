package org.example.service;

public class OperationResult<T> {
    private final boolean success;
    private final String message;
    private final T payload;

    public OperationResult(boolean success, String message, T payload) {
        this.success = success;
        this.message = message;
        this.payload = payload;
    }

    public static <T> OperationResult<T> success(String message, T payload) {
        return new OperationResult<>(true, message, payload);
    }

    public static <T> OperationResult<T> success(String message) {
        return new OperationResult<>(true, message, null);
    }

    public static <T> OperationResult<T> failure(String message) {
        return new OperationResult<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getPayload() {
        return payload;
    }
}
