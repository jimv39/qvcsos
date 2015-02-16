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
package com.qumasoft.guitools.qwin.operation;

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
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import java.io.File;
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
     * @param viewName the view name.
     * @param branchName the name of the branch we are promoting.
     * @param userLocationProperties the user location properties.
     * @param filePromotionList the list of files to promote.
     */
    public OperationPromoteFile(String serverName, String projectName, String viewName, String branchName, UserLocationProperties userLocationProperties,
                                List<FilePromotionInfo> filePromotionList) {
        super(null, serverName, projectName, viewName, userLocationProperties);
        this.filePromotionInfoList = filePromotionList;
        this.parentBranchName = viewName;
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
                        finalFilePromotionInfoList.stream().forEach((filePromotionInfo) -> {
                            try {
                                promoteFile(projectProperties, filePromotionInfo);
                            } catch (QVCSException e) {
                                LOGGER.warn(e.getLocalizedMessage(), e);
                            }
                        });
                    } catch (Exception e) {
                        LOGGER.warn(e.getLocalizedMessage(), e);
                    } finally {
                        ClientTransactionManager.getInstance().sendEndTransaction(fTransportProxy, transactionId);
                    }
                }

                void promoteFile(AbstractProjectProperties projectProperties, FilePromotionInfo filePromotionInfo) throws QVCSException {
                    final String finalParentBranchName = getParentBranchName();
                    final String finalBranchToPromoteFrom = getBranchToPromoteFrom();
                    MergedInfoInterface mergedInfo = deduceMergedInfo(projectProperties, filePromotionInfo);
                    PromoteFileResults promoteFileResults;
                    switch (filePromotionInfo.getTypeOfMerge()) {
                        case SIMPLE_MERGE_TYPE:
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), finalBranchToPromoteFrom, finalParentBranchName, filePromotionInfo,
                                    filePromotionInfo.getFileId());
                            if (promoteFileResults != null) {
                                boolean overlapDetectedFlag = false;
                                String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
                                if (promoteFileResults.getMergedResultBuffer() != null) {
                                    // Write this to the workfile location for this file, as it is our best guess at a merged result
                                    writeMergedResultToWorkfile(mergedInfo, workfileBase, promoteFileResults.getMergedResultBuffer());
                                }
                                if (promoteFileResults.getBranchTipRevisionBuffer() != null) {
                                    writeConflictFile(mergedInfo, "branchTip", workfileBase, promoteFileResults.getBranchTipRevisionBuffer());
                                    overlapDetectedFlag = true;
                                }
                                if (promoteFileResults.getCommonAncestorBuffer() != null) {
                                    writeConflictFile(mergedInfo, "commonAncestor", workfileBase, promoteFileResults.getCommonAncestorBuffer());
                                    overlapDetectedFlag = true;
                                }
                                if (overlapDetectedFlag) {
                                    writeConflictFile(mergedInfo, "branchParentTip", workfileBase, promoteFileResults.getBranchParentTipRevisionBuffer());
                                }
                                QWinFrame.getQWinFrame().refreshCurrentView();
                                if (overlapDetectedFlag) {
                                    // TODO -- Ideally, we should automatically launch the visual merge tool here.
                                    StringBuilder stringBuffer = new StringBuilder();
                                    stringBuffer.append("Overlap detected when merging [").append(mergedInfo.getShortWorkfileName())
                                            .append("]. You will need to perform a manual merge.");
                                    final String message = stringBuffer.toString();
                                    QWinUtility.logProblem(message);

                                    // Show the message box on the Swing thread.
                                    Runnable later = () -> {
                                        // Let the user know they'll need to perform the merge manually.
                                        JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), message, "Merge Overlap Detected", JOptionPane.PLAIN_MESSAGE);
                                    };
                                    SwingUtilities.invokeLater(later);
                                }
                            }
                            break;
                        case CHILD_CREATED_MERGE_TYPE:
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), finalBranchToPromoteFrom, finalParentBranchName, filePromotionInfo,
                                    filePromotionInfo.getFileId());
                            if (promoteFileResults != null) {
                                String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
                                if (promoteFileResults.getMergedResultBuffer() != null) {
                                    // Write this to the workfile location for this file, as it is our best guess at a merged result
                                    writeMergedResultToWorkfile(mergedInfo, workfileBase, promoteFileResults.getMergedResultBuffer());
                                }
                            }
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

    private MergedInfoInterface deduceMergedInfo(AbstractProjectProperties projectProperties, FilePromotionInfo filePromotionInfo) throws QVCSException {
        String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
        String appendedPath = filePromotionInfo.getAppendedPath();
        String workfileDirectory;
        if (appendedPath.length() > 0) {
            workfileDirectory = workfileBase + File.separator + appendedPath;
        } else {
            workfileDirectory = workfileBase;
        }

        // Need to build the directory manager from scratch, since there is no guarantee that it has been built yet.
        DirectoryManagerInterface directoryManager;
        switch (filePromotionInfo.getTypeOfMerge()) {
            case CHILD_CREATED_MERGE_TYPE:
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), branchToPromoteFrom, filePromotionInfo.getAppendedPath());
                directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(getServerName(), directoryCoordinate,
                        QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, projectProperties, workfileDirectory, null, false);
                break;
            default:
                DirectoryCoordinate defaultDirectoryCoordinate = new DirectoryCoordinate(getProjectName(), getViewName(), filePromotionInfo.getAppendedPath());
                directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(getServerName(), defaultDirectoryCoordinate,
                        QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, projectProperties, workfileDirectory, null, false);
                break;
        }
        MergedInfoInterface mergedInfo = directoryManager.getMergedInfo(filePromotionInfo.getShortWorkfileName());

        return mergedInfo;
    }
}
