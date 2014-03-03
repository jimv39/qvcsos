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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ClientRequestGetInfoForMergeData;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseGetInfoForMerge;
import com.qumasoft.qvcslib.ServerResponseInterface;

/**
 * This class is used to lookup some information about a file so that the client can figure out what kind of merge on a project
 * branch it needs to perform in the case of overlap. At this writing, we're only supporting translucent type of project branches.
 *
 * @author Jim Voris
 */
public class ClientRequestGetInfoForMerge implements ClientRequestInterface {
    private final MergeTypeHelper mergeTypeHelper;
    private final ClientRequestGetInfoForMergeData request;

    /**
     * Creates a new instance of ClientRequestGetInfoForMerge.
     *
     * @param data instance of super class that contains command line arguments, etc.
     */
    public ClientRequestGetInfoForMerge(ClientRequestGetInfoForMergeData data) {
        request = data;
        mergeTypeHelper = new MergeTypeHelper(request.getProjectName(), request.getViewName());
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
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String appendedPath = request.getAppendedPath();
        int fileID = request.getFileID();
        ServerResponseGetInfoForMerge serverResponseGetInfoForMerge = new ServerResponseGetInfoForMerge();
        ProjectView projectView = ViewManager.getInstance().getView(projectName, viewName);
        String parentBranchName = projectView.getRemoteViewProperties().getBranchParent();
        FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(projectName, viewName, fileID);
        FileIDInfo parentFileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(projectName, parentBranchName, fileID);
        serverResponseGetInfoForMerge.setProjectName(projectName);
        serverResponseGetInfoForMerge.setViewName(viewName);
        serverResponseGetInfoForMerge.setAppendedPath(appendedPath);
        serverResponseGetInfoForMerge.setShortWorkfileName(fileIDInfo.getShortFilename());
        serverResponseGetInfoForMerge.setParentAppendedPath(parentFileIDInfo.getAppendedPath());
        serverResponseGetInfoForMerge.setParentShortWorkfileName(parentFileIDInfo.getShortFilename());

        serverResponseGetInfoForMerge.setParentMovedFlag(mergeTypeHelper.didFileMoveOnParentBranch(fileID, parentBranchName));
        serverResponseGetInfoForMerge.setParentRenamedFlag(mergeTypeHelper.wasFileRenamedOnParentBranch(fileID, parentBranchName));
        serverResponseGetInfoForMerge.setBranchMovedFlag(mergeTypeHelper.didFileMoveOnBranch(fileID, parentBranchName));
        serverResponseGetInfoForMerge.setBranchRenamedFlag(mergeTypeHelper.wasFileRenamedOnBranch(fileID, parentBranchName));
        serverResponseGetInfoForMerge.setCreatedOnBranchFlag(mergeTypeHelper.wasFileCreatedOnBranch(fileID, parentBranchName));
        returnObject = serverResponseGetInfoForMerge;
        return returnObject;
    }
}
