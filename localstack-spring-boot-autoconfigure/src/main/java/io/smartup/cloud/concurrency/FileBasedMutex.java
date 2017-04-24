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
    public static final Logger LOG = LoggerFactory.getLogger(FileBasedMutex.class);
    private final File file;
    private final FileChannel fileChannel;
    private FileLock fileLock;

    public FileBasedMutex(String fileName) {
        try {
            file = new File(fileName);
            fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1);
        } catch (IOException e) {
            throw new FileBasedOperationException("Error occured during lockfile creation: {}", e);
        }
    }

    /**
     * Lock the mutex
     */
    public void lock() {
        try {
            fileLock = fileChannel.lock();
        } catch (IOException e) {
            throw new FileBasedOperationException("Error occured during lock", e);
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
     * Close the counter
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

    /**
     * Destroy the counter
     * <p>
     * This method will close the counter and delete the backing file.
     */
    public void destroy() {
        close();
        if (file != null) {
            file.delete();
        }
    }

}
