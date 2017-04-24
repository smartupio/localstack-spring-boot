package io.smartup.cloud.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

/**
 * FileBasedCounter lets multiple JVM processes to use a common synchronized counter.
 * <p>
 * For synchronization the class uses FileChannel and FileLock.
 *
 * @see FileChannel
 * @see FileLock
 */
public class FileBasedCounter {
    private static final Logger LOG = LoggerFactory.getLogger(FileBasedCounter.class);
    private static final int LOCK_RETRY_COUNT = 3;
    private static final int LOCK_RETRY_TIME = 3000;

    private final File file;
    private final FileChannel fileChannel;
    private final IntBuffer intBuffer;

    private FileLock fileLock;

    public FileBasedCounter(String fileName) {
        try {
            file = new File(fileName);
            fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4);
            intBuffer = byteBuffer.asIntBuffer();
        } catch (IOException e) {
            throw new FileBasedOperationException("Error occured during lockfile creation: {}", e);
        }
    }

    /**
     * Increments the counter
     *
     * @return the new value of the counter
     */
    public int incrementAndGet() {
        lock();
        try {
            int currentValue = getValue() + 1;
            setValue(currentValue);
            return currentValue;
        } finally {
            release();
        }
    }

    /**
     * Decrements the counter
     *
     * @return the new value of the counter
     */
    public int decrementAndGet() {
        lock();
        try {
            int currentValue = getValue() - 1;
            setValue(currentValue);
            return currentValue;
        } finally {
            release();
        }
    }

    /**
     * Close the counter
     * <p>
     * This method will release the FileLock if there is one and it will close the connection to the file.
     */
    public void close() {
        release();
        try {
            fileChannel.close();
        } catch (IOException e) {
            throw new FileBasedOperationException("Error occurred during counter close: {}", e);
        }
    }

    /**
     * Destroy the counter
     * <p>
     * This method will close the counter and delete the backing file.
     */
    public void destroy() {
        close();
        file.delete();
    }

    /**
     * Acquire FileLock
     */
    private void lock() {
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

    /**
     * Release FileLock
     */
    private void release() {
        try {
            if (fileLock != null) {
                fileLock.release();
                fileLock = null;
            }
        } catch (IOException e) {
            throw new FileBasedOperationException("Could not release lock", e);
        }
    }

    /**
     * Get current value of counter
     *
     * @return current value of counter
     */
    private int getValue() {
        intBuffer.position(0);
        return intBuffer.get();
    }

    /**
     * Set value of counter
     *
     * @param value
     */
    private void setValue(int value) {
        intBuffer.position(0);
        intBuffer.put(value);
    }

}