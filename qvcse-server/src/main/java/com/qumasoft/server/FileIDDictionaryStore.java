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

import com.qumasoft.qvcslib.QVCSConstants;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * File id dictionary store.
 * @author Jim Voris
 */
class FileIDDictionaryStore implements java.io.Serializable {
    private static final long serialVersionUID = -6215907547664301640L;

    // This is the map of dictionary ID to DictionaryIDInfo objects.
    // Keys are Project, Branch, and fileID.
    private final Map<String, Map<String, Map<Integer, FileIDInfo>>> dictionaryMap = Collections.synchronizedMap(new TreeMap<>());

    /**
     * Creates a new instance of FileIDDictionaryStore
     */
    FileIDDictionaryStore() {
    }

    /**
     * Save the file ID information into the map.
     *
     * @param projectName the name of the project
     * @param branchName the name of the branch
     * @param fileID the file's fileID.
     * @param appendedPath the file's appended path.
     * @param shortFilename the short workfile name.
     * @param directoryID the directory ID.
     */
    synchronized void saveFileIDInfo(String projectName, String branchName, int fileID, String appendedPath, String shortFilename, int directoryID) {
        Map<String, Map<Integer, FileIDInfo>> projectMap = dictionaryMap.get(projectName);
        if (projectMap == null) {
            projectMap = new TreeMap<>();
            dictionaryMap.put(projectName, projectMap);
        }
        Map<Integer, FileIDInfo> branchMap = projectMap.get(branchName);
        if (branchMap == null) {
            branchMap = new TreeMap<>();
            projectMap.put(branchName, branchMap);
        }
        branchMap.put(fileID, new FileIDInfo(directoryID, appendedPath, shortFilename));
    }

    /**
     * Lookup the fileID information for the given fileID.
     *
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param fileID the file's fileID.
     * @return the file's fileIDInformation, or null if we can't find it.
     */
    synchronized FileIDInfo lookupFileIDInfo(String projectName, String branchName, int fileID) {
        FileIDInfo fileIDInfo = null;
        Map<String, Map<Integer, FileIDInfo>> projectMap = dictionaryMap.get(projectName);
        if (projectMap != null) {
            Map<Integer, FileIDInfo> branchMap = projectMap.get(branchName);
            if (branchMap != null) {
                fileIDInfo = branchMap.get(fileID);
            }
        }
        if (fileIDInfo == null) {
            // Walk up the branch 'tree' (i.e. look in this branch's parent map to see if the file info can be found there).
            // repeat until we reach the Trunk.
            if (!branchName.equalsIgnoreCase(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                String parentBranchName = BranchManager.getInstance().getBranch(projectName, branchName).getRemoteBranchProperties().getBranchParent();
                return lookupFileIDInfo(projectName, parentBranchName, fileID);
            }
        }
        return fileIDInfo;
    }

    /**
     * Remove the file id's for a given project. This should be called when a project is deleted.
     *
     * @param projectName the name of the project.
     * @return true on success (i.e. we found the project, and pruned it from the store), false otherwise.
     */
    synchronized boolean removeIDsForProject(String projectName) {
        boolean retVal = false;
        if (dictionaryMap.containsKey(projectName)) {
            dictionaryMap.remove(projectName);
            retVal = true;
        }
        return retVal;
    }

    /**
     * Remove the file id's for the given branch for a given project. This should be called when a branch is deleted.
     *
     * @param projectName the name of the project.
     * @param branchName the name of the branch within that project.
     * @return true on success (i.e. we found the project and the branch, and pruned the branch from the store). false otherwise.
     */
    synchronized boolean removeIDsForBranch(String projectName, String branchName) {
        boolean retVal = false;
        if (dictionaryMap.containsKey(projectName)) {
            Map<String, Map<Integer, FileIDInfo>> projectMap = dictionaryMap.get(projectName);
            if (projectMap.containsKey(branchName)) {
                projectMap.remove(branchName);
                retVal = true;
            }
        }
        return retVal;
    }
}
