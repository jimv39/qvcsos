/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.FileMerge;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.ServedProjectProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server utility. Holds some server side utility methods.
 * @author Jim Voris
 */
public final class ServerUtility {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerUtility.class);
    /** Map (keyed by project name) of the cemetery archive directory managers */
    private static final Map<String, ArchiveDirManagerInterface> CEMETERY_ARCHIVE_DIR_MANAGER_MAP = Collections.synchronizedMap(new HashMap<String, ArchiveDirManagerInterface>());
    /** Map (keyed by project name) of the branch archive directory managers */
    private static final Map<String, ArchiveDirManagerInterface> BRANCH_ARCHIVE_DIR_MANAGER_MAP = Collections.synchronizedMap(new HashMap<String, ArchiveDirManagerInterface>());

    /** Hide the constructor. */
    private ServerUtility() {
    }

    static String deduceAppendedPath(File directory, ServedProjectProperties servedProjectProperties) {
        String appendedPath = null;
        String projectBaseDirectory = servedProjectProperties.getArchiveLocation();

        String directoryPath;
        String standardDirectoryPath;
        try {
            directoryPath = directory.getCanonicalPath();
            standardDirectoryPath = Utility.convertToStandardPath(directoryPath);
            if (projectBaseDirectory.length() == standardDirectoryPath.length()) {
                appendedPath = "";
            } else {
                appendedPath = standardDirectoryPath.substring(1 + projectBaseDirectory.length());
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return appendedPath;
    }

    /**
     * Given an appended path, return the parent appended path.
     *
     * @param appendedPath the appended path.
     * @return the parent appended path; null if there is no parent.
     */
    public static String getParentAppendedPath(String appendedPath) {
        String parentAppendedPath = null;
        if (appendedPath.length() > 0) {
            int lastForwardSlashIndex = appendedPath.lastIndexOf("/");
            int lastBackSlashIndex = appendedPath.lastIndexOf("\\");
            int maxIndex;
            if (lastForwardSlashIndex > lastBackSlashIndex) {
                maxIndex = lastForwardSlashIndex;
            } else {
                maxIndex = lastBackSlashIndex;
            }
            if (maxIndex > 0) {
                parentAppendedPath = appendedPath.substring(0, maxIndex);
            } else {
                parentAppendedPath =  "";
            }
        }
        return parentAppendedPath;
    }

    /**
     * Create a merged result buffer.
     * @param commonAncestorRevisionFile
     * @param parentBranchTipRevisionFile
     * @param featureBranchTipRevisionFile
     * @return the merged buffer.
     * @throws IOException for an IO problem.
     */
    public static byte[] createMergedResultBuffer(File commonAncestorRevisionFile, File parentBranchTipRevisionFile, File featureBranchTipRevisionFile)  throws IOException {
        byte[] mergedResultBuffer = null;
        FileInputStream fileInputStream = null;
        try {
            // <editor-fold>
            String[] args = new String[4];
            args[0] = commonAncestorRevisionFile.getCanonicalPath();
            args[1] = parentBranchTipRevisionFile.getCanonicalPath();
            args[2] = featureBranchTipRevisionFile.getCanonicalPath();
            args[3] = File.createTempFile("mergeResult", null).getCanonicalPath();
            // </editor-fold>
            FileMerge fileMerge = new FileMerge(args);
            if (fileMerge.execute()) {
                // <editor-fold>
                File mergedResult = new File(args[3]);
                // </editor-fold>
                fileInputStream = new FileInputStream(mergedResult);
                mergedResultBuffer = new byte[(int) mergedResult.length()];
                fileInputStream.read(mergedResultBuffer);
                mergedResult.delete();
            }
        } catch (QVCSOperationException e) {
            // This gets thrown if the merge cannot be done 'automatically'. We just eat it here, since we don't want to allow the exception
            // to be thrown to our caller... we just want the mergedResult to be null.
            LOGGER.info("Failed to create merged buffer. Merge must be done manually. QVCSOperation exception: [{}]", e.getLocalizedMessage());
        } finally {
            commonAncestorRevisionFile.delete();
            parentBranchTipRevisionFile.delete();
            featureBranchTipRevisionFile.delete();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
        return mergedResultBuffer;
    }

    /**
     * Create a temp file from the buffer. We write the buffer to a temp file that we create. The name of the temp file uses the
     * name parameter to help identify the role that the temp file plays here. We close the file before returning.
     *
     * @param name the prefix name we'll use for the temp file.
     * @param buffer the buffer that gets written to the temp file.
     * @return the File for the temp file that we write to.
     */
    public static File createTempFileFromBuffer(String name, byte[] buffer) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(name, null);
            tempFile.deleteOnExit();
            FileOutputStream outStream = new java.io.FileOutputStream(tempFile);
            outStream.write(buffer);
        } catch (IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
            throw new RuntimeException(ex);
        }
        return tempFile;
    }
}
