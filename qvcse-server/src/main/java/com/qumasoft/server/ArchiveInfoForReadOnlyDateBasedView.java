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

import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeader;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive info for read-only date based view.
 * @author Jim Voris
 */
public final class ArchiveInfoForReadOnlyDateBasedView implements ArchiveInfoInterface, LogFileInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveInfoForReadOnlyDateBasedView.class);
    private final String shortWorkfileName;
    private Date lastCheckInDate;
    private String lastEditBy;
    private String defaultRevisionString;
    private final byte[] defaultRevisionDigest;
    private final ArchiveAttributes archiveAttributes;
    private final LogfileInfo logfileInfo;
    private int logFileFileID = -1;
    private final String projectName;
    private int revisionCount = -1;
    private String fullLogfileName;
    private Set<String> validRevisionNumbers;

    /**
     * Creates a new instance of ArchiveInfoForReadOnlyDateBasedView.
     * @param shortName the short workfile name.
     * @param logFile the LogFile from which we create this archive info for this read only date-based branch.
     * @param remoteViewProperties the project properties.
     */
    ArchiveInfoForReadOnlyDateBasedView(String shortName, LogFile logFile, RemoteBranchProperties remoteViewProperties) {
        shortWorkfileName = shortName;
        projectName = remoteViewProperties.getProjectName();
        logfileInfo = buildLogfileInfo(logFile, remoteViewProperties);
        defaultRevisionDigest = ArchiveDigestManager.getInstance().getArchiveDigest(logFile, getDefaultRevisionString());
        archiveAttributes = new ArchiveAttributes(logFile.getAttributes());
        logFileFileID = logFile.getFileID();
    }

    @Override
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    // These can NEVER be locked, since they are read-only.
    @Override
    public int getLockCount() {
        return 0;
    }

    @Override
    public String getLockedByString() {
        return "";
    }

    @Override
    public Date getLastCheckInDate() {
        return lastCheckInDate;
    }

    // This is always an empty string, since a file can never be locked.
    @Override
    public String getWorkfileInLocation() {
        return "";
    }

    @Override
    public String getLastEditBy() {
        return lastEditBy;
    }

    @Override
    public byte[] getDefaultRevisionDigest() {
        return defaultRevisionDigest;
    }

    @Override
    public ArchiveAttributes getAttributes() {
        return archiveAttributes;
    }

    @Override
    public LogfileInfo getLogfileInfo() {
        return logfileInfo;
    }

    @Override
    public String getRevisionDescription(final String revisionString) {
        String returnValue = null;
        try {
            if (validateRevisionString(revisionString)) {
                int revisionIndex = getLogfileInfo().getRevisionInformation().getRevisionIndex(revisionString);
                returnValue = getLogfileInfo().getRevisionInformation().getRevisionHeader(revisionIndex).getRevisionDescription();
            } else {
                LOGGER.warn("Requested revision: " + revisionString + " is not present in this view!");
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnValue;
    }

    @Override
    public byte[] getRevisionAsByteArray(String revisionString) {
        byte[] returnValue = null;
        try {
            if (validateRevisionString(revisionString)) {
                returnValue = getCurrentLogFile().getRevisionAsByteArray(revisionString);
            } else {
                LOGGER.warn("Requested revision: " + revisionString + " is not present in this view!");
            }
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnValue;
    }

    @Override
    public String getLockedRevisionString(String userName) {
        return "";
    }

    @Override
    public String getDefaultRevisionString() {
        return defaultRevisionString;
    }

    @Override
    public boolean checkInRevision(CheckInCommandArgs commandArgs, String checkInFilename, boolean ignoreLocksToEnableBranchCheckInFlag) throws QVCSException {
        return false;
    }

    @Override
    public boolean checkOutRevision(CheckOutCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        return false;
    }

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

    @Override
    public boolean lockRevision(LockRevisionCommandArgs commandArgs) throws QVCSException {
        return false;
    }

    @Override
    public boolean unlockRevision(UnlockRevisionCommandArgs commandArgs) throws QVCSException {
        return false;
    }

    @Override
    public boolean breakLock(UnlockRevisionCommandArgs commandArgs) throws QVCSException {
        return false;
    }

    @Override
    public boolean labelRevision(LabelRevisionCommandArgs commandArgs) throws QVCSException {
        return false;
    }

    @Override
    public boolean unLabelRevision(UnLabelRevisionCommandArgs commandArgs) throws QVCSException {
        return false;
    }

    @Override
    public boolean deleteArchive(String userName) throws QVCSException {
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
        return false;
    }

    private String getProjectName() {
        return projectName;
    }

    @Override
    public String getFullArchiveFilename() {
        return fullLogfileName;
    }

    private LogFile getCurrentLogFile() throws QVCSException {

        FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH, logFileFileID);
        int directoryID = fileIDInfo.getDirectoryID();

        // Lookup the archiveDirManager for the file's current location...
        ArchiveDirManager archiveDirManager = DirectoryIDDictionary.getInstance().lookupArchiveDirManager(getProjectName(), directoryID, null);

        String keyToFile = fileIDInfo.getShortFilename();
        boolean ignoreCaseFlag = archiveDirManager.getProjectProperties().getIgnoreCaseFlag();
        if (ignoreCaseFlag) {
            keyToFile = keyToFile.toLowerCase();
        }

        // Get the file's current archiveInfo...
        LogFile archiveInfo = (LogFile) archiveDirManager.getArchiveInfo(keyToFile);
        return archiveInfo;
    }

    private LogfileInfo buildLogfileInfo(LogFile logFile, RemoteBranchProperties remoteViewProperties) {
        LogfileInfo info = null;
        fullLogfileName = logFile.getFullArchiveFilename();
        RevisionInformation viewRevisionInformation = buildViewRevisionInformation(logFile, remoteViewProperties);
        if (viewRevisionInformation != null) {
            LogFileHeaderInfo viewLogFileHeaderInfo = buildViewLogFileHeaderInfo(logFile, viewRevisionInformation);
            info = new LogfileInfo(viewLogFileHeaderInfo, viewRevisionInformation, getFileID(), fullLogfileName);

            // And save away some things for our accessor methods...
            RevisionHeader tipRevisionHeader = viewRevisionInformation.getRevisionHeader(0);
            defaultRevisionString = tipRevisionHeader.getRevisionString();

            AccessList modifierList = new AccessList(info.getLogFileHeaderInfo().getModifierList());
            lastEditBy = modifierList.indexToUser(tipRevisionHeader.getCreatorIndex());
            lastCheckInDate = tipRevisionHeader.getCheckInDate();
        }
        return info;
    }

    private RevisionInformation buildViewRevisionInformation(LogFile logFile, RemoteBranchProperties remoteViewProperties) {
        RevisionInformation viewRevisionInformation = null;
        validRevisionNumbers = new HashSet<>();

        long viewDate = remoteViewProperties.getDateBasedDate().getTime();

        // Need to create the set of revisions that are visible for this view...
        String sortableTipRevisionString = getSortableRevisionStringForChosenBranch(remoteViewProperties.getDateBasedBranch(), logFile);
        TreeMap<String, RevisionHeader> revisionMap = new TreeMap<>();
        TreeMap<String, Integer> revisionIndexMap = new TreeMap<>();
        RevisionInformation revisionInformation = logFile.getRevisionInformation();

        // Put all the revisions in a sorted map.
        for (int i = 0; i < logFile.getRevisionCount(); i++) {
            RevisionHeader revisionHeader = revisionInformation.getRevisionHeader(i);
            String sortableRevisionString = revisionHeader.getRevisionDescriptor().toSortableString();
            revisionMap.put(sortableRevisionString, revisionHeader);

            // So we can later put the revisions in revisionIndex order...
            revisionIndexMap.put(sortableRevisionString, i);
        }

        // Now iterate through all the revisions finding those that are on the
        // view's 'branch' that are also before the view's date.
        TreeMap<Integer, RevisionHeader> branchRevisionMapKeyedByRevisionIndex = new TreeMap<>();
        Iterator<Map.Entry<String, RevisionHeader>> entrySetIt = revisionMap.entrySet().iterator();
        while (entrySetIt.hasNext()) {
            Map.Entry<String, RevisionHeader> mapEntry = entrySetIt.next();
            String sortableRevisionString = mapEntry.getKey();
            if (sortableRevisionString.compareTo(sortableTipRevisionString) <= 0) {
                RevisionHeader revisionHeader = mapEntry.getValue();
                String checkInDateString = revisionHeader.getCheckInDate().toString();
                String viewDateString = remoteViewProperties.getDateBasedDate().toString();
                LOGGER.trace("Comparing dates. Check in date: [" + checkInDateString + "]. View date: [" + viewDateString + "]");
                if (revisionHeader.getCheckInDate().getTime() <= viewDate) {
                    // Use the revisionIndex as the key so when we pull these out,
                    // they will be in revisionIndex order, instead the order
                    // that you would get by using the sortableRevisionString.
                    branchRevisionMapKeyedByRevisionIndex.put(revisionIndexMap.get(sortableRevisionString), revisionHeader);
                }
            }
        }

        if (branchRevisionMapKeyedByRevisionIndex.size() > 0) {
            // Ok. The revisions in the branchRevisionMap are the ones we need.
            AccessList accessList = new AccessList(logFile.getLogFileHeaderInfo().getAccessList());
            viewRevisionInformation = new RevisionInformation(branchRevisionMapKeyedByRevisionIndex.size(), accessList, accessList);
            setRevisionCount(branchRevisionMapKeyedByRevisionIndex.size());

            // Iterate through the revisions, adding them to the RevisionInformation object.
            // The revisions will appear in the new RevisionInformation object
            // in revisionIndex order.
            Iterator<RevisionHeader> revisionIterator = branchRevisionMapKeyedByRevisionIndex.values().iterator();
            int revisionIndex = 0;
            while (revisionIterator.hasNext()) {
                RevisionHeader revisionHeader = revisionIterator.next();
                RevisionHeader viewRevisionHeader = new RevisionHeader(revisionHeader);

                // As this is a read-only view, no locks are allowed.
                viewRevisionHeader.setIsLocked(false);
                viewRevisionInformation.updateRevision(revisionIndex++, viewRevisionHeader);

                // Save away the valid revision numbers.
                validRevisionNumbers.add(viewRevisionHeader.getRevisionString());
            }
        }

        return viewRevisionInformation;
    }

    private String getSortableRevisionStringForChosenBranch(final String branchString, final LogFile logFile) {
        String revisionStringForBranch = null;
        String sortableRevisionStringForBranch = null;

        // This is a 'get by label' request.  Find the label, if we can.
        if ((branchString != null) && (logFile != null)) {
            if (0 == branchString.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                // We're on the trunk...
                revisionStringForBranch = logFile.getRevisionInformation().getRevisionHeader(0).getRevisionString();
            } else {
                LabelInfo[] labelInfo = logFile.getLogFileHeaderInfo().getLabelInfo();
                if (labelInfo != null) {
                    for (LabelInfo labelInfo1 : labelInfo) {
                        if (branchString.equals(labelInfo1.getLabelString())) {
                            // If it is a floating label, we have to figure out the
                            // revision string...
                            if (labelInfo1.isFloatingLabel()) {
                                RevisionInformation revisionInformation = logFile.getRevisionInformation();
                                int revCount = logFile.getRevisionCount();
                                for (int j = 0; j < revCount; j++) {
                                    RevisionHeader revHeader = revisionInformation.getRevisionHeader(j);
                                    if (revHeader.getDepth() == labelInfo1.getDepth()) {
                                        if (revHeader.isTip()) {
                                            String labelRevisionString = labelInfo1.getLabelRevisionString();
                                            String revisionString = revHeader.getRevisionString();
                                            if (revisionString.startsWith(labelRevisionString)) {
                                                revisionStringForBranch = revisionString;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                revisionStringForBranch = labelInfo1.getLabelRevisionString();
                            }
                            break;
                        }
                    }
                }
            }

            if (revisionStringForBranch != null) {
                int revisionIndex = logFile.getRevisionInformation().getRevisionIndex(revisionStringForBranch);
                RevisionHeader revisionHeader = logFile.getRevisionInformation().getRevisionHeader(revisionIndex);
                sortableRevisionStringForBranch = revisionHeader.getRevisionDescriptor().toSortableString();
            }
        }
        return sortableRevisionStringForBranch;
    }

    private LogFileHeaderInfo buildViewLogFileHeaderInfo(LogFile logFile, RevisionInformation viewRevisionInformation) {
        LogFileHeaderInfo logFileHeaderInfo;

        LabelInfo[] labelInfo = buildViewLabelInfo(logFile, viewRevisionInformation);

        LogFileHeader logFileHeader = buildViewLogFileHeader(logFile, viewRevisionInformation);

        logFileHeaderInfo = new LogFileHeaderInfo(logFileHeader);

        logFileHeaderInfo.setAccessList(logFile.getLogFileHeaderInfo().getAccessList());
        logFileHeaderInfo.setCommentPrefix(logFile.getLogFileHeaderInfo().getCommentPrefix());
        logFileHeaderInfo.setModifierList(logFile.getLogFileHeaderInfo().getModifierList());
        logFileHeaderInfo.setModuleDescription(logFile.getLogFileHeaderInfo().getModuleDescription());
        logFileHeaderInfo.setOwner(logFile.getLogFileHeaderInfo().getOwner());
        logFileHeaderInfo.setWorkfileName(logFile.getLogFileHeaderInfo().getWorkfileName());
        logFileHeaderInfo.setLabelInfo(labelInfo);

        return logFileHeaderInfo;
    }

    private LogFileHeader buildViewLogFileHeader(LogFile logFile, RevisionInformation viewRevisionInformation) {
        LogFileHeader viewLogFileHeader = new LogFileHeader();
        LogFileHeader existingLogFileHeader = logFile.getLogFileHeaderInfo().getLogFileHeader();
        RevisionHeader tipRevisionHeader = viewRevisionInformation.getRevisionHeader(0);

        viewLogFileHeader.getQVCSVersion().setValue((short) QVCSConstants.QVCS_ARCHIVE_VERSION);

        viewLogFileHeader.getMajorNumber().setValue((short) tipRevisionHeader.getMajorNumber());
        viewLogFileHeader.getMinorNumber().setValue((short) tipRevisionHeader.getMinorNumber());
        viewLogFileHeader.getAttributeBits().setValue((short) existingLogFileHeader.attributes().getAttributesAsInt());
        viewLogFileHeader.getVersionCount().setValue((short) 0);
        viewLogFileHeader.getRevisionCount().setValue((short) getRevisionCount());
        viewLogFileHeader.getDefaultDepth().setValue((short) 0);
        viewLogFileHeader.getLockCount().setValue((short) 0);
        viewLogFileHeader.getAccessSize().setValue((short) 0);
        viewLogFileHeader.getModifierSize().setValue((short) 0);
        viewLogFileHeader.getCommentSize().setValue((short) 0);
        viewLogFileHeader.getOwnerSize().setValue((short) 0);
        viewLogFileHeader.getDescSize().setValue((short) 0);
        viewLogFileHeader.getSupplementalInfoSize().setValue((short) 0);
        viewLogFileHeader.setAttributes(new ArchiveAttributes(viewLogFileHeader.getAttributeBits().getValue()));
        viewLogFileHeader.getCheckSum().setValue(viewLogFileHeader.computeCheckSum());

        return viewLogFileHeader;
    }

    @Override
    public RevisionInformation getRevisionInformation() {
        return logfileInfo.getRevisionInformation();
    }

    private LabelInfo[] buildViewLabelInfo(LogFile logFile, RevisionInformation viewRevisionInformation) {
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
                    viewLabelArray.add(currentLogFileLabelInfo1);
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
    public int getRevisionCount() {
        return this.revisionCount;
    }

    private void setRevisionCount(int revCount) {
        this.revisionCount = revCount;
    }

    private boolean validateRevisionString(String revisionString) {
        return validRevisionNumbers.contains(revisionString);
    }

    @Override
    public int getFileID() {
        return this.logFileFileID;
    }

    /**
     * We do not support overlap detection for read-only date based views.
     *
     * @return false;
     */
    @Override
    public boolean getIsOverlap() {
        return false;
    }
}
