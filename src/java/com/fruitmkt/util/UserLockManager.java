package com.fruitmkt.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class UserLockManager {
    private static final ConcurrentHashMap<Integer, ReentrantLock> locks = new ConcurrentHashMap<>();

    public static ReentrantLock getLock(int userId) {
        return locks.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    public static void cleanUp(int userId) {
        ReentrantLock lock = locks.get(userId);
        if (lock != null && !lock.hasQueuedThreads()) {
            locks.remove(userId, lock);
        }
    }
}
