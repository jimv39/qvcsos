/*
 * Copyright 2021-2022 Jim Voris.
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

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public final class DbTestHelper {

    /**
     * Hide the default constructor so it cannot be used.
     */
    private DbTestHelper() {
    }

    /**
     * Create our logger object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DbTestHelper.class);

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

    public static void beginTransaction(ServerResponseFactoryInterface response) {
        ServerTransactionManager.getInstance().clientBeginTransaction(response);
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            LOGGER.warn(null, e);
            throw new QVCSRuntimeException(e.getLocalizedMessage());
        }
    }

    public static void endTransaction(ServerResponseFactoryInterface response) {
        ServerTransactionManager.getInstance().clientEndTransaction(response);
        try {
            if (!ServerTransactionManager.getInstance().transactionIsInProgress(response)) {
                DatabaseManager.getInstance().closeConnection();
            }
        } catch (SQLException e) {
            LOGGER.warn(null, e);
            throw new QVCSRuntimeException(e.getLocalizedMessage());
        }
    }

}
