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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestBreakLockData;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckInData;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckOutData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetForVisualCompareData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetInfoForMergeData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetLogfileInfoData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionForCompareData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLabelData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLockData;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestResolveConflictFromParentBranchData;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetAttributesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetCommentPrefixData;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetModuleDescriptionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetRevisionDescriptionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnLabelData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnlockData;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logfile proxy. This class is used on the client to act as a proxy for an actual QVCS archive file (a.k.a. LogFile).
 * @author Jim Voris
 */
public class LogFileProxy implements ArchiveInfoInterface {

    private SkinnyLogfileInfo skinnyLogfileInfo;
    private LogfileInfo logfileInfo;
    private final ArchiveDirManagerProxy archiveDirManagerProxy;
    private final TransportProxyInterface transportProxy;
    private InfoForMerge infoForMerge;
    private ResolveConflictResults resolveConflictResults;
    private PromoteFileResults promoteFileResults;
    private final Object syncObject;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileProxy.class);

    /**
     * Creates a new instance of LogFileProxy.
     * @param skinnyInfo the skinny logfile info.
     * @param archiveDirMgrProxy the directory manager proxy for this logfile proxy.
     */
    public LogFileProxy(SkinnyLogfileInfo skinnyInfo, ArchiveDirManagerProxy archiveDirMgrProxy) {
        skinnyLogfileInfo = skinnyInfo;
        archiveDirManagerProxy = archiveDirMgrProxy;
        transportProxy = archiveDirMgrProxy.getTransportProxy();
        syncObject = new Object();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getLastCheckInDate() {
        Date retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getLastCheckInDate();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastEditBy() {
        String retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getLastEditBy();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLockCount() {
        int retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getLockCount();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLockedByString() {
        String retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getLockedByString();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveAttributes getAttributes() {
        ArchiveAttributes retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getAttributes();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortWorkfileName() {
        String retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getShortWorkfileName();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorkfileInLocation() {
        String retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getWorkfileInLocation();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultRevisionString() {
        String retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getDefaultRevisionString();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFileID() {
        int retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getFileID();
        }
        return retVal;
    }

    /**
     * Get the logfile info from the server.  We make this a synchronous call since the caller (typically a GUI element) needs the returned info before it can do anything anyway.
     * This is a lazy type of method -- meaning it only goes to the server if asked, and only goes to the server once. After the 1st round-trip to the server, it holds on
     * to the information returned from the server so that another round trip is not required.
     * @return the LogfileInfo from the server.
     */
    @Override
    public synchronized LogfileInfo getLogfileInfo() {
        if (logfileInfo == null) {
            ClientRequestGetLogfileInfoData clientRequest = new ClientRequestGetLogfileInfoData();

            clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
            clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
            clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());
            clientRequest.setShortWorkfileName(getShortWorkfileName());

            // Send the request.
            transportProxy.write(clientRequest);
            try {
                // Wait for the server response.
                wait();
            } catch (InterruptedException e) {
                LOGGER.warn("Server response not received!!");

                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
        return logfileInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRevisionDescription(final String revisionString) {
        String retVal = "";
        RevisionInformation revisionInformation = getLogfileInfo().getRevisionInformation();
        int revisionIndex = revisionInformation.getRevisionIndex(revisionString);
        if (revisionIndex != -1) {
            retVal = revisionInformation.getRevisionHeader(revisionIndex).getRevisionDescription();
        }
        return retVal;
    }

    /**
     * Update the logfile info.
     * @param info the new logfile info.
     */
    public synchronized void setLogfileInfo(LogfileInfo info) {
        logfileInfo = info;
    }

    /**
     * Return a buffer that contains the requested revision. This method is synchronous.
     *
     * @param revisionString the revision that should be fetched
     * @return a byte array containing the non-keyword expanded copy of the requested revision, or null if the revision cannot be retrieved.
     */
    @Override
    public synchronized byte[] getRevisionAsByteArray(String revisionString) {
        byte[] workfileBuffer;
        workfileBuffer = KeywordContractedWorkfileCache.getInstance().getContractedBufferByName(archiveDirManagerProxy.getProjectName(), archiveDirManagerProxy.getAppendedPath(),
                getShortWorkfileName(), revisionString);
        if (workfileBuffer == null) {
            ClientRequestGetRevisionForCompareData clientRequest = new ClientRequestGetRevisionForCompareData();

            clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
            clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
            clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());
            clientRequest.setRevisionString(revisionString);
            clientRequest.setShortWorkfileName(getShortWorkfileName());

            if (logfileInfo == null) {
                clientRequest.setIsLogfileInfoRequired(true);
            } else {
                clientRequest.setIsLogfileInfoRequired(false);
            }

            transportProxy.write(clientRequest);
            try {
                // Wait for the server response.
                wait();
            } catch (InterruptedException e) {
                LOGGER.warn("Server response not received!!");

                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                workfileBuffer = KeywordContractedWorkfileCache.getInstance().getContractedBufferByName(archiveDirManagerProxy.getProjectName(),
                        archiveDirManagerProxy.getAppendedPath(),
                        getShortWorkfileName(), revisionString);
            }
        }
        return workfileBuffer;
    }

    /**
     * Return the string for the locked revision for the given userName. If the user does not have any locked revisions, return null.
     * @param user the user name.
     * @return the locked revision string for the given user.
     */
    @Override
    public String getLockedRevisionString(String user) {
        String retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getLockedRevisionString(user);
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getDefaultRevisionDigest() {
        byte[] retVal;
        synchronized (syncObject) {
            retVal = skinnyLogfileInfo.getDefaultRevisionDigest();
        }
        return retVal;
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Check in a revision.
     *
     * @param commandArgs the command arguments.
     * @param checkInFilename the check in file name.
     * @param ignoreLocksToEnableBranchCheckInFlag this flag is ignored on the client side, and exists here only to satisfy the interface signature.
     * @return true if things worked; false otherwise.
     */
    @Override
    public boolean checkInRevision(CheckInCommandArgs commandArgs, String checkInFilename, boolean ignoreLocksToEnableBranchCheckInFlag) {
        boolean retVal = false;
        FileInputStream fileInputStream = null;

        ClientRequestCheckInData clientRequest = new ClientRequestCheckInData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandArgs);
        try {
            File checkInFile = new File(checkInFilename);

            if (checkInFile.canRead()) {
                // Need to read the resulting file into a buffer that we can send to the server.
                fileInputStream = new FileInputStream(checkInFile);
                byte[] buffer = new byte[(int) checkInFile.length()];
                Utility.readDataFromStream(buffer, fileInputStream);
                clientRequest.setBuffer(buffer);

                // Save the contracted workfile buffer so when we get the response we can expand keywords
                // without having to 'get' the workfile.
                int cacheIndex = KeywordContractedWorkfileCache.getInstance().addContractedBuffer(archiveDirManagerProxy.getProjectName(),
                        archiveDirManagerProxy.getAppendedPath(),
                        getShortWorkfileName(), buffer);
                clientRequest.setIndex(cacheIndex);

                transportProxy.write(clientRequest);
                retVal = true;
            } else {
                LOGGER.warn("Cannot read [" + checkInFilename + "]. Checkin failed.");
            }
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
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkOutRevision(CheckOutCommandArgs commandLineArgs, String fetchToFileName) {
        ClientRequestCheckOutData clientRequest = new ClientRequestCheckOutData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandLineArgs);
        commandLineArgs.setOutputFileName(fetchToFileName);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getForVisualCompare(GetRevisionCommandArgs commandLineArgs, String outputFileName) {
        ClientRequestGetForVisualCompareData clientRequest = new ClientRequestGetForVisualCompareData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandLineArgs);
        commandLineArgs.setOutputFileName(outputFileName);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getRevision(GetRevisionCommandArgs commandLineArgs, String fetchToFileName) {
        ClientRequestGetRevisionData clientRequest = new ClientRequestGetRevisionData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandLineArgs);
        commandLineArgs.setOutputFileName(fetchToFileName);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean lockRevision(LockRevisionCommandArgs commandArgs) {
        ClientRequestLockData clientRequest = new ClientRequestLockData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandArgs);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unlockRevision(UnlockRevisionCommandArgs commandArgs) {
        ClientRequestUnlockData clientRequest = new ClientRequestUnlockData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandArgs);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean breakLock(UnlockRevisionCommandArgs commandArgs) {
        ClientRequestBreakLockData clientRequest = new ClientRequestBreakLockData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandArgs);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean labelRevision(LabelRevisionCommandArgs commandArgs) {
        ClientRequestLabelData clientRequest = new ClientRequestLabelData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandArgs);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unLabelRevision(UnLabelRevisionCommandArgs commandArgs) {
        ClientRequestUnLabelData clientRequest = new ClientRequestUnLabelData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandArgs);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteArchive(String userName) {
        // This IS used.
        ClientRequestDeleteFileData clientRequest = new ClientRequestDeleteFileData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setShortWorkfileName(getShortWorkfileName());

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean setAttributes(String userName, ArchiveAttributes attributes) {
        boolean retVal = true;
        ClientRequestSetAttributesData clientRequest = new ClientRequestSetAttributesData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setShortWorkfileName(getShortWorkfileName());
        clientRequest.setAttributes(attributes);

        transportProxy.write(clientRequest);

        try {
            // Wait for the server response.
            wait();
        } catch (InterruptedException e) {
            LOGGER.warn("Server response not received!!");
            retVal = false;

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean setCommentPrefix(String userName, String commentPrefix) {
        boolean retVal = true;
        ClientRequestSetCommentPrefixData clientRequest = new ClientRequestSetCommentPrefixData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setShortWorkfileName(getShortWorkfileName());
        clientRequest.setCommentPrefix(commentPrefix);

        transportProxy.write(clientRequest);

        try {
            // Wait for the server response.
            wait();
        } catch (InterruptedException e) {
            LOGGER.warn("Server response not received!!");
            retVal = false;

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean setModuleDescription(String userName, String moduleDescription) {
        boolean retVal = true;
        ClientRequestSetModuleDescriptionData clientRequest = new ClientRequestSetModuleDescriptionData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setShortWorkfileName(getShortWorkfileName());
        clientRequest.setModuleDescription(moduleDescription);

        transportProxy.write(clientRequest);

        try {
            // Wait for the server response.
            wait();
        } catch (InterruptedException e) {
            LOGGER.warn("Server response not received!!");
            retVal = false;

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setRevisionDescription(SetRevisionDescriptionCommandArgs commandArgs) {
        ClientRequestSetRevisionDescriptionData clientRequest = new ClientRequestSetRevisionDescriptionData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());

        clientRequest.setCommandArgs(commandArgs);

        transportProxy.write(clientRequest);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRevisionCount() {
        int revisionCount;
        synchronized (syncObject) {
            revisionCount = skinnyLogfileInfo.getRevisionCount();
        }
        return revisionCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RevisionInformation getRevisionInformation() {
        return getLogfileInfo().getRevisionInformation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsOverlap() {
        boolean overlapFlag;
        synchronized (syncObject) {
            overlapFlag = skinnyLogfileInfo.getOverlapFlag();
        }
        return overlapFlag;
    }

    /**
     * Update the info for merge.
     * @param mergeInfo the merge info.
     */
    public synchronized void setInfoForMerge(InfoForMerge mergeInfo) {
        this.infoForMerge = mergeInfo;
    }

    /**
     * Update the resolve conflict results.
     * @param resolvConflictResults the resolve conflict results.
     */
    public synchronized void setResolveConflictResults(ResolveConflictResults resolvConflictResults) {
        this.resolveConflictResults = resolvConflictResults;
    }

    /**
     * Set the promote file results.
     * @param promteFileResults the promote file results.
     */
    public void setPromoteFileResults(PromoteFileResults promteFileResults) {
        this.promoteFileResults = promteFileResults;
    }

    /**
     * Get the info needed for a merge. This is a synchronous round-trip to the server.
     * @param project the project name.
     * @param branch the branch name.
     * @param path the appended path.
     * @param id the file id.
     * @return the info needed for a merge.
     */
    public synchronized InfoForMerge getInfoForMerge(String project, String branch, String path, int id) {
        InfoForMerge infoForMergeFromServer = null;
        ClientRequestGetInfoForMergeData clientRequestGetInfoForMergeData = new ClientRequestGetInfoForMergeData();
        clientRequestGetInfoForMergeData.setProjectName(project);
        clientRequestGetInfoForMergeData.setBranchName(branch);
        clientRequestGetInfoForMergeData.setAppendedPath(path);
        clientRequestGetInfoForMergeData.setFileID(id);

        // Send the request.
        transportProxy.write(clientRequestGetInfoForMergeData);
        try {
            // Wait for the server response.
            wait();
        } catch (InterruptedException e) {
            LOGGER.warn("Server response not received for get info for merge request!!");

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            if (infoForMerge != null) {
                infoForMergeFromServer = infoForMerge;
            }
            setInfoForMerge(null);
        }
        return infoForMergeFromServer;
    }

    /**
     * Resolve conflicts from parent branch. This method is synchronous.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param fileId the file id.
     * @return the results of the attempt to resolve conflicts.
     */
    public synchronized ResolveConflictResults resolveConflictFromParentBranch(String projectName, String branchName, int fileId) {
        ResolveConflictResults resolveConflictResultsFromServer = null;
        ClientRequestResolveConflictFromParentBranchData clientRequestResolveConflictFromParentBranchData = new ClientRequestResolveConflictFromParentBranchData();
        clientRequestResolveConflictFromParentBranchData.setProjectName(projectName);
        clientRequestResolveConflictFromParentBranchData.setBranchName(branchName);
        clientRequestResolveConflictFromParentBranchData.setFileID(fileId);
        // Send the request.
        transportProxy.write(clientRequestResolveConflictFromParentBranchData);
        try {
            // Wait for the server response.
            wait();
        } catch (InterruptedException e) {
            LOGGER.warn("Server response not received!!");

            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            if (resolveConflictResults != null) {
                resolveConflictResultsFromServer = resolveConflictResults;
            }
            setResolveConflictResults(null);
        }
        return resolveConflictResultsFromServer;
    }

    /**
     * Resolve conflicts from parent branch. This method is synchronous.
     *
     * @param userName the user name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param parentBranchName the parent branch name.
     * @param filePromotionInfo file promotion information.
     * @param fileId the file id.
     * @return the results of the attempt to resolve conflicts.
     */
    public PromoteFileResults promoteFile(String userName, String projectName, String branchName, String parentBranchName, FilePromotionInfo filePromotionInfo, int fileId) {
        PromoteFileResults promoteResults = null;
        ClientRequestPromoteFileData clientRequestPromoteFileData = new ClientRequestPromoteFileData();
        clientRequestPromoteFileData.setProjectName(projectName);
        clientRequestPromoteFileData.setBranchName(branchName);
        clientRequestPromoteFileData.setMergedInfoBranchName(archiveDirManagerProxy.getBranchName());
        clientRequestPromoteFileData.setParentBranchName(parentBranchName);
        clientRequestPromoteFileData.setFilePromotionInfo(filePromotionInfo);
        clientRequestPromoteFileData.setFileID(fileId);
        clientRequestPromoteFileData.setUserName(userName);
        // Send the request.
        Object directorySyncObject = archiveDirManagerProxy.getSynchronizationObject();
        synchronized (directorySyncObject) {
            transportProxy.write(clientRequestPromoteFileData);
            try {
                // Wait for the server response.
                directorySyncObject.wait();
            } catch (InterruptedException e) {
                LOGGER.warn("Server response not received!!");

                // Restore interrupted state...
                Thread.currentThread().interrupt();
            } finally {
                ArchiveInfoInterface newArchiveInfo = archiveDirManagerProxy.getArchiveInfo(getShortWorkfileName());
                LogFileProxy newLogFileProxy = (LogFileProxy) newArchiveInfo;
                if (newLogFileProxy.promoteFileResults != null) {
                    promoteResults = newLogFileProxy.promoteFileResults;
                }
                newLogFileProxy.setPromoteFileResults(null);
            }
        }
        return promoteResults;
    }

    /**
     * Update the skinny logfile information for this proxy object.
     * @param skinnyInfo the new skinny logfile information.
     */
    public void setSkinnyLogfileInfo(SkinnyLogfileInfo skinnyInfo) {
        synchronized (syncObject) {
            this.skinnyLogfileInfo = skinnyInfo;
        }
        synchronized (this) {
            logfileInfo = null;
        }
    }
}
