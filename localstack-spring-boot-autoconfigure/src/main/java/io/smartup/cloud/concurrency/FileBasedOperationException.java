package io.smartup.cloud.concurrency;

public class FileBasedOperationException extends RuntimeException {
    public FileBasedOperationException() {
    }

    public FileBasedOperationException(String message) {
        super(message);
    }

    public FileBasedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
