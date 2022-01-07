/*
 * Copyright 2021 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.qvcslib;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache LogFileProxy data sent from the server. Each project has its own cache.
 * Each project branch has a Map of LogFileProxy objects, keyed by fileId. This
 * class is used to make it easier to notify child branches of changes on a
 * parent branch. There is no need to notify read-only child branches.
 *
 * @author Jim Voris.
 */
public class LogFileProxyCache {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileProxyCache.class);

    private final Integer projectId;
    private final Map<Integer, Map<Integer, LogFileProxy>> mapsByBranchId = Collections.synchronizedMap(new TreeMap<>());

    public LogFileProxyCache(Integer id) {
        this.projectId = id;
    }

    /**
     * @return the projectId
     */
    public Integer getProjectId() {
        return projectId;
    }

    public void updateLogFileProxy(String serverName, String projectName, ClientBranchInfo branchInfo, LogFileProxy proxy) {
        RemoteBranchProperties remoteBranchProperties = new RemoteBranchProperties(projectName, branchInfo.getBranchName(), branchInfo.getBranchProperties());
        Boolean readOnlyBranchFlag = remoteBranchProperties.getIsReadOnlyBranchFlag();
        Boolean releaseBranchFlag = remoteBranchProperties.getIsReleaseBranchFlag();

        // There is only work to do if this is not a read-only branch & not a release branch,
        // since neither are allowed to have child branches.
        if (!readOnlyBranchFlag && !releaseBranchFlag) {
            Map<Integer, LogFileProxy> logFileProxyMapForBranch = mapsByBranchId.get(branchInfo.getBranchId());
            if (logFileProxyMapForBranch == null) {
                logFileProxyMapForBranch = Collections.synchronizedMap(new TreeMap<>());
                mapsByBranchId.put(branchInfo.getBranchId(), logFileProxyMapForBranch);
                logFileProxyMapForBranch.put(proxy.getFileID(), proxy);
            } else {
                LogFileProxy fetchedProxy = logFileProxyMapForBranch.get(proxy.getFileID());
                if (fetchedProxy != null) {
                    fetchedProxy.setSkinnyLogfileInfo(proxy.getSkinnyLogfileInfo());
                } else {
                    logFileProxyMapForBranch.put(proxy.getFileID(), proxy);
                }
            }
            mergeManagers(proxy);
            updateChildBranches(serverName, projectName, branchInfo.getBranchId(), proxy);
        }
    }

    private void updateChildBranches(String serverName, String projectName, Integer branchId, LogFileProxy proxy) {
        for (Integer branchKey : mapsByBranchId.keySet()) {

            // Only need to update possible child branches that are neither read-only nor release branches.
            ClientBranchInfo branchInfo = ClientBranchManager.getInstance().getClientBranchInfo(serverName, projectName, branchKey);
            RemoteBranchProperties remoteBranchProperties = new RemoteBranchProperties(projectName, branchInfo.getBranchName(), branchInfo.getBranchProperties());
            Boolean readOnlyBranchFlag = remoteBranchProperties.getIsReadOnlyBranchFlag();
            Boolean releaseBranchFlag = remoteBranchProperties.getIsReleaseBranchFlag();

            if ((!readOnlyBranchFlag && !releaseBranchFlag) && (branchKey > branchId)) {
                Map<Integer, LogFileProxy> logFileProxyMapForBranch = mapsByBranchId.get(branchKey);
                LogFileProxy branchProxy = logFileProxyMapForBranch.get(proxy.getFileID());
                if (branchProxy != null) {
                    if (branchProxy.getSkinnyLogfileInfo().getBranchId().intValue() == proxy.getSkinnyLogfileInfo().getBranchId().intValue()) {
                        branchProxy.setSkinnyLogfileInfo(proxy.getSkinnyLogfileInfo());
                        mergeManagers(branchProxy);
                    }
                }
            }
        }
    }
    public void removeLogFileProxy(Integer branchId, Integer fileId) {
        Map<Integer, LogFileProxy> logFileProxyMapForBranch = mapsByBranchId.get(branchId);
        if (logFileProxyMapForBranch != null) {
            LogFileProxy fetchedProxy = logFileProxyMapForBranch.remove(fileId);
        }
        removeFromChildBranches(branchId, fileId);
    }

    private void removeFromChildBranches(Integer branchId, Integer fileId) {
        for (Integer branchKey : mapsByBranchId.keySet()) {
            if (branchKey.intValue() != branchId.intValue()) {
                Map<Integer, LogFileProxy> logFileProxyMapForBranch = mapsByBranchId.get(branchKey);
                LogFileProxy branchProxy = logFileProxyMapForBranch.get(fileId);
                if (branchProxy != null) {
                    if (branchProxy.getSkinnyLogfileInfo().getBranchId().intValue() == branchId.intValue()) {
                        logFileProxyMapForBranch.remove(fileId);
                    }
                }
            }
        }
    }

    private void mergeManagers(LogFileProxy logFileProxy) {
        ArchiveDirManagerProxy dirManagerProxy = logFileProxy.getArchiveDirManagerProxy();
        dirManagerProxy.notifyListeners();
    }
}
