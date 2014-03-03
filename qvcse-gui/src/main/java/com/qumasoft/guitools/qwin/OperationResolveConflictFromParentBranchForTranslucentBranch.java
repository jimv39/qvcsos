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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.InfoForMerge;
import com.qumasoft.qvcslib.MergeType;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ResolveConflictResults;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Operation resolve conflict from parent branch for a translucent branch. This operation wrappers the work that needs to be done in order to merge changes that have been made on
 * the parent view (typically the trunk) into the current translucent branch. The operation should only be called when the set of files are all current, marked as <b>overlapped
 * (a.k.a. conflict).</b>
 *
 * @author Jim Voris
 */
public class OperationResolveConflictFromParentBranchForTranslucentBranch extends OperationBaseClass {

    /**
     * Create a resolve conflict from parent branch for a translucent branch operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    OperationResolveConflictFromParentBranchForTranslucentBranch(JTable fileTable, String serverName, String projectName, String viewName,
                                                                 UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    void executeOperation() {
        if (getFileTable() != null) {
            final List<MergedInfoInterface> mergedInfoArray = getSelectedFiles();
            // Run the update on the Swing thread.
            Runnable later = new Runnable() {

                @Override
                public void run() {
                    for (MergedInfoInterface mergedInfo : mergedInfoArray) {
                        if (mergedInfo.getStatusIndex() != MergedInfoInterface.CURRENT_STATUS_INDEX) {
                            QWinUtility.logProblem(Level.SEVERE, "Invalid status for resolve conflict.");
                            continue;
                        }
                        if (!mergedInfo.getIsOverlap()) {
                            QWinUtility.logProblem(Level.SEVERE, "No conflict found for [" + mergedInfo.getShortWorkfileName()
                                    + "]. You cannot resolve a conflict that does not exist.");
                            continue;
                        }
                        if (!mergedInfo.getAttributes().getIsBinaryfile()) {
                            try {
                                resolveConflictFromParentBranch(mergedInfo);
                            } catch (IOException | QVCSException e) {
                                QWinUtility.logProblem(Level.SEVERE, Utility.expandStackTraceToString(e));
                            }
                        } else {
                            // TODO -- A binary file. We should ask the user if it is okay to just take the latest from the trunk... basically, there is no way for us
                            // to perform the merge. All we can do is have the user take the latest from the trunk and then perform a manual merge based on what is there.
                            // For example, suppose we're dealing with a Word document. We can't merge the trunk changes to our branch, but the user can do that. What we
                            // should do is copy the trunk version of the binary file, check that in as the 'tip' of the branch and re-anchor the file to the trunk's tip
                            // revision, and then (actually before we do the trunk work) rename the branch copy of the file so that is not lost. The net result will be two
                            // files -- or maybe I should have 3 files: the correctly named file would be the copy from the trunk; one other workfile only would be the
                            // trunk's common ancestor file; the 2nd workfile would be the branch's tip file. This user would then need to manually edit the trunk copy
                            // to apply the edits from the branch. They would also have the common ancestor for comparison to help them decide what the valid changes
                            // are.... alternately, we could have the same effect without doing a checkin by simply removing the branch label from the file -- in that absent
                            // the branch label, the translucent branch view treats the trunk tip as the tip revision of the branch. We would still need the 3 files:
                            // 1 from trunk tip; 1 from trunk common ancestor (workfile only), 1 from branch tip (workfile only).
                            //
                            // What to do for binary files that have:
                            //  1. Been renamed?
                            //  2. Been moved?
                            //  3. Been moved and renamed?
                            //  4. Been deleted?
                            QWinUtility.logProblem(Level.INFO, "There is no support for merging binary files.");
                        }
                    }
                }
            };
            SwingUtilities.invokeLater(later);
        }
    }

    /**
     * <p>
     * Merge the changes from the parent branch to the file on this branch. The file on this branch <i>must</i> have a status of current. If all goes well, the result of the merge
     * will move the branch label for this file to the tip of the parent branch, apply the edits from the parent branch to this branch's workfile, and leave that workfile ready to
     * be checked in. Note that we do
     * <i>not</i> check-in the results of the merge -- we leave that to the user, presumably after they verify the results of the merge.</p>
     * <p>
     * This routine does have the effect of altering the archive file so that the branch label is removed from archive so that a check-in will re-anchor the branch to a
     * branch-point off the tip of the parent branch, and as a result of that, the 'overlap' (a.k.a. conflict) should disappear, since there are no longer any edits on the parent
     * branch that have not been applied to the child branch.</p>
     *
     * @param mergedInfo the merged info for the file we are working with. This mergedInfo is the one for the translucent branch, i.e. the one that show overlap. It is <i>not</i>
     * the mergedInfo from the parent branch!
     * @throws IOException thrown if we have problems reading/writing files.
     * @throws QVCSException if we cannot figure out the merge type.
     */
    private void resolveConflictFromParentBranch(MergedInfoInterface mergedInfo) throws IOException, QVCSException {
        // First, need to decide kind of merge we will have to perform.
        MergeType typeOfMerge = deduceTypeOfMerge(mergedInfo);
        switch (typeOfMerge) {
            // 0000 -- no renames or moves.
            case SIMPLE_MERGE_TYPE:
                performSimpleMerge(mergedInfo);
                break;
            // 0001 -- parent rename
            case PARENT_RENAMED_MERGE_TYPE:
                performParentRenamedMerge(mergedInfo);
                break;
            // 0010 -- parent move
            case PARENT_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for parent moved merge.
                break;
            // 0011 -- parent rename && parent move
            case PARENT_RENAMED_AND_PARENT_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for parent renamed and parent moved merge.
                break;
            // 0100 -- branch rename
            case CHILD_RENAMED_MERGE_TYPE:
                // TODO -- resolve conflict for child renamed merge.
                break;
            // 0101 -- branch rename && parent rename
            case PARENT_RENAMED_AND_CHILD_RENAMED_MERGE_TYPE:
                // TODO -- resolve conflict for parent renamed and child renamed merge.
                break;
            // 0110 -- parent move && branch rename
            case PARENT_MOVED_AND_CHILD_RENAMED_MERGE_TYPE:
                // TODO -- resolve conflict for parent moved and child renamed merge.
                break;
            // 0111 -- parent rename && parent move && branch rename
            case PARENT_RENAMED_AND_PARENT_MOVED_AND_CHILD_RENAMED_MERGE_TYPE:
                // TODO -- resolve conflict for parent renamed and child renamed merge.
                break;
            // 1000 -- branch move
            case CHILD_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for child moved merge.
                break;
            // 1001 -- branch move && parent rename
            case PARENT_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for parent renamed child moved merge.
                break;
            // 1010 -- branch move && parent move
            case PARENT_MOVED_AND_CHILD_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for parent moved child moved merge.
                break;
            // 1011 -- branch move && parent move && parent rename
            case PARENT_MOVED_AND_PARENT_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for parent moved and parent renamed and child moved merge.
                break;
            // 1100 -- branch move && branch rename
            case CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for child renamed and child moved merge.
                break;
            // 1101 -- branch move && branch rename && parent rename
            case PARENT_RENAMED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for parent renamed and child renamed and child moved merge.
                break;
            // 1110 -- branch move && branch rename && parent move
            case PARENT_MOVED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for parent moved and child renamed and child moved merge.
                break;
            // 1111 -- branch move && branch rename && parent move && parent rename
            case PARENT_RENAMED_AND_PARENT_MOVED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                // TODO -- resolve conflict for parent renamed and parent moved and child renamed and child moved merge.
                break;
            case PARENT_DELETED_MERGE_TYPE:
                // TODO -- resolve conflict for parent deleted merge.
                break;
            case CHILD_DELETED_MERGE_TYPE:
                // TODO -- resolve conflict for child deleted merge.
                break;
            default:
                break;
        }
    }

