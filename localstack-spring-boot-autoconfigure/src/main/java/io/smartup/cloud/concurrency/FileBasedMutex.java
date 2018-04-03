package io.smartup.cloud.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

/**
 * FileBasedMutex functions as mutex between multiple JVM processes
 * <p>
 * For locking the class uses FileChannel and FileLock.
 *
 * @see FileChannel
 * @see FileLock
 */
public class FileBasedMutex {
    private static final Logger LOG = LoggerFactory.getLogger(FileBasedMutex.class);
    private static final int LOCK_RETRY_COUNT = 5;
    private static final int LOCK_RETRY_TIME = 5000;

    private FileLock fileLock;

    private final FileChannel fileChannel;

    public FileBasedMutex(String fileName) {
        try {
            File file = File.createTempFile(fileName, "");
            fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new FileBasedOperationException("Error occured during lockfile creation: {}", e);
        }
    }

    public void lock() {
        if (!fileChannel.isOpen()) {
            return;
        }
        int i = 0;
        boolean isLocked = false;
        while (!isLocked && i < LOCK_RETRY_COUNT) {
            fileLock = tryLock();

            if (fileLock != null) {
                isLocked = true;
            } else {
                if (i < LOCK_RETRY_COUNT - 1) {
                    sleep(LOCK_RETRY_TIME);
                }
                i++;
            }
        }

        if (!isLocked) {
            throw new FileBasedOperationException("Could not acquire lock!");
        }
    }

    /**
     * Release the mutex
     */
    public void release() {
        try {
            if (fileLock != null) {
                fileLock.release();
                fileLock = null;
            }
        } catch (IOException e) {
            throw new FileBasedOperationException("Error occurred during release", e);
        }
    }

    /**
     * Close the mutex
     * <p>
     * This method will release the FileLock if there is one and it will close the connection to the file.
     */
    public void close() {
        try {
            release();
            if (fileChannel != null && fileChannel.isOpen()) {
                fileChannel.close();
            }
        } catch (IOException e) {
            LOG.error("Error occurred during close: {}", e);
        }
    }

    private FileLock tryLock() {
        FileLock fileLock = null;
        try {
            fileLock = fileChannel.tryLock();
        } catch (Exception e) {
            LOG.info("tryLock failed: ", e);
        }
        return fileLock;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new FileBasedOperationException("Interrupted while waiting to retry to acquire lock: ", e);
        }
    }
}
