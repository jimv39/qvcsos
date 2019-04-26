/*   Copyright 2004-2014 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that supplies both read locks and write locks for a resource. If a resource needs a read lock, it should call getReadLock(); if a resource needs a write lock,
 * it should call getWriteLock(). This class will guarantee that only one writer holds a write lock, and all readers are blocked. Many concurrent readers are allowed.
 * Re-written to use the java.util.concurrent.lock.ReentrantReadWriteLock class.
 *
 * @author Jim Voris
 */
public class ReadWriteLock {
    private final ReentrantReadWriteLock reentrantReadWriteLock;

    /**
     * Creates a new instance of ReadWriteLock.
     */
    public ReadWriteLock() {
        reentrantReadWriteLock = new ReentrantReadWriteLock();
    }

    /**
     * Get a read lock. This method is synchronous. It will return only after the read lock has been acquired.
     */
    public void getReadLock() {
        reentrantReadWriteLock.readLock().lock();
    }

    /**
     * Release the read lock.
     */
    public void releaseReadLock() {
        reentrantReadWriteLock.readLock().unlock();
    }

    /**
     * Get a write lock. This method is synchronous and will return only after the write lock is acquired.
     */
    public void getWriteLock() {
        reentrantReadWriteLock.writeLock().lock();
    }

    /**
     * Release the write lock.
     */
    public void releaseWriteLock() {
        reentrantReadWriteLock.writeLock().unlock();
    }
}
