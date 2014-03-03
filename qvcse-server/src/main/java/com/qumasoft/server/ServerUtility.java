//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.FileMerge;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.ServedProjectProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseGetRevision;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server utility. Holds some server side utility methods.
 * @author Jim Voris
 */
public final class ServerUtility {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
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
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return appendedPath;
    }

    static void setTimestampData(ArchiveInfoInterface logfile, ServerResponseGetRevision serverResponse, Utility.TimestampBehavior timestampBehavior) {
        assert (serverResponse.getRevisionString() != null);
        assert (serverResponse.getRevisionString().length() > 0);

        if (timestampBehavior == Utility.TimestampBehavior.SET_TIMESTAMP_TO_NOW) {
            // Nothing to do here.
            return;
        }

        // Get the revision header for the revision that is getting fetched.
        int revisionIndex = logfile.getLogfileInfo().getRevisionInformation().getRevisionIndex(serverResponse.getRevisionString());
        RevisionHeader revisionHeader = logfile.getLogfileInfo().getRevisionInformation().getRevisionHeader(revisionIndex);
        long timestampTime;

        switch (timestampBehavior) {
            case SET_TIMESTAMP_TO_EDIT_TIME:
                timestampTime = revisionHeader.getEditDate().getTime();
                break;

            default:
            case SET_TIMESTAMP_TO_CHECKIN_TIME:
                timestampTime = revisionHeader.getCheckInDate().getTime();
                break;
        }
        serverResponse.setTimestamp(timestampTime);
    }

    /**
     * Get the cemetery archive directory manager for a given project.
     *
     * @param projectName the name of the project.
     * @param response the response object.
     * @return the archive dir manager for the project's cemetery.
     * @throws QVCSException if we have a problem.
     */
    public static ArchiveDirManagerInterface getCemeteryArchiveDirManager(String projectName, ServerResponseFactoryInterface response) throws QVCSException {
        ArchiveDirManagerInterface cemeteryArchiveDirManager;
        synchronized (CEMETERY_ARCHIVE_DIR_MANAGER_MAP) {
            cemeteryArchiveDirManager = CEMETERY_ARCHIVE_DIR_MANAGER_MAP.get(projectName);
            if (cemeteryArchiveDirManager == null) {
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, QVCSConstants.QVCS_TRUNK_VIEW, QVCSConstants.QVCS_CEMETERY_DIRECTORY);
                cemeteryArchiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, directoryCoordinate,
                        QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
                CEMETERY_ARCHIVE_DIR_MANAGER_MAP.put(projectName, cemeteryArchiveDirManager);
            }
        }
        return cemeteryArchiveDirManager;
    }

    /**
     * Get the branch archive directory manager. This is the directory manager for archive files that exist only on the branch -- i.e. someone has created a new file on a
     * branch, so the archive file does not exist on the trunk. We have to put that archive some where, so it gets created in this directory -- the branch archive directory.
     * @param projectName the project name.
     * @param response a link to the client.
     * @return the branch archive directory manager.
     * @throws QVCSException for QVCS problems.
     */
    public static ArchiveDirManagerInterface getBranchArchiveDirManager(String projectName, ServerResponseFactoryInterface response) throws QVCSException {
        ArchiveDirManagerInterface branchArchiveDirManager;
        synchronized (BRANCH_ARCHIVE_DIR_MANAGER_MAP) {
            branchArchiveDirManager = BRANCH_ARCHIVE_DIR_MANAGER_MAP.get(projectName);
            if (branchArchiveDirManager == null) {
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, QVCSConstants.QVCS_TRUNK_VIEW, QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY);
                branchArchiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, directoryCoordinate,
                        QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
                BRANCH_ARCHIVE_DIR_MANAGER_MAP.put(projectName, branchArchiveDirManager);
            }
        }
        return branchArchiveDirManager;
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

    static String getSortableRevisionStringForChosenLabel(final String labelString, final LogFile logFile) {
        String revisionStringForLabel = null;
        String sortableRevisionStringForLabel = null;

        // This is a 'get by label' request.  Find the label, if we can.
        if ((labelString != null) && (logFile != null)) {
            LabelInfo[] labelInfo = logFile.getLogFileHeaderInfo().getLabelInfo();
            if (labelInfo != null) {
                for (LabelInfo labelInfo1 : labelInfo) {
                    if (labelString.equals(labelInfo1.getLabelString())) {
                        // If it is a floating label, we have to figure out the
                        // revision string...
                        if (labelInfo1.isFloatingLabel()) {
                            RevisionInformation revisionInformation = logFile.getRevisionInformation();
                            int revisionCount = logFile.getRevisionCount();
                            for (int j = 0; j < revisionCount; j++) {
                                RevisionHeader revHeader = revisionInformation.getRevisionHeader(j);
                                if (revHeader.getDepth() == labelInfo1.getDepth()) {
                                    if (revHeader.isTip()) {
                                        String labelRevisionString = labelInfo1.getLabelRevisionString();
                                        String revisionString = revHeader.getRevisionString();
                                        if (revisionString.startsWith(labelRevisionString)) {
                                            revisionStringForLabel = revisionString;
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            revisionStringForLabel = labelInfo1.getLabelRevisionString();
                        }
                        break;
                    }
                }
            }

            if (revisionStringForLabel != null) {
                int revisionIndex = logFile.getRevisionInformation().getRevisionIndex(revisionStringForLabel);
                RevisionHeader revisionHeader = logFile.getRevisionInformation().getRevisionHeader(revisionIndex);
                sortableRevisionStringForLabel = revisionHeader.getRevisionDescriptor().toSortableString();
            }
        }
        return sortableRevisionStringForLabel;
    }

    /**
     * Copy one file to another.
     *
     * @param fromFile the file to copy from.
     * @param toFile the file to copy to.
     * @throws IOException if there is an IO problem.
     */
    public static void copyFile(java.io.File fromFile, java.io.File toFile) throws IOException {
        FileChannel toChannel;
        try (FileChannel fromChannel = new FileInputStream(fromFile).getChannel()) {
            toChannel = new FileOutputStream(toFile).getChannel();
            toChannel.transferFrom(fromChannel, 0L, fromChannel.size());
        }
        toChannel.close();
    }

    /**
     * Create a merged result buffer.
     * @param archiveInfoForTranslucentBranch the archive info for the translucent branch file.
     * @param commonAncestorBuffer the common ancestor buffer.
     * @param branchTipRevisionBuffer the branch tip revision buffer.
     * @param branchParentTipRevisionBuffer the branch parent tip revision buffer.
     * @return the merged buffer.
     * @throws QVCSException for a QVCS problem.
     * @throws IOException for an IO problem.
     */
    public static byte[] createMergedResultBuffer(ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch,
            MutableByteArray commonAncestorBuffer, MutableByteArray branchTipRevisionBuffer, MutableByteArray branchParentTipRevisionBuffer) throws QVCSException, IOException {
        byte[] mergedResultBuffer = null;
        String branchTipRevisionString = archiveInfoForTranslucentBranch.getBranchTipRevisionString();
        String[] branchTipRevisionElements = branchTipRevisionString.split("\\.");
        StringBuilder commonAncestorStringBuffer = new StringBuilder();
        for (int i = 0; i < branchTipRevisionElements.length - 2; i++) {
            if (i > 0) {
                commonAncestorStringBuffer.append(".");
            }
            commonAncestorStringBuffer.append(branchTipRevisionElements[i]);
        }
        String commonAncestorRevisionString = commonAncestorStringBuffer.toString();
        commonAncestorBuffer.setValue(archiveInfoForTranslucentBranch.getCurrentLogFile().getRevisionAsByteArray(commonAncestorRevisionString));
        branchTipRevisionBuffer.setValue(archiveInfoForTranslucentBranch.getCurrentLogFile().getRevisionAsByteArray(archiveInfoForTranslucentBranch.getBranchTipRevisionString()));
        String branchParentTipRevision = findBranchParentTipRevision(archiveInfoForTranslucentBranch, commonAncestorRevisionString);
        branchParentTipRevisionBuffer.setValue(archiveInfoForTranslucentBranch.getCurrentLogFile().getRevisionAsByteArray(branchParentTipRevision));
        File commonAncestorTempFile = createTempFileFromBuffer("commonAncestor", commonAncestorBuffer.getValue());
        File branchTipTempFile = createTempFileFromBuffer("branchTip", branchTipRevisionBuffer.getValue());
        File branchParentTipFile = createTempFileFromBuffer("branchParentTip", branchParentTipRevisionBuffer.getValue());
        FileInputStream fileInputStream = null;
        try {
            // <editor-fold>
            String[] args = new String[4];
            args[0] = commonAncestorTempFile.getCanonicalPath();
            args[1] = branchParentTipFile.getCanonicalPath();
            args[2] = branchTipTempFile.getCanonicalPath();
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
            LOGGER.log(Level.INFO, "Failed to create merged buffer. Merge must be done manually. QVCSOperation exception: " + e.getLocalizedMessage());
        } finally {
            commonAncestorTempFile.delete();
            branchParentTipFile.delete();
            branchTipTempFile.delete();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        return mergedResultBuffer;
    }

    /**
     * Find the branch parent's tip revision. If the branch's parent branch is the trunk, walk toward a lower index to find the
     * parent branch's tip revision. If the branch's parent branch is another branch, it's stored as a forward delta, so the tip
     * revision will be at a higher index.
     *
     * @param archiveInfoForTranslucentBranch archive info for the translucent branch.
     * @param commonAncestorRevisionString the common ancestor revision string.
     * @return the revision string of the parent branch's tip revision.
     * @throws QVCSException if we can't find the parent branch's tip revision.
     */
    private static String findBranchParentTipRevision(ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch, String commonAncestorRevisionString) throws QVCSException {
        String branchParentTipRevisionString = null;
        LogFile logfile = archiveInfoForTranslucentBranch.getCurrentLogFile();
        int commonAncestorRevisionIndex = logfile.getRevisionInformation().getRevisionIndex(commonAncestorRevisionString);
        RevisionHeader commonAncestorRevisionHeader = logfile.getRevisionInformation().getRevisionHeader(commonAncestorRevisionIndex);
        int indexIncrement = -1;
        if (commonAncestorRevisionHeader.getDepth() > 0) {
            indexIncrement = 1;
        }
        int index = commonAncestorRevisionIndex;
        while (index >= 0) {
            RevisionHeader revisionHeader = logfile.getRevisionInformation().getRevisionHeader(index);
            if (revisionHeader != null) {
                if (revisionHeader.getDepth() == commonAncestorRevisionHeader.getDepth()) {
                    if (revisionHeader.isTip()) {
                        branchParentTipRevisionString = revisionHeader.getRevisionString();
                        break;
                    }
                }
            } else {
                throw new QVCSException("Failed to find branch parent tip revision");
            }
            // Handle the increment this way so that this method will work for both trunk based branches, and
            // for branches where the parent branch is another branch.
            index += indexIncrement;
        }
        return branchParentTipRevisionString;
    }

    /**
     * Create a temp file from the buffer. We write the buffer to a temp file that we create. The name of the temp file uses the
     * name parameter to help identify the role that the temp file plays here. We close the file before returning.
     *
     * @param name the prefix name we'll use for the temp file.
     * @param buffer the buffer that gets written to the temp file.
     * @return the File for the temp file that we write to.
     * @throws IOException if there is an IO problem.
     */
    private static File createTempFileFromBuffer(String name, byte[] buffer) throws IOException {
        FileOutputStream outStream = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile(name, null);
            tempFile.deleteOnExit();
            outStream = new java.io.FileOutputStream(tempFile);
            outStream.write(buffer);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        return tempFile;
    }
}
