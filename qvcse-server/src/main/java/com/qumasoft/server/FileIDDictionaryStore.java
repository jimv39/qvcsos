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
    // Keys are Project, View, and fileID.
    private final Map<String, Map<String, Map<Integer, FileIDInfo>>> dictionaryMap = Collections.synchronizedMap(new TreeMap<String, Map<String, Map<Integer, FileIDInfo>>>());

    /**
     * Creates a new instance of FileIDDictionaryStore
     */
    FileIDDictionaryStore() {
    }

    /**
     * Save the file ID information into the map.
     *
     * @param projectName the name of the project
     * @param viewName the name of the view/branch
     * @param fileID the file's fileID.
     * @param appendedPath the file's appended path.
     * @param shortFilename the short workfile name.
     * @param directoryID the directory ID.
     */
    synchronized void saveFileIDInfo(String projectName, String viewName, int fileID, String appendedPath, String shortFilename, int directoryID) {
        Map<String, Map<Integer, FileIDInfo>> projectMap = dictionaryMap.get(projectName);
        if (projectMap == null) {
            projectMap = new TreeMap<>();
            dictionaryMap.put(projectName, projectMap);
        }
        Map<Integer, FileIDInfo> viewMap = projectMap.get(viewName);
        if (viewMap == null) {
            viewMap = new TreeMap<>();
            projectMap.put(viewName, viewMap);
        }
        viewMap.put(Integer.valueOf(fileID), new FileIDInfo(directoryID, appendedPath, shortFilename));
    }

    /**
     * Lookup the fileID information for the given fileID.
     *
     * @param projectName the project name.
     * @param viewName the view name.
     * @param fileID the file's fileID.
     * @return the file's fileIDInformation, or null if we can't find it.
     */
    synchronized FileIDInfo lookupFileIDInfo(String projectName, String viewName, int fileID) {
        FileIDInfo fileIDInfo = null;
        Map<String, Map<Integer, FileIDInfo>> projectMap = dictionaryMap.get(projectName);
        if (projectMap != null) {
            Map<Integer, FileIDInfo> viewMap = projectMap.get(viewName);
            if (viewMap != null) {
                fileIDInfo = viewMap.get(Integer.valueOf(fileID));
            }
        }
        if (fileIDInfo == null) {
            // Walk up the branch 'tree' (i.e. look in this branch's parent map to see if the file info can be found there).
            // repeat until we reach the Trunk.
            if (!viewName.equalsIgnoreCase(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                String parentViewName = ViewManager.getInstance().getView(projectName, viewName).getRemoteBranchProperties().getBranchParent();
                return lookupFileIDInfo(projectName, parentViewName, fileID);
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
     * Remove the file id's for the given view for a given project. This should be called when a view is deleted.
     *
     * @param projectName the name of the project.
     * @param viewName the name of the view within that project.
     * @return true on success (i.e. we found the project and the view, and pruned the view from the store). false otherwise.
     */
    synchronized boolean removeIDsForView(String projectName, String viewName) {
        boolean retVal = false;
        if (dictionaryMap.containsKey(projectName)) {
            Map<String, Map<Integer, FileIDInfo>> projectMap = dictionaryMap.get(projectName);
            if (projectMap.containsKey(viewName)) {
                projectMap.remove(viewName);
                retVal = true;
            }
        }
        return retVal;
    }
}