    /**
     * Deduce the type of merge we need to perform.
     *
     * @param mergedInfo the merged info for the translucent branch's file.
     * @return
     */
    private MergeType deduceTypeOfMerge(MergedInfoInterface mergedInfo) throws QVCSException {
        InfoForMerge infoForMerge = mergedInfo.getInfoForMerge(getProjectName(), getViewName(), mergedInfo.getArchiveDirManager().getAppendedPath());
        MergeType typeOfMerge = Utility.deduceTypeOfMerge(infoForMerge, mergedInfo.getShortWorkfileName());
        return typeOfMerge;
    }

    /**
     * <p>
     * The 'happy' path case where the parent branch's file is a text file, and we can just do a merge of the trunk to the branch. To do that, we should:</p> <ol> <li>Request the
     * server to perform the merge, passing the branch info, and file id, etc.</li> <li>The server tries an auto-merge in a scratch area. If it succeeds, it takes care of updating
     * the archive by removing the branch label, and sends the merged result file back to the client, where it should produce a status of 'Your copy changed'. The client will have
     * to expand keywords if needed.</li> <li>If the auto-merge fails, the server can still update the archive by removing the branch label, but it sends 4 files back to the
     * client: the common ancestor, the branch parent tip, the branch tip, and the merged result with the overlap markers in place. The user will have to manually edit the files in
     * order to produce the merged file the file should show a status of 'Your copy changed'. (Alternately, it could cause the client to display the merge file gui frame where the
     * user _must_ produce a merged result).</li> </ol>
     *
     * @param mergedInfo the file on which the simple merge is performed.
     */
    private void performSimpleMerge(MergedInfoInterface mergedInfo) {
        // This is a synchronous call.
        ResolveConflictResults resolveConflictResults = mergedInfo.resolveConflictFromParentBranch(getProjectName(), getViewName());
        if (resolveConflictResults != null) {
            boolean overlapDetectedFlag = false;
            String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getViewName());
            if (resolveConflictResults.getMergedResultBuffer() != null) {
                // Write this to the workfile location for this file, as it is our best guess at a merged result
                writeMergedResultToWorkfile(mergedInfo, workfileBase, resolveConflictResults.getMergedResultBuffer());
            }
            if (resolveConflictResults.getBranchTipRevisionBuffer() != null) {
                writeConflictFile(mergedInfo, "branchTip", workfileBase, resolveConflictResults.getBranchTipRevisionBuffer());
                overlapDetectedFlag = true;
            }
            if (resolveConflictResults.getCommonAncestorBuffer() != null) {
                writeConflictFile(mergedInfo, "commonAncestor", workfileBase, resolveConflictResults.getCommonAncestorBuffer());
                overlapDetectedFlag = true;
            }
            if (overlapDetectedFlag) {
                writeConflictFile(mergedInfo, "branchParentTip", workfileBase, resolveConflictResults.getBranchParentTipRevisionBuffer());
            }
            QWinFrame.getQWinFrame().refreshCurrentView();
            if (overlapDetectedFlag) {
                // TODO -- Ideally, we should automatically launch the visual merge tool here.
                StringBuilder stringBuffer = new StringBuilder();
                stringBuffer.append("Overlap detected when merging [").append(mergedInfo.getShortWorkfileName()).append("]. You will need to perform a manual merge.");
                final String message = stringBuffer.toString();
                QWinUtility.logProblem(Level.INFO, message);
                Runnable later = new Runnable() {

                    @Override
                    public void run() {
                        // Let the user know they'll need to perform the merge manually.
                        JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), message, "Merge Overlap Detected", JOptionPane.PLAIN_MESSAGE);
                    }
                };
                SwingUtilities.invokeLater(later);
            }
        }
    }

    private void performParentRenamedMerge(MergedInfoInterface mergedInfo) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
