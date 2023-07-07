package net.gudenau.panama.internal;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public final class SharedLock {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock read = lock.readLock();
    private final Lock write = lock.writeLock();

    public void read(Runnable action) {
        read.lock();
        try {
            action.run();
        } finally {
            read.unlock();
        }
    }

    public <T> T read(Supplier<T> action) {
        read.lock();
        try {
            return action.get();
        } finally {
            read.unlock();
        }
    }

    public void write(Runnable action) {
        write.lock();
        try {
            action.run();
        } finally {
            write.unlock();
        }
    }

    public <T> T write(Supplier<T> action) {
        write.lock();
        try {
            return action.get();
        } finally {
            write.unlock();
        }
    }
}
