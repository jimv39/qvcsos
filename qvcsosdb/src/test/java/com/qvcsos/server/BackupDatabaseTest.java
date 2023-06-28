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
package com.qvcsos.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple test to backup the database using the pg_dump tool.
 *
 * @author Jim Voris.
 */
@Ignore
public class BackupDatabaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupDatabaseTest.class);

    /**
     * Backup the database.
     */
    @Test
    public void testBackupDatabase() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        Date now = new Date();
        String dateString = dateFormatter.format(now);
        String host = "localhost";
        String port = "5432";
        String userDir = System.getProperty("user.home");
        try {
            String execString = String.format("pg_dump --file %s/tmp/qvcsos_db_backup-%s --host %s --port %s --username postgres --no-password --verbose --role qvcsos410prod --format=p --encoding UTF8 qvcsos410prod",
                    userDir, dateString, host, port);
            Process p = Runtime.getRuntime().exec(execString);

            try ( BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            }
            p.waitFor();
            LOGGER.info("Backup database process exit value: [{}]", p.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
    }

}
