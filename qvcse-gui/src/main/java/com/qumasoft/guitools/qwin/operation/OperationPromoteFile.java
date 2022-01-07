/*   Copyright 2004-2021 Jim Voris
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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.merge.MergeFrame;
import com.qumasoft.guitools.qwin.ProjectTreeControl;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.QWinUtility;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.PromoteFileResults;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.WorkFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Promote a file.
 *
 * @author Jim Voris
 */
public class OperationPromoteFile extends OperationBaseClass {
    // Create our logger object

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationPromoteFile.class);
    private final List<FilePromotionInfo> filePromotionInfoList;
    private final String parentBranchName;
    private final String branchToPromoteFrom;

    /**
     * Operation promote file constructor.
     *
     * @param serverName the server name.
     * @param projectName the project name.
     * @param parentBranch the parent branch name.
     * @param branchName the name of the branch we are promoting.
     * @param userLocationProperties the user location properties.
     * @param filePromotionList the list of files to promote.
     */
    public OperationPromoteFile(String serverName, String projectName, String parentBranch, String branchName, UserLocationProperties userLocationProperties,
                                List<FilePromotionInfo> filePromotionList) {
        super(null, serverName, projectName, parentBranch, userLocationProperties);
        this.filePromotionInfoList = filePromotionList;
        this.parentBranchName = parentBranch;
        this.branchToPromoteFrom = branchName;
    }

    @Override
    public void executeOperation() {
        if (filePromotionInfoList != null && filePromotionInfoList.size() > 0) {
            final List<FilePromotionInfo> finalFilePromotionInfoList = this.filePromotionInfoList;
            final AbstractProjectProperties projectProperties = ProjectTreeControl.getInstance().getActiveProject();
            final TransportProxyInterface fTransportProxy = TransportProxyFactory.getInstance().getTransportProxy(QWinFrame.getQWinFrame().getActiveServerProperties());
            if (fTransportProxy != null) {
                LOGGER.info("=========== Transport proxy transport name: [" + fTransportProxy.getTransportName() + "] user name: ["
                        + fTransportProxy.getUsername() + "]");
            } else {
                LOGGER.warn("null value for transport proxy!!!");
            }

            Runnable later = new Runnable() {

                @Override
                public void run() {
                    int transactionId = ClientTransactionManager.getInstance().sendBeginTransaction(fTransportProxy);
                    try {
                        for (FilePromotionInfo filePromotionInfo : finalFilePromotionInfoList) {
                            promoteFile(projectProperties, filePromotionInfo);
                        }
                    } catch (Exception e) {
                        LOGGER.warn(e.getLocalizedMessage(), e);
                    } finally {
                        ClientTransactionManager.getInstance().sendEndTransaction(fTransportProxy, transactionId);
                    }
                }

                void promoteFile(AbstractProjectProperties projectProperties, FilePromotionInfo filePromotionInfo) throws IOException {
                    final String fPromoteToBranchName = getParentBranchName();
                    final String fPromoteFromBranchName = getBranchToPromoteFrom();
                    MergedInfoInterface mergedInfo = deduceMergedInfo(projectProperties, filePromotionInfo);
                    PromoteFileResults promoteFileResults;
                    switch (filePromotionInfo.getTypeOfPromotion()) {
                        case SIMPLE_PROMOTION_TYPE:
                            LOGGER.info("Simple promotion for: [{}]", filePromotionInfo.getPromotedToShortWorkfileName());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, filePromotionInfo,
                                    filePromotionInfo.getFileId());
                            if (promoteFileResults != null) {
                                processPromoteFileResults(promoteFileResults, filePromotionInfo);
                            }
                            break;
                        case FILE_NAME_CHANGE_PROMOTION_TYPE:
                            LOGGER.info("Changing name from: [{}] to [{}]", filePromotionInfo.getPromotedToShortWorkfileName(), filePromotionInfo.getPromotedFromShortWorkfileName());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, filePromotionInfo,
                                    filePromotionInfo.getFileId());
                            if (promoteFileResults != null) {
                                processPromoteFileResults(promoteFileResults, filePromotionInfo);
                            }
                            break;
                        case FILE_LOCATION_CHANGE_PROMOTION_TYPE:
                            LOGGER.info("Changing location for: [{}] from [{}] to [{}]", mergedInfo.getShortWorkfileName(), filePromotionInfo.getPromotedToAppendedPath(),
                                    filePromotionInfo.getPromotedFromAppendedPath());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, filePromotionInfo,
                                    filePromotionInfo.getFileId());
                            if (promoteFileResults != null) {
                                processPromoteFileResults(promoteFileResults, filePromotionInfo);
                            }
                            break;
                        case LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE:
                            LOGGER.info("Changing name from: [{}] to [{}]", filePromotionInfo.getPromotedToShortWorkfileName(), filePromotionInfo.getPromotedFromShortWorkfileName());
                            LOGGER.info("Changing location for: [{}] from [{}] to [{}]", mergedInfo.getShortWorkfileName(), filePromotionInfo.getPromotedToAppendedPath(),
                                    filePromotionInfo.getPromotedFromAppendedPath());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, filePromotionInfo,
                                    filePromotionInfo.getFileId());
                            if (promoteFileResults != null) {
                                processPromoteFileResults(promoteFileResults, filePromotionInfo);
                            }
                            break;
                        case FILE_CREATED_PROMOTION_TYPE:
                            LOGGER.info("Create promotion for: [{}]", filePromotionInfo.getPromotedFromShortWorkfileName());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, filePromotionInfo,
                                    filePromotionInfo.getFileId());
                            if (promoteFileResults != null) {
                                String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), filePromotionInfo.getPromotedToBranchName());
                                if (promoteFileResults.getMergedResultBuffer() != null) {
                                    // Write this to the workfile location for this file, as it is our best guess at a merged result
                                    String promotedToAppendedPath = filePromotionInfo.getPromotedToAppendedPath();
                                    String promotedToShortWorkfileName = filePromotionInfo.getPromotedFromShortWorkfileName();
                                    writeMergedResultToWorkfile(promotedToAppendedPath, promotedToShortWorkfileName, workfileBase, promoteFileResults.getMergedResultBuffer());
                                }
                                QWinFrame.getQWinFrame().refreshCurrentBranch();
                            }
                            break;
                        case FILE_DELETED_PROMOTION_TYPE:
                            LOGGER.info("Promoting deletion of file: [{}]", filePromotionInfo.getPromotedFromShortWorkfileName());
                            mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, filePromotionInfo,
                                    filePromotionInfo.getFileId());
                            QWinFrame.getQWinFrame().refreshCurrentBranch();
                            break;
                        default:
                            // Not supported yet.
                            throw new UnsupportedOperationException("Not supported yet.");
                    }
                }
            };
            // Put all this on a separate worker thread.
            new Thread(later).start();
        }
    }

    String getParentBranchName() {
        return this.parentBranchName;
    }

    String getBranchToPromoteFrom() {
        return this.branchToPromoteFrom;
    }

    private MergedInfoInterface deduceMergedInfo(AbstractProjectProperties projectProperties, FilePromotionInfo filePromotionInfo) {
        String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
        String appendedPath = filePromotionInfo.getPromotedFromAppendedPath();
        String workfileDirectory;
        if (appendedPath.length() > 0) {
            workfileDirectory = workfileBase + File.separator + appendedPath;
        } else {
            workfileDirectory = workfileBase;
        }

        // Need to build the directory manager from scratch, since there is no guarantee that it has been built yet.
        DirectoryManagerInterface directoryManager;
        switch (filePromotionInfo.getTypeOfPromotion()) {
            case SIMPLE_PROMOTION_TYPE:
            case FILE_NAME_CHANGE_PROMOTION_TYPE:
            case FILE_CREATED_PROMOTION_TYPE:
            case FILE_LOCATION_CHANGE_PROMOTION_TYPE:
            case LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE:
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), branchToPromoteFrom, filePromotionInfo.getPromotedFromAppendedPath());
                directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), getServerName(),
                        directoryCoordinate, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, projectProperties, workfileDirectory, null, false);
                break;
            default:
                DirectoryCoordinate defaultDirectoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), filePromotionInfo.getPromotedFromAppendedPath());
                directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), getServerName(),
                        defaultDirectoryCoordinate, QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, projectProperties, workfileDirectory, null, false);
                break;
        }
        MergedInfoInterface mergedInfo = directoryManager.getMergedInfoByFileId(filePromotionInfo.getFileId());

        return mergedInfo;
    }

    private void processPromoteFileResults(PromoteFileResults promoteFileResults, FilePromotionInfo filePromotionInfo) throws IOException {
        boolean overlapDetectedFlag = false;
        String promotedToWorkfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), filePromotionInfo.getPromotedToBranchName());
        WorkFile commonAncestorWorkFile = null;
        WorkFile branchTipWorkFile = null;
        WorkFile parentTipWorkFile = null;
        final String fPromotedToAppendedPath = filePromotionInfo.getPromotedFromAppendedPath();
        final String fPromotedToShortWorkfileName = filePromotionInfo.getPromotedToShortWorkfileName();
        final String fPromotedFromShortWorkfileName = filePromotionInfo.getPromotedFromShortWorkfileName();

        if (promoteFileResults.getMergedResultBuffer() != null) {
            // Write this to the workfile location for this file, as it is our best guess at a merged result
            writeMergedResultToWorkfile(fPromotedToAppendedPath, fPromotedFromShortWorkfileName, promotedToWorkfileBase, promoteFileResults.getMergedResultBuffer());
        } else {
            overlapDetectedFlag = true;
            if (promoteFileResults.getBranchTipRevisionBuffer() != null) {
                String branchTip = String.format("branchTip.%d", promoteFileResults.getFeatureBranchTipRevisionId());
                branchTipWorkFile = writeConflictFile(fPromotedToAppendedPath, fPromotedToShortWorkfileName, branchTip, promotedToWorkfileBase, promoteFileResults.getBranchTipRevisionBuffer());
            }
            if (promoteFileResults.getCommonAncestorBuffer() != null) {
                String commonAncestor = String.format("commonAncestor.%d", promoteFileResults.getCommonAncestorRevisionId());
                commonAncestorWorkFile = writeConflictFile(fPromotedToAppendedPath, fPromotedToShortWorkfileName, commonAncestor, promotedToWorkfileBase,
                        promoteFileResults.getCommonAncestorBuffer());
            }
            if (promoteFileResults.getBranchParentTipRevisionBuffer() != null) {
                String parentTip = String.format("parentTip.%d", promoteFileResults.getParentBranchTipRevisionId());
                parentTipWorkFile = writeConflictFile(fPromotedToAppendedPath, fPromotedToShortWorkfileName, parentTip, promotedToWorkfileBase,
                        promoteFileResults.getBranchParentTipRevisionBuffer());
            }
        }
        QWinFrame.getQWinFrame().refreshCurrentBranch();
        if (overlapDetectedFlag) {
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append("Overlap detected when merging [").append(fPromotedToShortWorkfileName)
                    .append("]. You will need to perform a manual merge.");
            final String message = stringBuffer.toString();
            QWinUtility.logMessage(message);

            if (commonAncestorWorkFile == null || branchTipWorkFile == null || parentTipWorkFile == null) {
                throw new QVCSRuntimeException("Unexpected null value.");
            }
            final String fCommonAncestorWorkFileName = commonAncestorWorkFile.getCanonicalPath();
            final String fBranchTipWorkFileName = branchTipWorkFile.getCanonicalPath();
            final String fParentTipWorkFileName = parentTipWorkFile.getCanonicalPath();
            String fullWorkfileName = constructFullWorkfileName(filePromotionInfo.getPromotedToAppendedPath(), filePromotionInfo.getPromotedFromShortWorkfileName(), promotedToWorkfileBase);
            final String fOutputFileName = fullWorkfileName;
            final FilePromotionInfo fFilePromotionInfo = filePromotionInfo;
            final String fParentBranchDefaultRevisionString = String.format("%d.%d", filePromotionInfo.getPromotedToBranchId(), promoteFileResults.getParentBranchTipRevisionId());
            final WorkFile fPromotedToWorkfile = new WorkFile(fullWorkfileName);

            // Show the message box on the Swing thread.
            Runnable laterOnSwing = () -> {
                // Let the user know they'll need to perform the merge manually.
                JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), message, "Merge Overlap Detected", JOptionPane.PLAIN_MESSAGE);

                // Launch the visual merge tool here.
                MergeFrame mergeFrame = new MergeFrame(QWinFrame.getQWinFrame());
                try {
                    mergeFrame.mergeFiles(fCommonAncestorWorkFileName,
                            fParentTipWorkFileName, fPromotedToShortWorkfileName + " Parent Branch Revision: " + fParentBranchDefaultRevisionString,
                            fBranchTipWorkFileName, fPromotedFromShortWorkfileName + " Child Branch Revision: " + fFilePromotionInfo.getChildBranchTipRevisionString(),
                            fOutputFileName, "Merged Result: " + fPromotedFromShortWorkfileName,
                            null, fPromotedToWorkfile, null, null);
                } catch (IOException | QVCSOperationException ex) {
                    QWinUtility.warnProblem(ex.getLocalizedMessage());
                }
            };
            SwingUtilities.invokeLater(laterOnSwing);
        }
    }
}
