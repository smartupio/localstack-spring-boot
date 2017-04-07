package io.smartup.docker;

public class DockerServiceException extends RuntimeException {
    public DockerServiceException(Throwable cause) {
        super(cause);
    }

    public DockerServiceException(String message) {
        super(message);
    }
}
