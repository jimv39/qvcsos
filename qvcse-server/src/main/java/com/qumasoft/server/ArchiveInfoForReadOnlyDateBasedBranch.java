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
import com.qumasoft.qvcslib.QVCSRuntimeException;
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
 * Archive info for read-only date based branch.
 * @author Jim Voris
 */
public final class ArchiveInfoForReadOnlyDateBasedBranch implements ArchiveInfoInterface, LogFileInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveInfoForReadOnlyDateBasedBranch.class);
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
     * Creates a new instance of ArchiveInfoForReadOnlyDateBasedBranch.
     * @param shortName the short workfile name.
     * @param logFile the LogFile from which we create this archive info for this read only date-based branch.
     * @param remoteBranchProperties the project properties.
     */
    ArchiveInfoForReadOnlyDateBasedBranch(String shortName, LogFile logFile, RemoteBranchProperties remoteBranchProperties) {
        shortWorkfileName = shortName;
        projectName = remoteBranchProperties.getProjectName();
        logfileInfo = buildLogfileInfo(logFile, remoteBranchProperties);
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
                LOGGER.warn("Requested revision: " + revisionString + " is not present in this branch!");
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
                LOGGER.warn("Requested revision: " + revisionString + " is not present in this branch!");
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

    private LogfileInfo buildLogfileInfo(LogFile logFile, RemoteBranchProperties remoteBranchProperties) {
        LogfileInfo info = null;
        fullLogfileName = logFile.getFullArchiveFilename();
        RevisionInformation branchRevisionInformation = buildBranchRevisionInformation(logFile, remoteBranchProperties);
        if (branchRevisionInformation != null) {
            LogFileHeaderInfo branchLogFileHeaderInfo = buildBranchLogFileHeaderInfo(logFile, branchRevisionInformation);
            info = new LogfileInfo(branchLogFileHeaderInfo, branchRevisionInformation, getFileID(), fullLogfileName);

            // And save away some things for our accessor methods...
            RevisionHeader tipRevisionHeader = branchRevisionInformation.getRevisionHeader(0);
            defaultRevisionString = tipRevisionHeader.getRevisionString();

            AccessList modifierList = new AccessList(info.getLogFileHeaderInfo().getModifierList());
            lastEditBy = modifierList.indexToUser(tipRevisionHeader.getCreatorIndex());
            lastCheckInDate = tipRevisionHeader.getCheckInDate();
        }
        return info;
    }

    private RevisionInformation buildBranchRevisionInformation(LogFile logFile, RemoteBranchProperties remoteBranchProperties) {
        RevisionInformation branchRevisionInformation = null;
        validRevisionNumbers = new HashSet<>();

        long branchDate = remoteBranchProperties.getDateBasedDate().getTime();

        // Need to create the set of revisions that are visible for this branch...
        String sortableTipRevisionString = getSortableRevisionStringForChosenBranch(remoteBranchProperties.getBranchParent(), logFile);
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
        // branch's 'branch' that are also before the branch's date.
        TreeMap<Integer, RevisionHeader> branchRevisionMapKeyedByRevisionIndex = new TreeMap<>();
        Iterator<Map.Entry<String, RevisionHeader>> entrySetIt = revisionMap.entrySet().iterator();
        while (entrySetIt.hasNext()) {
            Map.Entry<String, RevisionHeader> mapEntry = entrySetIt.next();
            String sortableRevisionString = mapEntry.getKey();
            if (sortableRevisionString.compareTo(sortableTipRevisionString) <= 0) {
                RevisionHeader revisionHeader = mapEntry.getValue();
                String checkInDateString = revisionHeader.getCheckInDate().toString();
                String branchDateString = remoteBranchProperties.getDateBasedDate().toString();
                LOGGER.trace("Comparing dates. Check in date: [" + checkInDateString + "]. Branch date: [" + branchDateString + "]");
                if (revisionHeader.getCheckInDate().getTime() <= branchDate) {
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
            branchRevisionInformation = new RevisionInformation(branchRevisionMapKeyedByRevisionIndex.size(), accessList, accessList);
            setRevisionCount(branchRevisionMapKeyedByRevisionIndex.size());

            // Iterate through the revisions, adding them to the RevisionInformation object.
            // The revisions will appear in the new RevisionInformation object
            // in revisionIndex order.
            Iterator<RevisionHeader> revisionIterator = branchRevisionMapKeyedByRevisionIndex.values().iterator();
            int revisionIndex = 0;
            while (revisionIterator.hasNext()) {
                RevisionHeader revisionHeader = revisionIterator.next();
                RevisionHeader branchRevisionHeader = new RevisionHeader(revisionHeader);

                // As this is a read-only branch, no locks are allowed.
                branchRevisionHeader.setIsLocked(false);
                branchRevisionInformation.updateRevision(revisionIndex++, branchRevisionHeader);

                // Save away the valid revision numbers.
                validRevisionNumbers.add(branchRevisionHeader.getRevisionString());
            }
        }

        return branchRevisionInformation;
    }

    private String getSortableRevisionStringForChosenBranch(final String branchString, final LogFile logFile) {
        String revisionStringForBranch = null;
        String sortableRevisionStringForBranch = null;

        if ((branchString != null) && (logFile != null)) {
            if (0 == branchString.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                // We're on the trunk...
                revisionStringForBranch = logFile.getRevisionInformation().getRevisionHeader(0).getRevisionString();
            } else {
                throw new QVCSRuntimeException("Only Trunk is supported as a parent branch for date based read-only branches.");
            }

            if (revisionStringForBranch != null) {
                int revisionIndex = logFile.getRevisionInformation().getRevisionIndex(revisionStringForBranch);
                RevisionHeader revisionHeader = logFile.getRevisionInformation().getRevisionHeader(revisionIndex);
                sortableRevisionStringForBranch = revisionHeader.getRevisionDescriptor().toSortableString();
            }
        } else {
            throw new QVCSRuntimeException("Missing branch string or logfile in getSortableRevisionStringForChosenBranch()");
        }
        return sortableRevisionStringForBranch;
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
        branchLogFileHeader.getDefaultDepth().setValue((short) 0);
        branchLogFileHeader.getLockCount().setValue((short) 0);
        branchLogFileHeader.getAccessSize().setValue((short) 0);
        branchLogFileHeader.getModifierSize().setValue((short) 0);
        branchLogFileHeader.getCommentSize().setValue((short) 0);
        branchLogFileHeader.getOwnerSize().setValue((short) 0);
        branchLogFileHeader.getDescSize().setValue((short) 0);
        branchLogFileHeader.getSupplementalInfoSize().setValue((short) 0);
        branchLogFileHeader.setAttributes(new ArchiveAttributes(branchLogFileHeader.getAttributeBits().getValue()));
        branchLogFileHeader.getCheckSum().setValue(branchLogFileHeader.computeCheckSum());

        return branchLogFileHeader;
    }

    @Override
    public RevisionInformation getRevisionInformation() {
        return logfileInfo.getRevisionInformation();
    }

    private LabelInfo[] buildBranchLabelInfo(LogFile logFile, RevisionInformation branchRevisionInformation) {
        LabelInfo[] branchLabelInfo = null;
        if (logFile.getLogfileInfo().getLogFileHeaderInfo().getLogFileHeader().getVersionCount().getValue() > 0) {
            // Build the set of revision strings visible for this branch. Only include
            // a label if the label's revision string is in that set.
            Set<String> branchRevisionStringSet = new HashSet<>();
            for (int i = 0; i < getRevisionCount(); i++) {
                branchRevisionStringSet.add(branchRevisionInformation.getRevisionHeader(i).getRevisionString());
            }

            ArrayList<LabelInfo> branchLabelArray = new ArrayList<>();
            LabelInfo[] currentLogFileLabelInfo = logFile.getLogFileHeaderInfo().getLabelInfo();
            for (LabelInfo currentLogFileLabelInfo1 : currentLogFileLabelInfo) {
                String labelRevisionString = currentLogFileLabelInfo1.getLabelRevisionString();
                if (branchRevisionStringSet.contains(labelRevisionString)) {
                    branchLabelArray.add(currentLogFileLabelInfo1);
                }
            }

            if (branchLabelArray.size() > 0) {
                branchLabelInfo = new LabelInfo[branchLabelArray.size()];
                for (int i = 0; i < branchLabelArray.size(); i++) {
                    branchLabelInfo[i] = branchLabelArray.get(i);
                }
            }
        }
        return branchLabelInfo;
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
     * We do not support overlap detection for read-only date based branches.
     *
     * @return false;
     */
    @Override
    public boolean getIsOverlap() {
        return false;
    }
}
