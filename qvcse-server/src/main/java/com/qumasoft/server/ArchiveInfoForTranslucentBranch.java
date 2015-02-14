/*   Copyright 2004-2015 Jim Voris
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

import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeader;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.LogfileListenerInterface;
import com.qumasoft.qvcslib.MajorMinorRevisionPair;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.RevisionDescriptor;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import com.qumasoft.qvcslib.logfileaction.SetAttributes;
import com.qumasoft.server.dataaccess.PromotionCandidateDAO;
import com.qumasoft.server.dataaccess.impl.PromotionCandidateDAOImpl;
import com.qumasoft.server.datamodel.PromotionCandidate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive info for translucent branch.
 * @author Jim Voris
 */
public final class ArchiveInfoForTranslucentBranch implements ArchiveInfoInterface, LogFileInterface, LogfileListenerInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveInfoForTranslucentBranch.class);
    private String shortWorkfileName;
    private Date dateOfLastCheckIn;
    private String lastEditBy;
    private String defaultRevisionString;
    private byte[] defaultRevisionDigest;
    private final ArchiveAttributes archiveAttributes;
    private LogfileInfo logfileInfo;
    private int logFileFileID = -1;
    private final String projectName;
    private final String viewName;
    private final String branchLabel;
    private int revisionCount = -1;
    private List<LogfileListenerInterface> logfileListeners;
    private String fullLogfileName;
    private final ProjectView projectView;
    private String workfileInLocation;
    private Map<String, RevisionHeader> revisionHeaderMap;
    private boolean isOverlapFlag;

    /**
     * Creates a new instance of ArchiveInfoForReadOnlyDateBasedView.
     * @param shortName the short workfile name.
     * @param logFile the LogFile instance from which we build the archive info for the translucent branch.
     * @param remoteViewProperties the project properties.
     */
    ArchiveInfoForTranslucentBranch(String shortName, LogFile logFile, RemoteViewProperties remoteViewProperties) {
        this.shortWorkfileName = shortName;
        this.projectName = remoteViewProperties.getProjectName();
        this.viewName = remoteViewProperties.getViewName();
        this.projectView = ViewManager.getInstance().getView(this.projectName, this.viewName);
        this.branchLabel = this.projectView.getTranslucentBranchLabel();
        this.logfileInfo = buildLogfileInfo(logFile);
        this.defaultRevisionDigest = ArchiveDigestManager.getInstance().getArchiveDigest(logFile, getDefaultRevisionString());
        this.archiveAttributes = new ArchiveAttributes(logFile.getAttributes());
        // No lock checking on  a translucent branch.
        this.archiveAttributes.setIsCheckLock(false);
        this.logFileFileID = logFile.getFileID();
    }

    /**
     * Get the short workfile name.
     *
     * @return the short workfile name.
     */
    @Override
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     *
     * @param newShortWorkfileName the new short workfile name.
     */
    public void setShortWorkfileName(String newShortWorkfileName) {
        this.shortWorkfileName = newShortWorkfileName;
    }

    /**
     * Return the lock count.
     *
     * @return the lock count.
     */
    @Override
    public int getLockCount() {
        return 0;
    }

    /**
     * Get the locked by string. Since we don't support locks on a translucent branch, this is always null;
     *
     * @return an empty string.
     */
    @Override
    public String getLockedByString() {
        return "";
    }

    /**
     * Get the last checkin date.
     *
     * @return the last checkin date.
     */
    @Override
    public synchronized Date getLastCheckInDate() {
        return dateOfLastCheckIn;
    }

    /**
     * Get the workfile in location. We figure this out when we build object... see the deduceWorkfileLocation method.
     *
     * @return the workfile in location.
     */
    @Override
    public synchronized String getWorkfileInLocation() {
        return this.workfileInLocation;
    }

    /**
     * Get the last edit by string.
     *
     * @return the last edit by string.
     */
    @Override
    public synchronized String getLastEditBy() {
        return this.lastEditBy;
    }

    /**
     * Get the default revision digest.
     *
     * @return the default revision digest.
     */
    @Override
    public synchronized byte[] getDefaultRevisionDigest() {
        return this.defaultRevisionDigest;
    }

    /**
     * Get the attributes.
     *
     * @return the attributes.
     */
    @Override
    public ArchiveAttributes getAttributes() {
        return this.archiveAttributes;
    }

    /**
     * Get the logfile info.
     *
     * @return the logfile info.
     */
    @Override
    public synchronized LogfileInfo getLogfileInfo() {
        return this.logfileInfo;
    }

    /**
     * Get the revision description for the given revision string.
     *
     * @param revisionString the revision string of the revision we are interested in.
     * @return that revision's revision description.
     */
    @Override
    public synchronized String getRevisionDescription(final String revisionString) {
        String returnValue = null;
        try {
            RevisionHeader revisionHeader = this.revisionHeaderMap.get(revisionString);
            if (revisionString != null) {
                returnValue = revisionHeader.getRevisionDescription();
            }
        } catch (ClassCastException | NullPointerException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnValue;
    }

    /**
     * Get a given revision as a byte array.
     *
     * @param revisionString the revision to fetch.
     * @return an in memory byte[] that is the non-keyword expanded bytes for the given revision.
     */
    @Override
    public byte[] getRevisionAsByteArray(String revisionString) {
        byte[] retVal = null;
        try {
            retVal = getCurrentLogFile().getRevisionAsByteArray(revisionString);
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return retVal;
    }

    /**
     * Get the locked revision string for the given user.
     *
     * @param userName the user.
     * @return null, since locks are not allowed on translucent branches.
     */
    @Override
    public String getLockedRevisionString(String userName) {
        return null;
    }

    /**
     * Get the default revision string.
     *
     * @return the default revision string.
     */
    @Override
    public synchronized String getDefaultRevisionString() {
        return this.defaultRevisionString;
    }

    /**
     * Get the requested revision to the requested file.
     *
     * @param commandLineArgs the command arguments that identify what revision to get.
     * @param fetchToFileName the file that the revision is written to.
     * @return true if things work as expected.
     * @throws QVCSException if something goes wrong.
     */
    @Override
    public boolean getRevision(GetRevisionCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        boolean returnValue = false;
        try {
            // We need to fill in the revision string.
            if ((commandLineArgs.getRevisionString() != null) && (commandLineArgs.getRevisionString().length() > 0)) {
                if (0 == commandLineArgs.getRevisionString().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
                    commandLineArgs.setRevisionString(getDefaultRevisionString());
                }
            }
            returnValue = getCurrentLogFile().getRevision(commandLineArgs, fetchToFileName);
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnValue;
    }

    /**
     * Get for visual compare.
     *
     * @param commandLineArgs command arguments to identify what revision to fetch.
     * @param outputFileName where the revision is fetched to.
     * @return true if things work as expected.
     * @throws QVCSException if something goes wrong.
     */
    @Override
    public boolean getForVisualCompare(GetRevisionCommandArgs commandLineArgs, String outputFileName) throws QVCSException {
        boolean returnValue = false;
        try {
            // We need to fill in the revision string.
            if ((commandLineArgs.getRevisionString() != null) && (commandLineArgs.getRevisionString().length() > 0)) {
                if (0 == commandLineArgs.getRevisionString().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION)) {
                    commandLineArgs.setRevisionString(getDefaultRevisionString());
                }
            }
            returnValue = getCurrentLogFile().getForVisualCompare(commandLineArgs, outputFileName);
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnValue;
    }

    /**
     * Check out a revision. This is not allowed on a translucent branch.
     *
     * @param commandLineArgs the command line arguments.
     * @param fetchToFileName the file to fetch to (not used).
     * @return false.
     * @throws QVCSException to satisfy the interface signature.
     */
    @Override
    public boolean checkOutRevision(CheckOutCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        // Check outs are not allowed on translucent branches.
        return false;
    }

    /**
     * Check in a revision.
     *
     * @param commandArgs the command arguments.
     * @param checkInFilename the local file containing the new revision.
     * @param ignoreLocksToEnableBranchCheckInFlag flag (that we ignore because we always set it to true).
     * @return true if things work as expected.
     * @throws QVCSException if something goes wrong.
     */
    @Override
    public boolean checkInRevision(CheckInCommandArgs commandArgs, String checkInFilename, boolean ignoreLocksToEnableBranchCheckInFlag) throws QVCSException {
        boolean retFlag;
        boolean firstRevisionOnBranchFlag = false;
        commandArgs.setApplyLabelFlag(true);
        commandArgs.setLabel(getProjectView().getTranslucentBranchLabel());
        commandArgs.setLockedRevisionString(getDefaultRevisionString());
        LogFile logfile = getCurrentLogFile();

        if (logfile.getLogfileInfo().getLogFileHeaderInfo().hasLabel(getBranchLabel())) {
            commandArgs.setForceBranchFlag(false);
            commandArgs.setReuseLabelFlag(true);
        } else {
            commandArgs.setForceBranchFlag(true);
            firstRevisionOnBranchFlag = true;
        }
        retFlag = logfile.checkInRevision(commandArgs, checkInFilename, true);

        // If things worked, and this is the first time we've checked in on this branch,
        // record the record in the promotion candidate table.
        if (retFlag && firstRevisionOnBranchFlag) {
            capturePromotionCandidate();
        }
        return retFlag;
    }

    void capturePromotionCandidate() throws QVCSException {
        Integer projectId = DatabaseCache.getInstance().getProjectId(getProjectName());
        Integer branchId = DatabaseCache.getInstance().getBranchId(projectId, getViewName());
        PromotionCandidate promotionCandidate = new PromotionCandidate(getFileID(), branchId);
        PromotionCandidateDAO promotionCandidateDAO = new PromotionCandidateDAOImpl();
        try {
            promotionCandidateDAO.insertIfMissing(promotionCandidate);
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new QVCSException("Failed to create promotion candidate record for [" + getShortWorkfileName() + "]");
        }
    }

    /**
     * Lock a revision. Locks are not allowed on a translucent branch.
     *
     * @param commandArgs command line arguments.
     * @return false.
     * @throws QVCSException to satisfy the interface signature.
     */
    @Override
    public boolean lockRevision(LockRevisionCommandArgs commandArgs) throws QVCSException {
        // Locks are not allowed on a translucent branch.
        return false;
    }

    /**
     * Do the work needed to delete an archive. At the logfile level, this involves creating a new revision on the file branch
     * associated with the translucent branch
     *
     * @param userName the user name.
     * @param appendedPath the current appended path.
     * @param shortName the short workfile name.
     * @param date the date of this transaction. This should be taken from the enclosing transaction.
     * @return true if the work was completed successfully.
     * @throws QVCSException if something goes wrong.
     */
    public boolean deleteArchive(String userName, String appendedPath, String shortName, final Date date) throws QVCSException {
        boolean retVal;
        boolean firstRevisionOnBranchFlag = false;
        String branchTipRevisionString = getBranchTipRevisionString();
        if (branchTipRevisionString.length() > 0) {
            LogFile logfile = getCurrentLogFile();
            if (!logfile.getLogfileInfo().getLogFileHeaderInfo().hasLabel(getBranchLabel())) {
                firstRevisionOnBranchFlag = true;
            }

            if (logfile.deleteArchiveOnTranslucentBranch(userName, appendedPath, shortName, date, getBranchLabel(), this)) {
                retVal = true;
                if (firstRevisionOnBranchFlag) {
                    capturePromotionCandidate();
                }
            } else {
                retVal = false;
            }
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Resolve the conflict from the parent branch. <i>All</i> we do here is remove the branch label from the archive file. The
     * other work for resolving the conflict is performed elsewhere. The <i>only</i> responsibility that this class has is to make
     * those changes to the archive file that are required for this operation. Period.
     *
     * @param userName the user name.
     * @param date the transaction timestamp.
     * @return true if things work as expected.
     * @throws QVCSException if something goes wrong.
     */
    public boolean resolveConflictFromParentBranch(String userName, final Date date) throws QVCSException {
        boolean retVal = false;
        LogFile logfile = getCurrentLogFile();
        if (logfile.hasLabel(getBranchLabel())) {
            UnLabelRevisionCommandArgs commandArgs = new UnLabelRevisionCommandArgs();
            commandArgs.setShortWorkfileName(getShortWorkfileName());
            commandArgs.setUserName(userName);
            commandArgs.setLabelString(getBranchLabel());
            retVal = logfile.unLabelRevision(commandArgs);
        } else {
            throw new QVCSException("Invalid attempt to resolve conflict from parent branch. Missing translucent branch label: [" + getBranchLabel() + "] for file: ["
                    + getShortWorkfileName() + "]");
        }
        return retVal;
    }

    /**
     * Promote the file. <i>All</i> we do here is remove the branch label from the archive file. The other work for promoting the
     * file is performed elsewhere. The <i>only</i> responsibility that this class has is to make those changes to the archive file
     * that are required for this operation. Period.
     *
     * @param userName the user name.
     * @param date the transaction timestamp.
     * @return true if things work as expected.
     * @throws QVCSException if something goes wrong.
     */
    public boolean promoteFile(String userName, final Date date) throws QVCSException {
        boolean retVal = false;
        LogFile logfile = getCurrentLogFile();
        if (logfile.hasLabel(getBranchLabel())) {
            UnLabelRevisionCommandArgs commandArgs = new UnLabelRevisionCommandArgs();
            commandArgs.setShortWorkfileName(getShortWorkfileName());
            commandArgs.setUserName(userName);
            commandArgs.setLabelString(getBranchLabel());
            retVal = logfile.unLabelRevision(commandArgs);
        } else {
            throw new QVCSException("Invalid attempt to promote a file. Missing translucent branch label: [" + getBranchLabel() + "] for file: [" + getShortWorkfileName() + "]");
        }
        return retVal;
    }

    /**
     * Do the work needed for a move. At the logfile level, this involves creating a new revision on the file branch associated with
     * the translucent branch
     *
     * @param userName the user name.
     * @param appendedPath the current appended path.
     * @param targetArchiveDirManagerInterface the destination directory (this should be an instance of a translucent branch
     * directory manager).
     * @param shortName the short workfile name.
     * @param date the date of this transaction. This should be taken from the enclosing transaction.
     * @return true if the work was completed successfully.
     * @throws QVCSException if something goes wrong.
     */
    public boolean moveArchive(String userName, String appendedPath, ArchiveDirManagerInterface targetArchiveDirManagerInterface, String shortName,
            final Date date) throws QVCSException {
        boolean retVal;
        boolean firstRevisionOnBranchFlag = false;
        String branchTipRevisionString = getBranchTipRevisionString();
        if (branchTipRevisionString.length() > 0) {
            LogFile logfile = getCurrentLogFile();
            if (!logfile.getLogfileInfo().getLogFileHeaderInfo().hasLabel(getBranchLabel())) {
                firstRevisionOnBranchFlag = true;
            }
            if (targetArchiveDirManagerInterface instanceof ArchiveDirManagerForTranslucentBranch) {
                ArchiveDirManagerForTranslucentBranch targetArchiveDirManager = (ArchiveDirManagerForTranslucentBranch) targetArchiveDirManagerInterface;

                if (logfile.moveArchiveOnTranslucentBranch(userName, appendedPath, targetArchiveDirManager, shortName, date, getBranchLabel(), this)) {
                    retVal = true;
                    if (firstRevisionOnBranchFlag) {
                        capturePromotionCandidate();
                    }
                } else {
                    retVal = false;
                }
            } else {
                retVal = false;
            }
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Do the work needed for a move. At the logfile level, this involves creating a new revision on the file branch associated with
     * the translucent branch
     *
     * @param userName the user name.
     * @param appendedPath the current appended path.
     * @param oldShortWorkfileName the current short workfile name.
     * @param newShortWorkfileName the new short workfile name.
     * @param date the date of this transaction. This should be taken from the enclosing transaction.
     * @return true if the work was completed successfully.
     * @throws QVCSException if something goes wrong.
     */
    public boolean renameArchive(String userName, String appendedPath, String oldShortWorkfileName, String newShortWorkfileName, final Date date) throws QVCSException {
        boolean retVal;
        boolean firstRevisionOnBranchFlag = false;
        String branchTipRevisionString = getBranchTipRevisionString();
        if (branchTipRevisionString.length() > 0) {
            LogFile logfile = getCurrentLogFile();
            if (!logfile.getLogfileInfo().getLogFileHeaderInfo().hasLabel(getBranchLabel())) {
                firstRevisionOnBranchFlag = true;
            }

            if (logfile.renameArchiveOnTranslucentBranch(userName, appendedPath, oldShortWorkfileName, newShortWorkfileName, date, getBranchLabel(), this)) {
                retVal = true;
                if (firstRevisionOnBranchFlag) {
                    capturePromotionCandidate();
                }
            } else {
                retVal = false;
            }
        } else {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public boolean unlockRevision(UnlockRevisionCommandArgs commandArgs) throws QVCSException {
        return false;
    }

    @Override
    public boolean breakLock(UnlockRevisionCommandArgs commandArgs) throws QVCSException {
        throw new QVCSException("Unexpected call to breakLock method.");
    }

    @Override
    public boolean labelRevision(LabelRevisionCommandArgs commandArgs) throws QVCSException {
        boolean retVal = false;

        // A label should only be allowed to be applied to the tip of the branch.
        String labelRevisionString = getBranchTipRevisionString();
        if (labelRevisionString.length() > 0) {
            commandArgs.setRevisionFlag(true);
            commandArgs.setRevisionString(labelRevisionString);
            commandArgs.setReuseLabelFlag(true);
            commandArgs.setFloatingFlag(false);
            commandArgs.setDuplicateFlag(false);

            LogFile logfile = getCurrentLogFile();

            if (logfile.labelRevision(commandArgs)) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public boolean unLabelRevision(UnLabelRevisionCommandArgs commandArgs) throws QVCSException {
        LogFile logfile = getCurrentLogFile();
        return logfile.unLabelRevision(commandArgs);
    }

    @Override
    public boolean getIsObsolete() {
        return false;
    }

    @Override
    public boolean setIsObsolete(String userName, boolean flag) throws QVCSException {
        return false;
    }

    @Override
    public boolean setAttributes(String userName, ArchiveAttributes attributes) throws QVCSException {
        return false;
    }

    @Override
    public boolean setCommentPrefix(String userName, String commentPrefix) throws QVCSException {
        return false;
    }

    @Override
    public boolean setModuleDescription(String userName, String moduleDescription) throws QVCSException {
        return false;
    }

    @Override
    public boolean setRevisionDescription(SetRevisionDescriptionCommandArgs commandArgs) throws QVCSException {
        LogFile logfile = getCurrentLogFile();
        return logfile.setRevisionDescription(commandArgs);
    }

    /**
     * Figure out the tip revision for this branch.
     *
     * If this file has been branched already for this translucent branch, then the tip will be labeled with the branch's label. If,
     * however, the file has not yet been branched for this branch, then we need to figure out the branch's <i>parent</i> tip
     * revision and return that, since for translucent branches, the parent branch's tip is the same as our tip (unless we have
     * branched from the parent).
     *
     * @param logFile the trunk's archive file.
     * @return the revision header for this branch's tip revision.
     */
    private int deduceBranchTipRevisionIndex(LogFile logFile, ProjectView view) throws QVCSException {
        int branchTipRevisionIndex = -1;
        String parentBranchName = view.getRemoteViewProperties().getBranchParent();
        if (logFile.hasLabel(getBranchLabel())) {
            String revisionStringForLabel = getRevisionStringFromLabel(getBranchLabel(), logFile);
            int localRevisionCount = logFile.getRevisionCount();
            RevisionInformation revisionInformation = logFile.getRevisionInformation();
            for (int i = 0; i < localRevisionCount; i++) {
                if (0 == revisionInformation.getRevisionHeader(i).getRevisionString().compareTo(revisionStringForLabel)) {
                    branchTipRevisionIndex = i;
                    break;
                }
            }
        } else {
            if (0 == parentBranchName.compareTo(QVCSConstants.QVCS_TRUNK_VIEW)) {
                branchTipRevisionIndex = 0;
            } else {
                ProjectView parentView = ViewManager.getInstance().getView(getProjectName(), parentBranchName);
                branchTipRevisionIndex = deduceBranchTipRevisionIndex(logFile, parentView);
            }
        }
        return branchTipRevisionIndex;
    }

    /**
     * Figure out the default depth. For a logfile that hasn't branched (file level branch) for this translucent (project level)
     * branch, then the depth will be 0; for others, the depth can be deduced based on the default revision string (which
     * <b>MUST</b> have been figured out before this method is called).
     *
     * @return the default depth for this archive info.
     */
    private int deduceDefaultDepth() {
        // We should just be able to count the number of revision segments minus 2.
        String localDefaultRevisionString = getDefaultRevisionString();
        String[] revisionStringSegments = localDefaultRevisionString.split("\\.");
        int depth = (revisionStringSegments.length - 2) / 2;
        return depth;
    }

    /**
     * If there is a lock on the trunk's tip revision, and the branch is still viewing the trunk's tip revision as the branch's tip
     * revision then we can display the trunk's "workfile in" value. Alternately, if we are branched then there is no workfile
     * location since we do not support locks on a translucent branch.
     *
     * @param logFile the trunk's archive file.
     * @return a string that shows where the workfile is checked out.
     */
    private String deduceWorkfileInLocation(LogFile logFile) {
        String localWorkfileInLocation = "";
        if (0 == logFile.getDefaultRevisionString().compareTo(getDefaultRevisionString())) {
            if (logFile.getLockCount() > 0) {
                localWorkfileInLocation = logFile.getWorkfileInLocation();
            }
        }
        return localWorkfileInLocation;
    }

    private String getProjectName() {
        return this.projectName;
    }

    private String getViewName() {
        return this.viewName;
    }

    @Override
    public synchronized String getFullArchiveFilename() {
        return this.fullLogfileName;
    }

    @Override
    public int getFileID() {
        return this.logFileFileID;
    }

    /**
     * Get the LogFile instance associated with this translucent archive info.
     * @return the LogFile instance associated with this translucent archive info.
     * @throws QVCSException if there are problems finding the associated LogFile.
     */
    public LogFile getCurrentLogFile() throws QVCSException {
        FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(getProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, this.logFileFileID);
        int directoryID = fileIDInfo.getDirectoryID();

        // Lookup the archiveDirManager for the file's current location...
        ArchiveDirManager archiveDirManager = DirectoryIDDictionary.getInstance().lookupArchiveDirManager(getProjectName(), directoryID, null, true);

        String keyToFile = fileIDInfo.getShortFilename();
        boolean ignoreCaseFlag = archiveDirManager.getProjectProperties().getIgnoreCaseFlag();
        if (ignoreCaseFlag) {
            keyToFile = keyToFile.toLowerCase();
        }

        // Get the file's current archiveInfo...
        return (LogFile) archiveDirManager.getArchiveInfo(keyToFile);
    }

    private synchronized LogfileInfo buildLogfileInfo(LogFile logFile) {
        LogfileInfo localLogfileInfo = null;
        this.fullLogfileName = logFile.getFullArchiveFilename();
        RevisionInformation branchRevisionInformation = buildBranchRevisionInformation(logFile);
        if (branchRevisionInformation != null) {
            this.defaultRevisionString = getBranchTipRevisionString(logFile);
            LogFileHeaderInfo branchLogFileHeaderInfo = buildBranchLogFileHeaderInfo(logFile, branchRevisionInformation);
            localLogfileInfo = new LogfileInfo(branchLogFileHeaderInfo, branchRevisionInformation, logFile.getFileID(), this.fullLogfileName);

            dateOfLastCheckIn = branchRevisionInformation.getRevisionHeader(0).getCheckInDate();

            // Figure out what we should display for workfile in location...
            this.workfileInLocation = deduceWorkfileInLocation(logFile);

            // Figure out who made the last edit.
            int lastEditByIndex = branchRevisionInformation.getRevisionHeader(0).getCreatorIndex();
            AccessList modifierList = branchRevisionInformation.getModifierList();
            this.lastEditBy = modifierList.indexToUser(lastEditByIndex);
        }
        return localLogfileInfo;
    }

    private synchronized RevisionInformation buildBranchRevisionInformation(LogFile logFile) {
        RevisionInformation branchRevisionInformation = null;
        try {
            // Need to create the set of revisions that are visible for this view...
            int branchTipRevisionIndex = deduceBranchTipRevisionIndex(logFile, getProjectView());
            RevisionInformation logFileRevisionInformation = logFile.getRevisionInformation();
            assert (branchTipRevisionIndex >= 0);
            isOverlapFlag = false;
            int[] branchIndexes = deduceBranchRevisions(branchTipRevisionIndex, logFile.getLogfileInfo());
            AccessList accessList = new AccessList(logFile.getLogFileHeaderInfo().getAccessList());
            branchRevisionInformation = new RevisionInformation(branchIndexes.length, accessList, accessList);
            this.revisionHeaderMap = new TreeMap<>();
            for (int i = 0; i < branchIndexes.length; i++) {
                RevisionHeader branchRevisionHeader = new RevisionHeader(logFileRevisionInformation.getRevisionHeader(branchIndexes[i]));
                // We do not allow locks on the branch.
                branchRevisionHeader.setIsLocked(false);
                branchRevisionInformation.updateRevision(i, branchRevisionHeader);
                this.revisionHeaderMap.put(branchRevisionHeader.getRevisionString(), branchRevisionHeader);
            }
            this.revisionCount = this.revisionHeaderMap.size();
        } catch (QVCSException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return branchRevisionInformation;
    }

    /**
     * This method builds an ordered list of revision indexes that are on this branch and figures out whether we have overlap.
     *
     * @param branchTipRevisionIndex The index of the branch's tip revision.
     * @param info the logfile info of the logfile.
     * @return an ordered array of the revision indexes that appear on this branch.
     * @throws java.io.IOException
     */
    private synchronized int[] deduceBranchRevisions(int branchTipRevisionIndex, LogfileInfo info) {
        RevisionInformation revisionInformation = info.getRevisionInformation();
        RevisionHeader branchTipRevisionHeader = revisionInformation.getRevisionHeader(branchTipRevisionIndex);
        assert (branchTipRevisionHeader.isTip());
        int localRevisionCount = info.getLogFileHeaderInfo().getRevisionCount();
        Map<RevisionDescriptor, Integer> revisions = Collections.synchronizedMap(new TreeMap<RevisionDescriptor, Integer>());

        // The idea is to walk the set of revisions looking for those that belong on this branch.
        // given that the branch tip revision is the one passed in.
        if (branchTipRevisionHeader.getDepth() == 0) {
            // We are dealing with the trunk tip revision.  This is the easy case.
            for (int i = branchTipRevisionIndex; i < localRevisionCount; i++) {
                RevisionHeader currentRevision = revisionInformation.getRevisionHeader(i);
                if (currentRevision.getDepth() == 0) {
                    revisions.put(currentRevision.getRevisionDescriptor(), i);
                }
            }
        } else {
            // For branch revisions, we need to look through all revisions,
            // since some of the revisions we show may be located at a lower
            // revision index than the one we are expanding the log for.
            for (int i = 0; i < localRevisionCount; i++) {
                RevisionHeader currentRevision = revisionInformation.getRevisionHeader(i);
                if (isAncestor(currentRevision, branchTipRevisionHeader)) {
                    revisions.put(currentRevision.getRevisionDescriptor(), i);
                } else {
                    // A non ancestor.... if any non-ancestor exists, and has a lower
                    // revision index than the index of the branchTipRevisionIndex, then
                    // we have overlap -- meaning that there have been edits to the file
                    // after we the branch point for this translucent branch.
                    if (i < branchTipRevisionIndex) {
                        isOverlapFlag = true;
                    }
                }
            }
        }

        // Bundle the result in a nice package.
        int[] returnedIndexes = new int[revisions.size()];
        int maxIndex = revisions.size() - 1;
        Iterator iterator = revisions.values().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            Integer integerIndex = (Integer) iterator.next();
            returnedIndexes[maxIndex - i] = integerIndex;
        }
        return returnedIndexes;
    }

    /**
     * (Cloned from QVCSKeywordManager). Determine whether a revision is an ancestor of the descendant revision.
     *
     * @param ancestorCandidate the prospective ancestor.
     * @param descendantRevision the descendant revision.
     * @return true if the prospective ancestor <i>is</i> an ancestor of the descendant.
     */
    private boolean isAncestor(RevisionHeader ancestorCandidate, RevisionHeader descendantRevision) {
        boolean retVal;

        if (ancestorCandidate.getDepth() > descendantRevision.getDepth()) {
            // There is no way for a revision that is deeper than the decendant
            // to be an ancestor of that decendant.
            retVal = false;
        } else if (ancestorCandidate.getDepth() == descendantRevision.getDepth()) {
            // If the depths are equal, then all elements except the final minor
            // number must match; the final minor number must be less than the
            // minor number of the decendant.
            int i;
            retVal = true;
            for (i = 0; retVal && (i < ancestorCandidate.getDepth()); i++) {
                MajorMinorRevisionPair ancestorPair = ancestorCandidate.getRevisionDescriptor().getRevisionPairs()[i];
                MajorMinorRevisionPair decendantPair = descendantRevision.getRevisionDescriptor().getRevisionPairs()[i];
                if (ancestorPair.getMajorNumber() == decendantPair.getMajorNumber()
                        && ancestorPair.getMinorNumber() == decendantPair.getMinorNumber()) {
                    continue;
                } else {
                    retVal = false;
                }
            }

            // Look at the last pair...
            if (retVal) {
                MajorMinorRevisionPair ancestorPair = ancestorCandidate.getRevisionDescriptor().getRevisionPairs()[i];
                MajorMinorRevisionPair decendantPair = descendantRevision.getRevisionDescriptor().getRevisionPairs()[i];
                if (ancestorPair.getMajorNumber() == decendantPair.getMajorNumber()) {
                    if (ancestorPair.getMinorNumber() > decendantPair.getMinorNumber()) {
                        retVal = false;
                    }
                } else {
                    retVal = false;
                }
            }
        } else {
            // If the candidate depth is less than the decendant, then
            // all the major/minor pairs of the candidate must match the
            // decendant's.
            int i;
            retVal = true;
            for (i = ancestorCandidate.getDepth(); retVal && (i > 0); i--) {
                MajorMinorRevisionPair ancestorPair = ancestorCandidate.getRevisionDescriptor().getRevisionPairs()[i];
                MajorMinorRevisionPair decendantPair = descendantRevision.getRevisionDescriptor().getRevisionPairs()[i];
                if (ancestorPair.getMajorNumber() == decendantPair.getMajorNumber()
                        && ancestorPair.getMinorNumber() == decendantPair.getMinorNumber()) {
                    continue;
                } else {
                    retVal = false;
                }
            }
            // For trunk revisions, the major number must be <=, and the minor number must be lower
            if (retVal) {
                MajorMinorRevisionPair ancestorPair = ancestorCandidate.getRevisionDescriptor().getRevisionPairs()[0];
                MajorMinorRevisionPair decendantPair = descendantRevision.getRevisionDescriptor().getRevisionPairs()[0];
                if (ancestorPair.getMajorNumber() > decendantPair.getMajorNumber()) {
                    retVal = false;
                } else {
                    if (ancestorPair.getMinorNumber() > decendantPair.getMinorNumber()) {
                        retVal = false;
                    }
                }
            }
        }
        return retVal;
    }

    private LogFileHeaderInfo buildBranchLogFileHeaderInfo(LogFile logFile, RevisionInformation branchRevisionInformation) {
        LogFileHeaderInfo logFileHeaderInfo;

        LabelInfo[] labelInfo = buildBranchLabelInfo(logFile, branchRevisionInformation);

        LogFileHeader logFileHeader = buildBranchLogFileHeader(logFile, branchRevisionInformation);

        logFileHeaderInfo = new LogFileHeaderInfo(logFileHeader);

        logFileHeaderInfo.setAccessList(logFile.getLogFileHeaderInfo().getAccessList());
        logFileHeaderInfo.setCommentPrefix(logFile.getLogFileHeaderInfo().getCommentPrefix());
        logFileHeaderInfo.setModifierList(logFile.getLogFileHeaderInfo().getModifierList());
        logFileHeaderInfo.setModuleDescription(logFile.getLogFileHeaderInfo().getModuleDescription());
        logFileHeaderInfo.setOwner(logFile.getLogFileHeaderInfo().getOwner());
        logFileHeaderInfo.setWorkfileName(logFile.getLogFileHeaderInfo().getWorkfileName());
        logFileHeaderInfo.setLabelInfo(labelInfo);
        logFileHeaderInfo.setDefaultRevisionDescriptor(branchRevisionInformation.getRevisionHeader(0).getRevisionDescriptor());

        return logFileHeaderInfo;
    }

    private LogFileHeader buildBranchLogFileHeader(LogFile logFile, RevisionInformation branchRevisionInformation) {
        LogFileHeader branchLogFileHeader = new LogFileHeader();
        LogFileHeader existingLogFileHeader = logFile.getLogFileHeaderInfo().getLogFileHeader();
        RevisionHeader tipRevisionHeader = branchRevisionInformation.getRevisionHeader(0);

        branchLogFileHeader.getQVCSVersion().setValue((short) QVCSConstants.QVCS_ARCHIVE_VERSION);

        branchLogFileHeader.getMajorNumber().setValue((short) tipRevisionHeader.getMajorNumber());
        branchLogFileHeader.getMinorNumber().setValue((short) tipRevisionHeader.getMinorNumber());
        branchLogFileHeader.getAttributeBits().setValue((short) existingLogFileHeader.attributes().getAttributesAsInt());
        branchLogFileHeader.getVersionCount().setValue((short) 0);
        branchLogFileHeader.getRevisionCount().setValue((short) getRevisionCount());
        branchLogFileHeader.getDefaultDepth().setValue((short) deduceDefaultDepth());
        branchLogFileHeader.getLockCount().setValue((short) 0);
        branchLogFileHeader.getAccessSize().setValue((short) 0);
        branchLogFileHeader.getModifierSize().setValue((short) 0);
        branchLogFileHeader.getCommentSize().setValue((short) 0);
        branchLogFileHeader.getOwnerSize().setValue((short) 0);
        branchLogFileHeader.getDescSize().setValue((short) 0);
        branchLogFileHeader.getSupplementalInfoSize().setValue((short) 0);
        // Lock checking is not allowed on the translucent branch.
        ArchiveAttributes branchHeaderAttributes = new ArchiveAttributes(branchLogFileHeader.getAttributeBits().getValue());
        branchHeaderAttributes.setIsCheckLock(false);
        branchLogFileHeader.setAttributes(branchHeaderAttributes);
        branchLogFileHeader.getCheckSum().setValue(branchLogFileHeader.computeCheckSum());

        return branchLogFileHeader;
    }

    @Override
    public synchronized RevisionInformation getRevisionInformation() {
        return this.logfileInfo.getRevisionInformation();
    }

    private LabelInfo[] buildBranchLabelInfo(LogFile logFile, RevisionInformation viewRevisionInformation) {
        LabelInfo[] viewLabelInfo = null;
        if (logFile.getLogfileInfo().getLogFileHeaderInfo().getLogFileHeader().getVersionCount().getValue() > 0) {
            // Build the set of revision strings visible for this view. Only include
            // a label if the label's revision string is in that set.
            Set<String> viewRevisionStringSet = new HashSet<>();
            for (int i = 0; i < getRevisionCount(); i++) {
                viewRevisionStringSet.add(viewRevisionInformation.getRevisionHeader(i).getRevisionString());
            }

            ArrayList<LabelInfo> viewLabelArray = new ArrayList<>();
            LabelInfo[] currentLogFileLabelInfo = logFile.getLogFileHeaderInfo().getLabelInfo();
            for (LabelInfo currentLogFileLabelInfo1 : currentLogFileLabelInfo) {
                String labelRevisionString = currentLogFileLabelInfo1.getLabelRevisionString();
                if (viewRevisionStringSet.contains(labelRevisionString)) {
                    // Make sure that we do NOT include the viewLabel... it's for
                    // internal use only.
                    if (0 != currentLogFileLabelInfo1.getLabelString().compareToIgnoreCase(getProjectView().getTranslucentBranchLabel())) {
                        viewLabelArray.add(currentLogFileLabelInfo1);
                    }
                }
            }

            if (viewLabelArray.size() > 0) {
                viewLabelInfo = new LabelInfo[viewLabelArray.size()];
                for (int i = 0; i < viewLabelArray.size(); i++) {
                    viewLabelInfo[i] = viewLabelArray.get(i);
                }
            }
        }
        return viewLabelInfo;
    }

    @Override
    public synchronized int getRevisionCount() {
        return this.revisionCount;
    }

    private ProjectView getProjectView() {
        return this.projectView;
    }

    /**
     * Get the branch label associated with the translucent branch that this archive info is associated with.
     * @return the branch label associated with the translucent branch that this archive info is associated with.
     */
    public String getBranchLabel() {
        return this.branchLabel;
    }

    /**
     * Figure out the tip revision string for this branch.
     *
     * @return the branch tip revision string.
     */
    public String getBranchTipRevisionString() {
        String branchTipRevisionString = "";

        try {
            // We have to go to the original logfile to get this info, since we don't
            // include the branchLabel in the labelInfo that we keep here.
            LogFile logfile = getCurrentLogFile();
            if (logfile.getLogfileInfo().getLogFileHeaderInfo().hasLabel(getBranchLabel())) {
                branchTipRevisionString = logfile.getLogfileInfo().getLogFileHeaderInfo().getRevisionStringForLabel(getBranchLabel());
            } else {
                branchTipRevisionString = logfile.getDefaultRevisionString();
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return branchTipRevisionString;
    }

    /**
     * Figure out the tip revision string for this branch.
     */
    private String getBranchTipRevisionString(LogFile logfile) {
        String branchRevisionString;

        // We have to go to the original logfile to get this info, since we don't
        // include the viewLabel in the labelInfo that we keep here.
        if (logfile.getLogfileInfo().getLogFileHeaderInfo().hasLabel(getBranchLabel())) {
            branchRevisionString = logfile.getLogfileInfo().getLogFileHeaderInfo().getRevisionStringForLabel(getBranchLabel());
        } else {
            branchRevisionString = logfile.getDefaultRevisionString();
        }
        return branchRevisionString;
    }

    /**
     * Add a logfile listener.
     * @param l the logfile listener to add.
     */
    public synchronized void addListener(LogfileListenerInterface l) {
        if (this.logfileListeners == null) {
            this.logfileListeners = new ArrayList<>();
        }
        this.logfileListeners.add(l);
    }

    /**
     * Remove a logfile listener.
     * @param l the listener to remove.
     */
    public synchronized void removeListener(LogfileListenerInterface l) {
        if (this.logfileListeners == null) {
            this.logfileListeners = new ArrayList<>();
        } else {
            this.logfileListeners.remove(l);
        }
    }

    private synchronized void notifyLogfileListeners(ActionType action) {
        if (this.logfileListeners != null) {
            // Make a copy of the listener array so we can avoid concurrent modification exceptions
            // which happen when we handle the delete notification (which removes itself as a listener).
            ArrayList<LogfileListenerInterface> listenersCopy = new ArrayList<>(this.logfileListeners);
            Iterator<LogfileListenerInterface> i = listenersCopy.iterator();
            while (i.hasNext()) {
                LogfileListenerInterface listener = i.next();
                listener.notifyLogfileListener(this, action);
            }
        }
    }

    /**
     * We receive notifications here directly from the LogFile object. We'll forward this notification on to our listeners if the
     * change resulted in a meaningful change.
     *
     * @param subject the LogFile object that has changed.
     * @param action the type of change made to the LogFile.
     */
    @Override
    public synchronized void notifyLogfileListener(ArchiveInfoInterface subject, ActionType action) {
        LOGGER.info("Received notification [" + action.getActionType() + "] on translucent branch for: [" + subject.getShortWorkfileName() + "]");
        int oldRevisionCount = getRevisionCount();
        int oldAttributes = getAttributes().getAttributesAsInt();
        int oldLabelCount = getLogfileInfo().getLogFileHeaderInfo().getLogFileHeader().versionCount();
        boolean oldOverlapFlag = getIsOverlap();
        if (subject instanceof LogFile) {
            LogFile logfile = (LogFile) subject;
            this.logfileInfo = buildLogfileInfo(logfile);
            this.defaultRevisionDigest = ArchiveDigestManager.getInstance().getArchiveDigest(logfile, getDefaultRevisionString());
            int newLabelCount = getLogfileInfo().getLogFileHeaderInfo().getLogFileHeader().versionCount();

            // If things changed enough to matter, then let our listeners know about the change.
            if ((oldRevisionCount != getRevisionCount())
                    || (oldAttributes != getAttributes().getAttributesAsInt())
                    || (oldLabelCount != newLabelCount)
                    || (oldOverlapFlag != getIsOverlap())
                    || (action.getAction() == ActionType.CHANGE_HEADER)
                    || (action.getAction() == ActionType.CHANGE_REVHEADER)
                    || (action.getAction() == ActionType.SET_REVISION_DESCRIPTION)
                    || (action.getAction() == ActionType.SET_MODULE_DESCRIPTION)
                    || // Include checkins since that could cause creation of overlap/conflict
                    (action.getAction() == ActionType.CHECKIN)) {
                // If the logfile has revisions associated with this branch, and this is a remove
                // operation, then we need to 'eat' the remove notification, and turn it into
                // a change header type notification, since we do not want to remove this
                // file from the branch's directory manager.
                if (action.getAction() == ActionType.REMOVE) {
                    if (logfile.hasLabel(getBranchLabel())) {
                        action = new SetAttributes(getAttributes());
                        LOGGER.info("\ttranslated to notification: [" + action.getActionType() + "]  on translucent branch for: ["
                                + subject.getShortWorkfileName() + "]");
                    }
                }
                notifyLogfileListeners(action);
            }

            if (action.getAction() == ActionType.REMOVE) {
                handleRemoveNotification(logfile);
            }
        }
    }

    private void handleRemoveNotification(LogFile logFile) {
        // If the logfile hasn't had any changes for this branch, then we are not
        // interested in it any more, since it will be in the cemetery and will not
        // represent any file that we are interested in on the branch.
        if (!logFile.hasLabel(getBranchLabel())) {
            logFile.removeListener(this);
        }
    }

    private String getRevisionStringFromLabel(final java.lang.String labelString, LogFile logFile) throws QVCSException {
        String revisionStringForLabel = null;

        // Find the label, if we can.
        if ((labelString != null) && (logFile != null)) {
            LabelInfo[] labelInfo = logFile.getLogFileHeaderInfo().getLabelInfo();
            if (labelInfo != null) {
                for (LabelInfo labelInfo1 : labelInfo) {
                    if (labelString.equals(labelInfo1.getLabelString())) {
                        // If it is a floating label, we have to figure out the
                        // revision string...
                        if (labelInfo1.isFloatingLabel()) {
                            throw new QVCSException("Internal Error. Found floating label for a branch!!");
                        } else {
                            revisionStringForLabel = labelInfo1.getLabelRevisionString();
                        }
                        break;
                    }
                }
            }
        }
        return revisionStringForLabel;
    }

    @Override
    public synchronized boolean getIsOverlap() {
        return isOverlapFlag;
    }
}
