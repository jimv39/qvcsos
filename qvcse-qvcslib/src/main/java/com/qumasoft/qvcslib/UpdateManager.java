/*   Copyright 2023 Jim Voris
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

import com.qumasoft.qvcslib.response.ServerResponseLogin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jim Voris
 */
public final class UpdateManager {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateManager.class);
    // This is a singleton.
    private static final UpdateManager UPDATE_MANAGER = new UpdateManager();

    /** The name of the client zip file. */
    public static final String QVCS_CLIENT_ZIP_FILENAME = "qvcse-client.zip";
    /** The name of the auto-update jar file. */
    public static final String QVCS_AUTO_UPDATE_FILENAME = "qvcse-autoupdate.jar";
    /** The temporary directory where we store the update. */
    public static final String TEMP_DIRECTORY_NAME = "tempDirectoryForClientUpdates";

    /**
     * Creates a new instance of UpdateManager.
     */
    private UpdateManager() {
    }

    /**
     * Get the UpdateManager singleton.
     *
     * @return the UpdateManager singleton.
     */
    public static UpdateManager getInstance() {
        return UPDATE_MANAGER;
    }

    public static void updateClient(ServerResponseLogin loginResponse) {
        if (loginResponse.getClientZip() != null) {
            // Write the new version zip file to a temp directory...
            String clientSideZipFileName = System.getProperty("user.dir")
                    + File.separator
                    + TEMP_DIRECTORY_NAME
                    + File.separator
                    + QVCS_CLIENT_ZIP_FILENAME;
            String autoUpdateFileName = System.getProperty("user.dir")
                    + File.separator
                    + TEMP_DIRECTORY_NAME
                    + File.separator
                    + QVCS_AUTO_UPDATE_FILENAME;
            FileOutputStream zipFileOutputStream = null;
            FileOutputStream autoUpdateFileOutputStream = null;
            try {
                File clientCopyOfNewZipFile = new File(clientSideZipFileName);
                File autoUpdateFileNameFile = new File(autoUpdateFileName);

                // Make sure the needed directories exists
                if (!clientCopyOfNewZipFile.getParentFile().exists()) {
                    clientCopyOfNewZipFile.getParentFile().mkdirs();
                }

                try {
                    zipFileOutputStream = new FileOutputStream(clientCopyOfNewZipFile);
                    Utility.writeDataToStream(loginResponse.getClientZip(), zipFileOutputStream);
                    autoUpdateFileOutputStream = new FileOutputStream(autoUpdateFileNameFile);
                    Utility.writeDataToStream(loginResponse.getAutoUpdateJar(), autoUpdateFileOutputStream);
                } finally {
                    if (zipFileOutputStream != null) {
                        zipFileOutputStream.close();
                    }
                    if (autoUpdateFileOutputStream != null) {
                        autoUpdateFileOutputStream.close();
                    }
                }
                LOGGER.info("Current directory: [{}]", System.getProperty("user.dir"));
                LOGGER.info("Autoupdate command line: [{}{}]", "java -jar ", autoUpdateFileName);
                Runtime.getRuntime().exec("java -jar " + autoUpdateFileName);
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    public static void updateAdminClient(ServerResponseLogin loginResponse) {
    }

    public static byte[] getClientZipAsByteArray() {
        byte[] buffer = null;
        String clientZipFileName = System.getProperty("user.dir") + File.separator + QVCS_CLIENT_ZIP_FILENAME;
        FileInputStream fileInputStream = null;
        try {
            File clientZipFile = new File(clientZipFileName);
            fileInputStream = new FileInputStream(clientZipFile);
            buffer = new byte[(int) clientZipFile.length()];
            Utility.readDataFromStream(buffer, fileInputStream);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }

        return buffer;
    }

    public static byte[] getUpdateJarAsByteArray() {
        byte[] buffer = null;
        String autoUpdateJarFileName = System.getProperty("user.dir") + File.separator + QVCS_AUTO_UPDATE_FILENAME;
        FileInputStream fileInputStream = null;
        try {
            File clientZipFile = new File(autoUpdateJarFileName);
            fileInputStream = new FileInputStream(clientZipFile);
            buffer = new byte[(int) clientZipFile.length()];
            Utility.readDataFromStream(buffer, fileInputStream);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }

        return buffer;
    }
}
