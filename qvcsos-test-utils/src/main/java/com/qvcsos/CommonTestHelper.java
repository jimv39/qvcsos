/*
 * Copyright 2022 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qvcsos;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.TimerManager;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton Common test helper class.
 *
 * @author Jim Voris
 */
public final class CommonTestHelper {
    private static final CommonTestHelper COMMON_TEST_HELPER = new CommonTestHelper();

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonTestHelper.class);
    private static final Long ONE_SECOND = 100L;
    private final Object syncObject;
    private final AtomicBoolean atomicLock;

    /**
     * Creates a new instance of CommonTestHelper.
     */
    private CommonTestHelper() {
        this.syncObject = new Object();
        this.atomicLock = new AtomicBoolean(false);
    }

    /**
     * Get the CommonTestHelper singleton.
     *
     * @return the CommonTestHelper singleton.
     */
    public static CommonTestHelper getCommonTestHelper() {
        return COMMON_TEST_HELPER;
    }

    /**
     * Use a psql script to reset the test database.
     */
    public void resetTestDatabaseViaPsqlScript() {
        if (!this.atomicLock.get()) {
            throw new QVCSRuntimeException("You don't have the lock!!!");
        }
        String userDir = System.getProperty("user.dir");
        try {
            Thread.sleep(ONE_SECOND);
            String execString = String.format("psql -f %s/postgres_qvcsos410_test_script.sql postgresql://postgres:postgres@localhost:5432/postgres", userDir);
            Process p = Runtime.getRuntime().exec(execString);
            p.waitFor();
            LOGGER.info("Reset test database process exit value: [{}]", p.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Use a psql script to reset the test database.
     */
    public void resetQvcsosTestDatabaseViaPsqlScript() {
        if (!this.atomicLock.get()) {
            throw new QVCSRuntimeException("You don't have the lock!!!");
        }
        String userDir = System.getProperty("user.dir");
        try {
            String execString = String.format("psql -f %s/postgres_qvcsos410_create_test_project_script.sql postgresql://postgres:postgres@localhost:5432/postgres", userDir);
            Process p = Runtime.getRuntime().exec(execString);
            p.waitFor();
            Thread.sleep(ONE_SECOND);
            LOGGER.info("Reset test database process exit value: [{}]", p.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Make sure that only one test that uses the database can run at a time.
     * This is done because most of the test code resets the test database to a
     * known state. If some test runner is multi-threaded, running these
     * database-using tests at the same time on different threads will cause
     * database collisions, and cause the tests to fail because of these
     * collisions.
     *
     * @throws InterruptedException
     */
    public void acquireSyncObject() throws InterruptedException {
        if (atomicLock.get()) {
            synchronized (syncObject) {
                LOGGER.info("Waiting for sync object.");
                syncObject.wait();
                LOGGER.info("Waiting completed.");
                atomicLock.set(true);
                WorkfileDigestManager.getInstance().cancelSaveOfStoreTask();
                TimerManager.getInstance().getTimer().purge();
            }
        } else {
            atomicLock.set(true);
            WorkfileDigestManager.getInstance().cancelSaveOfStoreTask();
            TimerManager.getInstance().getTimer().purge();
        }
    }

    public void releaseSyncObject() {
        synchronized (syncObject) {
            atomicLock.set(false);
            WorkfileDigestManager.getInstance().cancelSaveOfStoreTask();
            TimerManager.getInstance().getTimer().purge();
            syncObject.notify();
        }
    }

}
