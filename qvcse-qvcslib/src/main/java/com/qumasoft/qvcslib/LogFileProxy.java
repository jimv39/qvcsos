/*   Copyright 2004-2023 Jim Voris
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
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckInData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetForVisualCompareData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetInfoForMergeData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetLogfileInfoData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionForCompareData;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestResolveConflictFromParentBranchData;
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
    public LogfileInfo getLogfileInfo() {
        if (logfileInfo == null) {
            ClientRequestGetLogfileInfoData clientRequest = new ClientRequestGetLogfileInfoData();

            clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
            clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
            clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());
            clientRequest.setShortWorkfileName(getShortWorkfileName());
            clientRequest.setFileID(getFileID());

            // Send the request.
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequest);
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
     * @return a byte array containing the copy of the requested revision, or
     * null if the revision cannot be retrieved.
     */
    @Override
    public byte[] getRevisionAsByteArray(String revisionString) {
        byte[] workfileBuffer;
        workfileBuffer = ClientWorkfileCache.getInstance().getContractedBufferByName(archiveDirManagerProxy.getProjectName(), archiveDirManagerProxy.getBranchName(),
                archiveDirManagerProxy.getAppendedPath(), getShortWorkfileName(), revisionString);
        if (workfileBuffer == null) {
            ClientRequestGetRevisionForCompareData clientRequest = new ClientRequestGetRevisionForCompareData();

            clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
            clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
            clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());
            clientRequest.setRevisionString(revisionString);
            clientRequest.setShortWorkfileName(getShortWorkfileName());
            clientRequest.setFileID(getFileID());

            if (logfileInfo == null) {
                clientRequest.setIsLogfileInfoRequired(true);
            } else {
                clientRequest.setIsLogfileInfoRequired(false);
            }

            try {
                SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequest);
            } finally {
                workfileBuffer = ClientWorkfileCache.getInstance().getContractedBufferByName(archiveDirManagerProxy.getProjectName(),
                        archiveDirManagerProxy.getBranchName(), archiveDirManagerProxy.getAppendedPath(),
                        getShortWorkfileName(), revisionString);
            }
        }
        return workfileBuffer;
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
        clientRequest.setFileID(getFileID());

        clientRequest.setCommandArgs(commandArgs);
        try {
            File checkInFile = new File(checkInFilename);

            if (checkInFile.canRead()) {
                // Need to read the resulting file into a buffer that we can send to the server.
                fileInputStream = new FileInputStream(checkInFile);
                byte[] buffer = new byte[(int) checkInFile.length()];
                Utility.readDataFromStream(buffer, fileInputStream);
                clientRequest.setBuffer(buffer);

                // Save the workfile buffer.
                int cacheIndex = ClientWorkfileCache.getInstance().addBuffer(archiveDirManagerProxy.getProjectName(),
                        archiveDirManagerProxy.getBranchName(), archiveDirManagerProxy.getAppendedPath(),
                        getShortWorkfileName(), buffer);
                clientRequest.setIndex(cacheIndex);

                int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequest);
                ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
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
    public boolean getForVisualCompare(GetRevisionCommandArgs commandLineArgs, String outputFileName) {
        ClientRequestGetForVisualCompareData clientRequest = new ClientRequestGetForVisualCompareData();

        clientRequest.setProjectName(archiveDirManagerProxy.getProjectName());
        clientRequest.setBranchName(archiveDirManagerProxy.getBranchName());
        clientRequest.setAppendedPath(archiveDirManagerProxy.getAppendedPath());
        clientRequest.setFileID(getFileID());

        clientRequest.setCommandArgs(commandLineArgs);
        commandLineArgs.setOutputFileName(outputFileName);

        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequest);
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
        clientRequest.setFileID(getFileID());

        clientRequest.setCommandArgs(commandLineArgs);
        commandLineArgs.setOutputFileName(fetchToFileName);

        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequest);
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
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequest);
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
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
     * Get the info needed for a merge. This is a synchronous round-trip to the server.
     * @param project the project name.
     * @param branch the branch name.
     * @param path the appended path.
     * @param id the file id.
     * @return the info needed for a merge.
     */
    public InfoForMerge getInfoForMerge(String project, String branch, String path, int id) {
        InfoForMerge infoForMergeFromServer = null;
        ClientRequestGetInfoForMergeData clientRequestGetInfoForMergeData = new ClientRequestGetInfoForMergeData();
        clientRequestGetInfoForMergeData.setProjectName(project);
        clientRequestGetInfoForMergeData.setBranchName(branch);
        clientRequestGetInfoForMergeData.setAppendedPath(path);
        clientRequestGetInfoForMergeData.setFileID(id);

        // Send the request.
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestGetInfoForMergeData);
        if (infoForMerge != null) {
            infoForMergeFromServer = infoForMerge;
        }
        setInfoForMerge(null);
        return infoForMergeFromServer;
    }

    /**
     * Resolve conflicts from parent branch. This method is synchronous.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param fileId the file id.
     * @return the results of the attempt to resolve conflicts.
     */
    public ResolveConflictResults resolveConflictFromParentBranch(String projectName, String branchName, int fileId) {
        ResolveConflictResults resolveConflictResultsFromServer = null;
        ClientRequestResolveConflictFromParentBranchData clientRequestResolveConflictFromParentBranchData = new ClientRequestResolveConflictFromParentBranchData();
        clientRequestResolveConflictFromParentBranchData.setProjectName(projectName);
        clientRequestResolveConflictFromParentBranchData.setBranchName(branchName);
        clientRequestResolveConflictFromParentBranchData.setFileID(fileId);
        // Send the request.
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestResolveConflictFromParentBranchData);
        if (resolveConflictResults != null) {
            resolveConflictResultsFromServer = resolveConflictResults;
        }
        setResolveConflictResults(null);
        return resolveConflictResultsFromServer;
    }

    /**
     * Resolve conflicts from parent branch. This method is synchronous.
     *
     * @param userName the user name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param parentBranchName the parent branch name.
     * @param fpi file promotion information.
     * @param fileId the file id.
     * @return the results of the attempt to resolve conflicts.
     */
    public PromoteFileResults promoteFile(String userName, String projectName, String branchName, String parentBranchName, FilePromotionInfo fpi, int fileId) {
        PromoteFileResults promoteResults;
        ClientRequestPromoteFileData clientRequestPromoteFileData = new ClientRequestPromoteFileData();
        clientRequestPromoteFileData.setProjectName(projectName);
        clientRequestPromoteFileData.setBranchName(branchName);
        clientRequestPromoteFileData.setParentBranchName(parentBranchName);
        clientRequestPromoteFileData.setFilePromotionInfo(fpi);
        clientRequestPromoteFileData.setFileID(fileId);
        clientRequestPromoteFileData.setUserName(userName);
        // Send the request.
        PromoteFileResultsHelper promoteFileResultsHelper = Utility.getInstance().getSyncObjectForFileId(fileId);
        LOGGER.debug("<<<<<< Waiting for PromoteFile notify for branch: [{}] appendedPath: [{}] filename: [{}]",
                archiveDirManagerProxy.getBranchName(), archiveDirManagerProxy.getAppendedPath(), fpi.getPromotedToShortWorkfileName());
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, clientRequestPromoteFileData);
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
        promoteResults = promoteFileResultsHelper.getPromoteFileResults();
        LOGGER.debug(">>>>>>> PromoteFile notify received for branch: [{}] appendedPath: [{}] filename: [{}]",
                archiveDirManagerProxy.getBranchName(), archiveDirManagerProxy.getAppendedPath(), fpi.getPromotedToShortWorkfileName());
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

    /**
     * Get the skinnyLogfileInfo.
     *
     * @return the skinnyLogfileInfo.
     */
    public SkinnyLogfileInfo getSkinnyLogfileInfo() {
        return this.skinnyLogfileInfo;
    }

    /**
     * @return the archiveDirManagerProxy
     */
    public ArchiveDirManagerProxy getArchiveDirManagerProxy() {
        return archiveDirManagerProxy;
    }

    @Override
    public Integer getCommitId() {
        Integer commitId;
        synchronized (syncObject) {
            commitId = skinnyLogfileInfo.getCommitId();
        }
        return commitId;
    }
}
