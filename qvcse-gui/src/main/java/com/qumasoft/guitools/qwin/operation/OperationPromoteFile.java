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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.merge.MergeFrame;
import com.qumasoft.guitools.qwin.ProjectTreeControl;
import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.guitools.qwin.QWinUtility;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.PromoteFileResults;
import com.qumasoft.qvcslib.PromotionType;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
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

    /**
     * Operation promote file constructor.
     *
     * @param serverName the server name.
     * @param projectName the project name.
     * @param parentBranch the parent branch name.
     * @param branchName the name of the branch we are promoting.
     * @param remoteProperties the user location properties.
     * @param filePromotionList the list of files to promote.
     */
    public OperationPromoteFile(String serverName, String projectName, String parentBranch, String branchName, RemotePropertiesBaseClass remoteProperties,
                                List<FilePromotionInfo> filePromotionList) {
        super(null, serverName, projectName, parentBranch, remoteProperties);
        this.filePromotionInfoList = filePromotionList;
    }

    @Override
    public void executeOperation() {
        if (filePromotionInfoList != null && !filePromotionInfoList.isEmpty()) {
            final List<FilePromotionInfo> finalFilePromotionInfoList = this.filePromotionInfoList;
            final RemotePropertiesBaseClass projectProperties = ProjectTreeControl.getInstance().getActiveProjectRemoteProperties();
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
                            promoteFile(filePromotionInfo);
                        }
                    } catch (IOException e) {
                        LOGGER.warn(e.getLocalizedMessage(), e);
                    } finally {
                        ClientTransactionManager.getInstance().sendEndTransaction(fTransportProxy, transactionId);
                    }
                }

                void promoteFile(FilePromotionInfo fpi) throws IOException {
                    final String fPromoteToBranchName = fpi.getPromotedToBranchName();
                    final String fPromoteFromBranchName = fpi.getPromotedFromBranchName();
                    MergedInfoInterface mergedInfo = deduceMergedInfo(fpi);
                    PromoteFileResults promoteFileResults;
                    switch (fpi.getTypeOfPromotion()) {
                        case SIMPLE_PROMOTION_TYPE -> {
                            LOGGER.info("Simple promotion for: [{}]", fpi.getPromotedToShortWorkfileName());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, fpi,
                                    fpi.getFileId());
                            if (promoteFileResults != null) {
                                processPromoteFileResults(promoteFileResults, fpi);
                            } else {
                                LOGGER.info("Simple promotion produced NULL promoteFileResults!!!!!!!!");
                            }
                        }
                        case FILE_NAME_CHANGE_PROMOTION_TYPE -> {
                            LOGGER.info("Changing name from: [{}] to [{}]", fpi.getPromotedToShortWorkfileName(), fpi.getPromotedFromShortWorkfileName());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, fpi,
                                    fpi.getFileId());
                            if (promoteFileResults != null) {
                                processPromoteFileResults(promoteFileResults, fpi);
                            }
                        }
                        case FILE_LOCATION_CHANGE_PROMOTION_TYPE -> {
                            LOGGER.info("Changing location for: [{}] from [{}] to [{}]", mergedInfo.getShortWorkfileName(), fpi.getPromotedToAppendedPath(),
                                    fpi.getPromotedFromAppendedPath());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, fpi,
                                    fpi.getFileId());
                            if (promoteFileResults != null) {
                                processPromoteFileResults(promoteFileResults, fpi);
                            }
                        }
                        case LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE -> {
                            LOGGER.info("Changing name from: [{}] to [{}]", fpi.getPromotedToShortWorkfileName(), fpi.getPromotedFromShortWorkfileName());
                            LOGGER.info("Changing location for: [{}] from [{}] to [{}]", mergedInfo.getShortWorkfileName(), fpi.getPromotedToAppendedPath(),
                                    fpi.getPromotedFromAppendedPath());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, fpi,
                                    fpi.getFileId());
                            if (promoteFileResults != null) {
                                processPromoteFileResults(promoteFileResults, fpi);
                            }
                        }
                        case FILE_CREATED_PROMOTION_TYPE -> {
                            LOGGER.info("Create promotion for: [{}]", fpi.getPromotedFromShortWorkfileName());
                            promoteFileResults = mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, fpi,
                                    fpi.getFileId());
                            if (promoteFileResults != null) {
                                String workfileBase = getRemoteProperties().getWorkfileLocation(getServerName(), getProjectName(), fpi.getPromotedToBranchName());
                                if (promoteFileResults.getMergedResultBuffer() != null) {
                                    // Check for existing non-controlled file of the same name.
                                    boolean deletedFlag = checkForAndMaybeDeleteParentBranchWorkfile(fpi, fpi.getPromotedFromShortWorkfileName());

                                    // Write this to the workfile location for this file, as it is our best guess at a merged result
                                    String promotedToAppendedPath = fpi.getPromotedToAppendedPath();
                                    String promotedToShortWorkfileName = deduceNameOfWorkfileForCreatePromotion(deletedFlag, fpi.getPromotedFromShortWorkfileName());
                                    writeMergedResultToWorkfile(promotedToAppendedPath, promotedToShortWorkfileName, workfileBase, promoteFileResults.getMergedResultBuffer());
                                }
                                QWinFrame.getQWinFrame().refreshCurrentBranch();
                            }
                        }
                        case FILE_DELETED_PROMOTION_TYPE -> {
                            LOGGER.info("Promoting deletion of file: [{}]", fpi.getPromotedToShortWorkfileName());
                            mergedInfo.promoteFile(getProjectName(), fPromoteFromBranchName, fPromoteToBranchName, fpi,
                                    fpi.getFileId());
                            checkForAndMaybeDeleteParentBranchWorkfile(fpi, fpi.getPromotedToShortWorkfileName());


                            QWinFrame.getQWinFrame().refreshCurrentBranch();
                        }
                        default -> // Not supported yet.
                            throw new UnsupportedOperationException("Not supported yet.");
                    }
                }
            };

            // Put all this on a separate worker thread.
            new Thread(later).start();
        }
    }

    private MergedInfoInterface deduceMergedInfo(FilePromotionInfo fpi) {
        String workfileBase = getRemoteProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
        String promotedFromAppendedPath = fpi.getPromotedFromAppendedPath();
        String promotedFromBranchName = fpi.getPromotedFromBranchName();
        String workfileDirectory;
        if (promotedFromAppendedPath.length() > 0) {
            workfileDirectory = workfileBase + File.separator + promotedFromAppendedPath;
        } else {
            workfileDirectory = workfileBase;
        }

        // Need to build the directory manager from scratch, since there is no guarantee that it has been built yet.
        DirectoryManagerInterface directoryManager;
        switch (fpi.getTypeOfPromotion()) {
            case SIMPLE_PROMOTION_TYPE:
            case FILE_NAME_CHANGE_PROMOTION_TYPE:
            case FILE_CREATED_PROMOTION_TYPE:
            case FILE_LOCATION_CHANGE_PROMOTION_TYPE:
            case LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE:
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), promotedFromBranchName, promotedFromAppendedPath);
                directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), getServerName(),
                        directoryCoordinate, workfileDirectory, null, false, true);
                break;
            default:
                DirectoryCoordinate defaultDirectoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), fpi.getPromotedFromAppendedPath());
                directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), getServerName(),
                        defaultDirectoryCoordinate, workfileDirectory, null, false, true);
                break;
        }
        MergedInfoInterface mergedInfo = directoryManager.getMergedInfoByFileId(fpi.getFileId());

        return mergedInfo;
    }

    private void processPromoteFileResults(PromoteFileResults promoteFileResults, FilePromotionInfo fpi) throws IOException {
        boolean overlapDetectedFlag = false;
        String promotedToWorkfileBase = getRemoteProperties().getWorkfileLocation(getServerName(), getProjectName(), fpi.getPromotedToBranchName());
        LOGGER.debug("=======> promoted to workfile base: [{}] promoted to branch: [{}] filename: [{}]", promotedToWorkfileBase, fpi.getPromotedToBranchName(), fpi.getPromotedToShortWorkfileName());
        WorkFile commonAncestorWorkFile = null;
        WorkFile branchTipWorkFile = null;
        WorkFile parentTipWorkFile = null;
        final String fPromotedToAppendedPath = fpi.getPromotedToAppendedPath();
        final String fPromotedToShortWorkfileName = fpi.getPromotedToShortWorkfileName();
        final String fPromotedFromShortWorkfileName = fpi.getPromotedFromShortWorkfileName();
        final String fPromotedFromAppendedPath = fpi.getPromotedFromAppendedPath();

        if (promoteFileResults.getMergedResultBuffer() != null) {
            // Delete the existing workfile. We'll overwrite it, or write the new workfile where ever it belongs.
            // Note that it is 'safe' to do this because we don't allow a promotion unless the status on the parent
            // branch is 'Current', which means that any parent branch revisions are already captured in the database.
            checkForAndMaybeDeleteParentBranchWorkfile(fpi, fPromotedToShortWorkfileName);

            // Write the promoted file to the workfile location for this file, as it is our best guess at a merged result
            writeMergedResultToWorkfile(fPromotedFromAppendedPath, fPromotedFromShortWorkfileName, promotedToWorkfileBase, promoteFileResults.getMergedResultBuffer());
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
            String fullWorkfileName = constructFullWorkfileName(fpi.getPromotedToAppendedPath(), fpi.getPromotedFromShortWorkfileName(), promotedToWorkfileBase);
            final String fOutputFileName = fullWorkfileName;
            final FilePromotionInfo fFilePromotionInfo = fpi;
            final String fParentBranchDefaultRevisionString = String.format("%d.%d", fpi.getPromotedToBranchId(), promoteFileResults.getParentBranchTipRevisionId());
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

    /**
     * Check for the existence of the parent branch workfile. If it exists, delete it <i>if</i> we are not
     * doing a create promotion. In the case where we <i>are</i> doing a create promotion, then return false to indicate
     * that we did <i>not</i> delete the parent branch workfile.
     * @param fpi the file promotion info.
     * @param shortWorkfileName the short workfile name that we maybe will delete.
     * @return true is we deleted the named workfile; false if we did not delete the named workfile.
     */
    boolean checkForAndMaybeDeleteParentBranchWorkfile(FilePromotionInfo fpi, String shortWorkfileName) {
        boolean flag = true;
        String promotedToWorkfileBase = getRemoteProperties().getWorkfileLocation(getServerName(), getProjectName(), fpi.getPromotedToBranchName());
        final String fPromotedToAppendedPath = fpi.getPromotedToAppendedPath();
        final String fPromotedToShortWorkfileName = fpi.getPromotedToShortWorkfileName();

        // Delete the existing workfile.
        String fullExistingWorkfileName;
        if (fPromotedToAppendedPath.length() > 0) {
            fullExistingWorkfileName = String.format("%s%s%s%s%s", promotedToWorkfileBase, File.separator, fPromotedToAppendedPath, File.separator, shortWorkfileName);
        } else {
            fullExistingWorkfileName = String.format("%s%s%s", promotedToWorkfileBase, File.separator, shortWorkfileName);
        }
        LOGGER.info("Full promoted to workfile name that we will delete: [{}]", fullExistingWorkfileName);
        File oldWorkfile = new File(fullExistingWorkfileName);
        if (0 == fpi.getTypeOfPromotion().compareTo(PromotionType.FILE_CREATED_PROMOTION_TYPE)) {
            // If there is an uncontrolled file with the same name, we have to abandon the promotion so we don't overwrite the existing workfile of the same name.
            if (oldWorkfile.exists()) {
                LOGGER.warn("Workfile of the same name that already exists on parent branch not overwritten: [{}]", fullExistingWorkfileName);
                flag = false;
            }
        } else {
            if (oldWorkfile.exists()) {
                oldWorkfile.delete();
            }
        }
        return flag;
    }

    private String deduceNameOfWorkfileForCreatePromotion(boolean deletedFlag, String promotedFromShortWorkfileName) {
        String deducedName = promotedFromShortWorkfileName;
        if (!deletedFlag) {
            // We want the non-overlapping name to look like originalName-promoted
            // For example, foo.java becomes foo.java-promoted
            deducedName = promotedFromShortWorkfileName + "-promoted";
        }
        return deducedName;
    }
}
