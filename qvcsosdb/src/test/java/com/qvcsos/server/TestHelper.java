/*
 * Copyright 2021 Jim Voris.
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
package com.qvcsos.server;

import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public final class TestHelper {

    /**
     * Hide the default constructor so it cannot be used.
     */
    private TestHelper() {
    }

    /**
     * Create our logger object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestHelper.class);

    /**
     * Use a psql script to reset the test database.
     */
    public static void resetTestDatabaseViaPsqlScript() {
        String userDir = System.getProperty("user.dir");
        try {
            String execString = String.format("psql -f %s/postgres_qvcsos410_test_script.sql postgresql://postgres:postgres@localhost:5433/postgres", userDir);
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
    public static void createTestProjectViaPsqlScript() {
        String userDir = System.getProperty("user.dir");
        try {
            String execString = String.format("psql -f %s/postgres_qvcsos410_create_test_project_script.sql postgresql://postgres:postgres@localhost:5433/postgres", userDir);
            Process p = Runtime.getRuntime().exec(execString);
            p.waitFor();
            LOGGER.info("Create test project process exit value: [{}]", p.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
    }

    static private byte[] getFileData(java.io.File file) throws FileNotFoundException, IOException {
        byte[] buffer;
        try(FileInputStream inStream = new FileInputStream(file)) {
            buffer = new byte[(int) file.length()];
            Utility.readDataFromStream(buffer, inStream);
        }
        return buffer;
    }

    /**
     * Compare 2 files to see if they have the same contents.
     *
     * @param file1 the first file
     * @param file2 the 2nd file.
     * @return true if they have exactly the same contents; false if they are different.
     * @throws FileNotFoundException if either file cannot be found
     */
    public static boolean compareFilesByteForByte(File file1, File file2) throws FileNotFoundException, IOException {
        LOGGER.info("[{}] ********************************************************* TestHelper.compareFilesByteForByte", Thread.currentThread().getName());
        boolean compareResult = true;
        if (file1.exists() && file2.exists()) {
            if (file1.length() == file2.length()) {
                byte[] file1Data = getFileData(file1);
                byte[] file2Data = getFileData(file2);
                for (int i = 0; i < file1.length(); i++) {
                    if (file1Data[i] != file2Data[i]) {
                        compareResult = false;
                        break;
                    }
                }
            } else {
                compareResult = false;
            }
        } else if (file1.exists() && !file2.exists()) {
            compareResult = false;
        } else {
            compareResult = file1.exists() || !file2.exists();
        }
        return compareResult;
    }

}
