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

import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Merged information. Instances of this class contain both archive information and workfile information, hence the name mergedInfo. The class name choice pre-dates any thought
 * of merge support, so don't think of this class as having anything to do directly with merge types of operations.
 * @author Jim Voris
 */
public class MergedInfo implements MergedInfoInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
    private static final String[] STATUS_STRINGS = {"Current",
                                                     "Stale",
                                                     "Your copy changed",
                                                     "Merge required",
                                                     "Different",
                                                     "Missing",
                                                     "Not controlled",
                                                     "Invalid"};
    private static final String[] STATUS_SORT_VALUES = {"5", // current
                                                        "0", // stale
                                                        "1", // your copy has changed
                                                        "2", // merge required
                                                        "3", // different
                                                        "4", // missing
                                                        "6", // not controlled
                                                        "7"}; // invalid
    private WorkfileInfoInterface workfileInfo = null;
    private ArchiveInfoInterface archiveInfo = null;
    private ArchiveDirManagerInterface archiveDirManager = null;
    private AbstractProjectProperties projectProperties = null;
    private String userName = null;
    private String mergedInfoKey = null;
    private static Date oldestDate = new Date(0);

    /**
     * Create a new merged info instance.
     * @param workInfo the workfile info.
     * @param archiveDirMgr the containing archive directory manager.
     * @param projProperties the project properties.
     * @param user the user name.
     */
    public MergedInfo(WorkfileInfoInterface workInfo, ArchiveDirManagerInterface archiveDirMgr, AbstractProjectProperties projProperties, String user) {
        workfileInfo = workInfo;
        archiveDirManager = archiveDirMgr;
        projectProperties = projProperties;
        userName = user;
        if (projectProperties.getIgnoreCaseFlag()) {
            mergedInfoKey = workInfo.getShortWorkfileName().toLowerCase();
        } else {
            mergedInfoKey = workInfo.getShortWorkfileName();
        }
    }

    /**
     * Create a new merged info instance.
     * @param archInfo the archive info
     * @param archiveDirMgr the containing archive directory manager.
     * @param projProperties the project properties.
     * @param user the user name.
     */
    public MergedInfo(ArchiveInfoInterface archInfo, ArchiveDirManagerInterface archiveDirMgr, AbstractProjectProperties projProperties, String user) {
        archiveInfo = archInfo;
        archiveDirManager = archiveDirMgr;
        projectProperties = projProperties;
        userName = user;
        if (projectProperties.getIgnoreCaseFlag()) {
            mergedInfoKey = archInfo.getShortWorkfileName().toLowerCase();
        } else {
            mergedInfoKey = archInfo.getShortWorkfileName();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setArchiveInfo(ArchiveInfoInterface archInfo) {
        archiveInfo = archInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveInfoInterface getArchiveInfo() {
        return archiveInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkfileInfo(WorkfileInfoInterface workInfo) {
        workfileInfo = workInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkfileInfoInterface getWorkfileInfo() {
        return workfileInfo;
    }

    private WorkfileInfoInterface getDigestWorkfileInfo() {
        WorkfileInfoInterface digestWorkfileInfo = WorkfileDigestManager.getInstance().getDigestWorkfileInfo(workfileInfo);
        return digestWorkfileInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullWorkfileName() {
        String retVal = null;
        if (workfileInfo != null) {
            retVal = workfileInfo.getFullWorkfileName();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortWorkfileName() {
        String retVal = null;
        if (workfileInfo != null) {
            retVal = workfileInfo.getShortWorkfileName();
        } else if (archiveInfo != null) {
            retVal = archiveInfo.getShortWorkfileName();
        }
        return retVal;
    }

    private byte[] getWorkfileDigest() {
        byte[] retVal = null;
        if (workfileInfo != null && archiveInfo != null) {
            workfileInfo.setArchiveInfo(archiveInfo);
            retVal = WorkfileDigestManager.getInstance().updateWorkfileDigestOnly(workfileInfo, projectProperties);
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getDefaultRevisionDigest() {
        byte[] retVal = null;
        if (archiveInfo != null) {
            retVal = archiveInfo.getDefaultRevisionDigest();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getWorkfileLastChangedDate() {
        Date retVal = null;
        if (workfileInfo != null) {
            retVal = workfileInfo.getWorkfileLastChangedDate();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getWorkfileSize() {
        long retVal = 0;
        if (workfileInfo != null) {
            retVal = workfileInfo.getWorkfileSize();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getWorkfile() {
        File retVal = null;
        if (workfileInfo != null) {
            retVal = workfileInfo.getWorkfile();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFetchedDate() {
        long retVal = 0L;
        if (workfileInfo != null) {
            retVal = workfileInfo.getFetchedDate();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFetchedDate(long time) {
        if (workfileInfo != null) {
            workfileInfo.setFetchedDate(time);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorkfileRevisionString() {
        String retVal = null;
        if (workfileInfo != null) {
            retVal = workfileInfo.getWorkfileRevisionString();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkfileRevisionString(String revisionString) {
        if (workfileInfo != null) {
            workfileInfo.setWorkfileRevisionString(revisionString);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusValue() {
        return STATUS_SORT_VALUES[getStatusIndex()];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatusString() {
        return STATUS_STRINGS[getStatusIndex()];
    }

    /**
     * Get the status string.
     * @return the status string.
     */
    public static String[] getStatusStrings() {
        return STATUS_STRINGS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatusIndex() {
        int retVal = INVALID_STATUS_INDEX;
        if (workfileInfo != null && archiveInfo != null) {
            // This is where I compare the workfile digest with
            // the archive digest value to see if I can figure out if the
            // file is stale.
            if (!workfileInfo.getWorkfileExists()) {
                // Missing.
                retVal = MISSING_STATUS_INDEX;
            } else if (digestsMatch()) {
                // The workfile is current.
                retVal = CURRENT_STATUS_INDEX;

                // If we need to, update the digestWorkfileInfo object to have the
                // default revision string and the current timestamp on the current
                // workfile.
                WorkfileInfoInterface digestWorkfileInfo = getDigestWorkfileInfo();
                if (digestWorkfileInfo.getWorkfileRevisionString() == null) {
                    digestWorkfileInfo.setWorkfileRevisionString(getArchiveInfo().getDefaultRevisionString());
                    digestWorkfileInfo.setFetchedDate(workfileInfo.getWorkfile().lastModified());
                } else if (digestWorkfileInfo.getWorkfileRevisionString().compareTo(getArchiveInfo().getDefaultRevisionString()) != 0) {
                    // This is for the case where a user has checked in changes
                    // via the IDE and that same user is running a copy of the
                    // client application at the same time.
                    digestWorkfileInfo.setWorkfileRevisionString(getArchiveInfo().getDefaultRevisionString());
                    digestWorkfileInfo.setFetchedDate(workfileInfo.getWorkfile().lastModified());
                }
            } else {
                // Digests do not match.  Figure out the status....
                WorkfileInfoInterface digestWorkfileInfo = getDigestWorkfileInfo();

                if (digestWorkfileInfo != null) {
                    workfileInfo.setFetchedDate(digestWorkfileInfo.getFetchedDate());
                    workfileInfo.setWorkfileRevisionString(digestWorkfileInfo.getWorkfileRevisionString());
                }

                // Compare the revision string that we fetched with the revision on
                // the server.
                if (workfileInfo.getWorkfileRevisionString() != null) {
                    if (workfileInfo.getWorkfileRevisionString().equals(archiveInfo.getDefaultRevisionString())) {
                        // User's copy has changed
                        retVal = YOUR_COPY_CHANGED_STATUS_INDEX;
                    } else {
                        // There is a different (newer?) revision on the server...
                        if (workfileInfo.getWorkfile().lastModified() > workfileInfo.getFetchedDate()) {
                            String lockedRevisionString = getLockedRevisionString(getUserName());
                            if (lockedRevisionString != null) {
                                RevisionInformation revisionInformation = getArchiveInfo().getRevisionInformation();
                                int lockedRevisionIndex = revisionInformation.getRevisionIndex(lockedRevisionString);
                                int defaultRevisionIndex = revisionInformation.getRevisionIndex(getArchiveInfo().getDefaultRevisionString());
                                int fetchedRevisionIndex = revisionInformation.getRevisionIndex(workfileInfo.getWorkfileRevisionString());
                                assert (defaultRevisionIndex != fetchedRevisionIndex);
                                if (defaultRevisionIndex == lockedRevisionIndex) {
                                    if (defaultRevisionIndex != fetchedRevisionIndex) {
                                        // defaultRevisionIndex == lockedRevisionIndex &&
                                        // defaultRevisionIndex != fetchedRevisionIndex
                                        // The user locked a file after they made edits to it. A merge is required.
                                        retVal = MERGE_REQUIRED_STATUS_INDEX;
                                    }
                                } else {
                                    // defaultRevisionIndex != lockedRevisionIndex
                                    if (defaultRevisionIndex != fetchedRevisionIndex) {
                                        // defaultRevisionIndex != lockedRevisionIndex &&
                                        // defaultRevisionIndex != fetchedRevisionIndex
                                        if (fetchedRevisionIndex == lockedRevisionIndex) {
                                            // The user has made edits to a non-tip revision which they have locked.
                                            retVal = YOUR_COPY_CHANGED_STATUS_INDEX;
                                        } else {
                                            retVal = MERGE_REQUIRED_STATUS_INDEX;
                                        }
                                    }
                                }
                            } else {
                                // The user made edits, and there are changes on the server too!
                                // Merge required.
                                retVal = MERGE_REQUIRED_STATUS_INDEX;
                            }
                        } else {
                            // Stale.  The user hasn't made any edits since they did the get.
                            retVal = STALE_STATUS_INDEX;
                        }
                    }
                } else {
                    // We can't figure out much more than they are different.
                    retVal = DIFFERENT_STATUS_INDEX;
                }
            }
        } else if (workfileInfo == null && archiveInfo != null) {
            // Missing
            retVal = MISSING_STATUS_INDEX;
        } else if (workfileInfo != null && archiveInfo == null) {
            // Not controlled
            retVal = NOT_CONTROLLED_STATUS_INDEX;
        } else {
            // Invalid
            retVal = INVALID_STATUS_INDEX;
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLockCount() {
        if (archiveInfo != null) {
            return archiveInfo.getLockCount();
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLockedByString() {
        String retVal = "";
        if (archiveInfo != null) {
            String lockedBy = archiveInfo.getLockedByString();
            if (lockedBy != null) {
                retVal = lockedBy;
            }
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getLastCheckInDate() {
        if (archiveInfo != null) {
            return archiveInfo.getLastCheckInDate();
        } else {
            return oldestDate;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorkfileInLocation() {
        if (archiveInfo != null) {
            return archiveInfo.getWorkfileInLocation();
        } else {
            return "";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastEditBy() {
        if (archiveInfo != null) {
            return archiveInfo.getLastEditBy();
        } else {
            return "";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveAttributes getAttributes() {
        ArchiveAttributes retVal = null;
        if (archiveInfo != null) {
            retVal = archiveInfo.getAttributes();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultRevisionString() {
        String retVal = null;
        if (archiveInfo != null) {
            retVal = archiveInfo.getDefaultRevisionString();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLockedRevisionString(String user) {
        String retVal = null;
        if (archiveInfo != null) {
            retVal = archiveInfo.getLockedRevisionString(user);
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getKeywordExpansionAttribute() {
        boolean retVal = false;
        if (archiveInfo != null) {
            retVal = archiveInfo.getAttributes().getIsExpandKeywords();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBinaryFileAttribute() {
        boolean retVal = false;
        if (archiveInfo != null) {
            retVal = archiveInfo.getAttributes().getIsBinaryfile();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogfileInfo getLogfileInfo() {
        LogfileInfo retVal = null;
        if (archiveInfo != null) {
            retVal = archiveInfo.getLogfileInfo();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRevisionDescription(final String revString) {
        String retVal = null;
        if (archiveInfo != null) {
            retVal = archiveInfo.getRevisionDescription(revString);
        }
        return retVal;
    }

    /**
     * Set the keyword expansion attribute for the workfile. This method should not be called here -- and exists to satisfy the interface.
     * @param flag value for keyword expansion attribute.
     */
    @Override
    public void setKeywordExpansionAttribute(boolean flag) {
        throw new java.lang.RuntimeException("Invalid call of setKeywordExpansionAttribute in MergedInfo");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBinaryFileAttribute(boolean flag) {
        throw new java.lang.RuntimeException("Invalid call of setBinaryFileAttribute in MergedInfo");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectName() {
        return getProjectProperties().getProjectName();
    }

    // Return true if the archive digest matches the workfile digest.
    private boolean digestsMatch() {
        boolean retVal = true;
        byte[] workfileDigest = getWorkfileDigest();
        byte[] archiveDigest = getDefaultRevisionDigest();

        if ((workfileDigest != null) && (archiveDigest != null) && (workfileDigest.length == archiveDigest.length)) {
            int count = workfileDigest.length;
            for (int i = 0; i < count; i++) {
                if (workfileDigest[i] != archiveDigest[i]) {
                    LOGGER.log(Level.FINEST, getShortWorkfileName() + ": digest non-match in loop at index " + i);
                    retVal = false;
                    break;
                }
            }
        } else {
            LOGGER.log(Level.FINEST, "digest non-match NO loop");
            retVal = false;
        }
        return retVal;
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getRevision(GetRevisionCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.getRevision(commandLineArgs, fetchToFileName);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getForVisualCompare(GetRevisionCommandArgs commandLineArgs, String outputFileName) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.getForVisualCompare(commandLineArgs, outputFileName);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkOutRevision(CheckOutCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.checkOutRevision(commandLineArgs, fetchToFileName);
        }
        return false;
    }

    /**
     * Check in a revision.
     *
     * @param commandArgs the command arguments.
     * @param checkInFilename the check in file name.
     * @param ignoreLocksToEnableBranchCheckinFlag this flag is ignored on the client side, and exists here only to satisfy the interface signature.
     * @return true if things worked; false otherwise.
     * @throws QVCSException if something goes wrong.
     */
    @Override
    public boolean checkInRevision(CheckInCommandArgs commandArgs, String checkInFilename, boolean ignoreLocksToEnableBranchCheckinFlag) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.checkInRevision(commandArgs, checkInFilename, ignoreLocksToEnableBranchCheckinFlag);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lockRevision(LockRevisionCommandArgs commandArgs) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.lockRevision(commandArgs);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unlockRevision(UnlockRevisionCommandArgs commandArgs) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.unlockRevision(commandArgs);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean breakLock(UnlockRevisionCommandArgs commandArgs) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.breakLock(commandArgs);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean labelRevision(LabelRevisionCommandArgs commandArgs) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.labelRevision(commandArgs);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unLabelRevision(UnLabelRevisionCommandArgs commandArgs) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.unLabelRevision(commandArgs);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setIsObsolete(String user, boolean flag) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.setIsObsolete(user, flag);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsObsolete() {
        if (archiveInfo != null) {
            return archiveInfo.getIsObsolete();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setAttributes(String user, ArchiveAttributes attributes) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.setAttributes(user, attributes);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setCommentPrefix(String user, String commentPrefix) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.setCommentPrefix(user, commentPrefix);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setModuleDescription(String user, String moduleDescription) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.setModuleDescription(user, moduleDescription);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setRevisionDescription(SetRevisionDescriptionCommandArgs commandArgs) throws QVCSException {
        if (archiveInfo != null) {
            return archiveInfo.setRevisionDescription(commandArgs);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsRemote() {
        boolean retVal = false;

        if (archiveInfo != null) {
            if (archiveInfo instanceof LogFileProxy) {
                retVal = true;
            }
        } else {
            if (archiveDirManager instanceof ArchiveDirManagerProxy) {
                retVal = true;
            }
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractProjectProperties getProjectProperties() {
        return projectProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMergedInfoKey() {
        return mergedInfoKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveDirManagerInterface getArchiveDirManager() {
        return archiveDirManager;
    }

    /**
     * Return a buffer that contains the requested revision. This method is synchronous.
     * @param revisionString the revision that should be fetched
     * @return a byte array containing the non-keyword expanded copy of the requested revision, or null if the revision cannot be retrieved.
     *
     */
    @Override
    public byte[] getRevisionAsByteArray(String revisionString) {
        byte[] workfileBuffer = null;
        if (archiveInfo != null) {
            workfileBuffer = archiveInfo.getRevisionAsByteArray(revisionString);
        }
        return workfileBuffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getWorkfileExists() {
        if (workfileInfo != null) {
            return workfileInfo.getWorkfileExists();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRevisionCount() {
        int revisionCount = -1;
        if (archiveInfo != null) {
            revisionCount = archiveInfo.getRevisionCount();
        }
        return revisionCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionInformation getRevisionInformation() {
        RevisionInformation revisionInformation = null;
        if (archiveInfo != null) {
            revisionInformation = archiveInfo.getRevisionInformation();
        }
        return revisionInformation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFileID() {
        int fileID = -1;
        if (archiveInfo != null) {
            fileID = archiveInfo.getFileID();
        }
        return fileID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsOverlap() {
        boolean overlapFlag = false;
        if (archiveInfo != null) {
            overlapFlag = archiveInfo.getIsOverlap();
        }
        return overlapFlag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InfoForMerge getInfoForMerge(String project, String view, String path) {
        InfoForMerge infoForMerge = null;
        if (archiveInfo != null) {
            LogFileProxy logfileProxy = (LogFileProxy) archiveInfo;
            infoForMerge = logfileProxy.getInfoForMerge(project, view, path, getFileID());
        }
        return infoForMerge;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResolveConflictResults resolveConflictFromParentBranch(String project, String branch) {
        ResolveConflictResults resolveConflictResults = null;
        if (archiveInfo != null) {
            LogFileProxy logfileProxy = (LogFileProxy) archiveInfo;
            resolveConflictResults = logfileProxy.resolveConflictFromParentBranch(project, branch, getFileID());
        }
        return resolveConflictResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PromoteFileResults promoteFile(String project, String branch, String parentBranch, FilePromotionInfo filePromoInfo, int id) {
        PromoteFileResults promoteFileResults = null;
        if (archiveInfo != null) {
            LogFileProxy logfileProxy = (LogFileProxy) archiveInfo;
            promoteFileResults = logfileProxy.promoteFile(getUserName(), project, branch, parentBranch, filePromoInfo, id);
        }
        return promoteFileResults;
    }
}
