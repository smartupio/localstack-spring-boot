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
 *
 * For synchronization the class uses FileChannel and FileLock.
 *
 * @see FileChannel
 * @see FileLock
 */
public class FileBasedCounter {
    private File file;
    private FileChannel fileChannel;
    private IntBuffer intBuffer;
    private FileLock fileLock;

    private static final Logger LOG = LoggerFactory.getLogger(FileBasedCounter.class);

    public FileBasedCounter(String name) {
        createLockFile(name);
    }

    /**
     * Increments the counter
     * @return the new value of the counter
     */
    public int increment() {
        lock();

        int currentValue = getValue() + 1;
        setValue(currentValue);

        release();

        return currentValue;
    }

    /**
     * Decrements the counter
     * @return the new value of the counter
     */
    public int decrement() {
        lock();

        int currentValue = getValue() - 1;
        setValue(currentValue);

        release();

        return currentValue;
    }

    /**
     * Close the counter
     *
     * This method will release the FileLock if there is one and it will close the connection to the file.
     */
    public void close() {
        try {
            release();
            if (fileChannel != null && fileChannel.isOpen()) {
                fileChannel.close();
            }
        } catch (IOException e) {
            LOG.error("Error occurred during counter close: {}", e);
        }
    }

    /**
     * Destroy the counter
     *
     * This method will close the counter and delete the backing file.
     */
    public void destroy() {
        close();
        if (file != null) {
            file.delete();
        }
    }

    /**
     * Acquire FileLock
     */
    private void lock() {
        try {
            if (fileChannel != null && fileChannel.isOpen()) {
                fileLock = fileChannel.lock();
            }
        } catch (IOException e) {
            throw new FileBasedOperationException("Could not acquire lock", e);
        }
    }

    /**
     * Release FileLock
     */
    private void release() {
        try {
            if (fileLock != null && fileChannel.isOpen()) {
                fileLock.release();
                fileLock = null;
            }
        } catch (IOException e) {
            throw new FileBasedOperationException("Could not release lock", e);
        }
    }

    /**
     * Get current value of counter
     * @return current value of counter
     */
    private int getValue() {
        intBuffer.position(0);
        return intBuffer.get();
    }

    /**
     * Set value of counter
     * @param value
     */
    private void setValue(int value) {
        intBuffer.position(0);
        intBuffer.put(value);
    }

    /**
     * Create file that will function as lock file and will store the counter's value
     * @param fileName lockfile's name
     */
    private void createLockFile(String fileName) {
        try {
            file = new File(fileName);
            fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4);
            intBuffer = byteBuffer.asIntBuffer();
        } catch (IOException e) {
            LOG.error("Error occured during lockfile creation: {}", e);
        }
    }
}