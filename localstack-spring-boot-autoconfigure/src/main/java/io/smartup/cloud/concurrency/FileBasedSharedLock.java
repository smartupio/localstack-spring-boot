package io.smartup.cloud.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class FileBasedSharedLock {
    private static final Logger LOG = LoggerFactory.getLogger(FileBasedMutex.class);
    private static final int LOCK_RETRY_COUNT = 3;
    private static final int LOCK_RETRY_TIME = 3000;

    private FileLock fileLock;
    private final FileChannel fileChannel;

    public FileBasedSharedLock(String fileName) {
        try {
            File file = File.createTempFile(fileName, "");
            fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new FileBasedOperationException("Error occurred during lockfile creation: ", e);
        }
    }

    public void lock() {
        if (!fileChannel.isOpen()) {
            throw new FileBasedOperationException("Error occurred during shared lock creation fileChannel is close");
        }

        try {
            fileLock = fileChannel.lock(0, 1, true);
        } catch (Exception e) {
            LOG.info("Shared locking failed: {}", e);
        }
    }

    public boolean lockExclusive() {
        if (!fileChannel.isOpen()) {
            return false;
        }

        if (fileLock != null && fileLock.isValid()) {
            release();
        }

        int i = 0;
        boolean isLocked = false;
        while (!isLocked && i < LOCK_RETRY_COUNT) {
            fileLock = tryLockExclusive();

            if (fileLock != null) {
                isLocked = true;
            } else {
                if (i < LOCK_RETRY_COUNT - 1) {
                    sleep(LOCK_RETRY_TIME);
                }
                i++;
            }
        }

        return isLocked;
    }

    /**
     * Release the lock
     */
    private void release() {
        try {
            if (fileLock != null) {
                fileLock.release();
                fileLock = null;
            }
        } catch (IOException e) {
            throw new FileBasedOperationException("Error occurred during release", e);
        }
    }

    private FileLock tryLockExclusive() {
        FileLock fileLock = null;
        try {
            fileLock = fileChannel.tryLock(0, 1, false);
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
