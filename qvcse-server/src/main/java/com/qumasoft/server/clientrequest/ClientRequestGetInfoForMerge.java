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
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetInfoForMergeData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qvcsos.server.DatabaseManager;

/**
 * This class is used to lookup some information about a file so that the client can figure out what kind of merge on a project
 * branch it needs to perform in the case of overlap. At this writing, we're only supporting feature type of project branches.
 *
 * @author Jim Voris
 */
public class ClientRequestGetInfoForMerge implements ClientRequestInterface {
    private final DatabaseManager databaseManager;
    private final String schemaName;
    private final ClientRequestGetInfoForMergeData request;

    /**
     * Creates a new instance of ClientRequestGetInfoForMerge.
     *
     * @param data instance of super class that contains command line arguments, etc.
     */
    public ClientRequestGetInfoForMerge(ClientRequestGetInfoForMergeData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
    }

    /**
     * Lookup the info that we need in order to figure out what kind of merge we need to perform.
     *
     * @param userName user's name.
     * @param response the object that identifies the client.
     * @return information needed to figure out what kind of merge we need to perform on the client.
     */
    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        // TODO
        return null;
//        ServerResponseInterface returnObject = null;
//        String projectName = request.getProjectName();
//        String branchName = request.getBranchName();
//        String appendedPath = request.getAppendedPath();
//        int fileID = request.getFileID();
//        ServerResponseGetInfoForMerge serverResponseGetInfoForMerge = new ServerResponseGetInfoForMerge();
//        ProjectBranch projectBranch = BranchManager.getInstance().getBranch(projectName, branchName);
//        String parentBranchName = projectBranch.getRemoteBranchProperties().getBranchParent();
//        FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(projectName, branchName, fileID);
//        FileIDInfo parentFileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(projectName, parentBranchName, fileID);
//        serverResponseGetInfoForMerge.setProjectName(projectName);
//        serverResponseGetInfoForMerge.setBranchName(branchName);
//        serverResponseGetInfoForMerge.setAppendedPath(appendedPath);
//        serverResponseGetInfoForMerge.setShortWorkfileName(fileIDInfo.getShortFilename());
//        serverResponseGetInfoForMerge.setParentAppendedPath(parentFileIDInfo.getAppendedPath());
//        serverResponseGetInfoForMerge.setParentShortWorkfileName(parentFileIDInfo.getShortFilename());
//
////        serverResponseGetInfoForMerge.setParentMovedFlag(mergeTypeHelper.didFileMoveOnParentBranch(fileID, parentBranchName));
////        serverResponseGetInfoForMerge.setParentRenamedFlag(mergeTypeHelper.wasFileRenamedOnParentBranch(fileID, parentBranchName));
////        serverResponseGetInfoForMerge.setBranchMovedFlag(mergeTypeHelper.didFileMoveOnBranch(fileID, parentBranchName));
////        serverResponseGetInfoForMerge.setBranchRenamedFlag(mergeTypeHelper.wasFileRenamedOnBranch(fileID, parentBranchName));
////        serverResponseGetInfoForMerge.setCreatedOnBranchFlag(mergeTypeHelper.wasFileCreatedOnBranch(fileID, parentBranchName));
//        // TODO
//        returnObject = serverResponseGetInfoForMerge;
//        return returnObject;
    }
}
