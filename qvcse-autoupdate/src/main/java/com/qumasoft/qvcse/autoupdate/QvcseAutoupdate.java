/*
 * Copyright 2023 Jim Voris.
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

package com.qumasoft.qvcse.autoupdate;

import com.qumasoft.qvcslib.UpdateManager;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Jim Voris
 */
public final class QvcseAutoupdate {
    private static final int BUFFER_SIZE = 4096;
    private static QvcseAutoupdate qvcseAutoupdate;
    /** The temporary directory where we store the update. */
    public static final String NEW_CLIENT_DIRECTORY_NAME = "qvcse-new-client";
    /** The client jar file name. */
    public static final String CLIENT_JAR_NAME = "qvcse-gui.jar";
    /** The client shell script file name. */
    public static final String CLIENT_SH_NAME = "client.sh";

    public static void main(String[] args) throws InterruptedException, IOException {
        qvcseAutoupdate = new QvcseAutoupdate(args);
        qvcseAutoupdate.unzipClientZipFile();
        System.out.println("Unzipped new client to " + NEW_CLIENT_DIRECTORY_NAME);
        qvcseAutoupdate.moveFile(UpdateManager.TEMP_DIRECTORY_NAME + File.separator + NEW_CLIENT_DIRECTORY_NAME + File.separator + CLIENT_JAR_NAME, CLIENT_JAR_NAME);
        qvcseAutoupdate.moveFile(UpdateManager.TEMP_DIRECTORY_NAME + File.separator + NEW_CLIENT_DIRECTORY_NAME + File.separator + CLIENT_SH_NAME, CLIENT_SH_NAME);
        qvcseAutoupdate.copyLibJarFiles();
    }

    private QvcseAutoupdate(String[] args) {
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @throws IOException
     */
    private void unzipClientZipFile() throws IOException {
        String zipFilePath = UpdateManager.TEMP_DIRECTORY_NAME + File.separator + UpdateManager.QVCS_CLIENT_ZIP_FILENAME;
        String destDirectory = UpdateManager.TEMP_DIRECTORY_NAME + File.separator + NEW_CLIENT_DIRECTORY_NAME;
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private void moveFile(String sourceFileName, String destinationFileName) throws IOException {
        Path sourcePath = FileSystems.getDefault().getPath(sourceFileName, "");
        Path destinationPath = FileSystems.getDefault().getPath(destinationFileName, "");
        Files.move(sourcePath, destinationPath, REPLACE_EXISTING);
    }

    private void copyLibJarFiles() throws IOException {
        String fromDirectory = UpdateManager.TEMP_DIRECTORY_NAME + File.separator + NEW_CLIENT_DIRECTORY_NAME + File.separator + "lib";
        // We want to move all the .jar files to the right place...
        File fromDir = new File(fromDirectory);
        File[] jarFiles = fromDir.listFiles();
        for (File jarFile : jarFiles) {
            moveFile(fromDirectory + File.separator + jarFile.getName(), "lib" + File.separator + jarFile.getName());
        }
    }
}
