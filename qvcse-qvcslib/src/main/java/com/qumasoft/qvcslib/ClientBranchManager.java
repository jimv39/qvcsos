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

import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Our client side branch manager. We use this singleton to keep track of the
 * branches that we know about so we can easily know/lookup a branch's id, etc.
 *
 * @author Jim Voris.
 */
public final class ClientBranchManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientBranchManager.class);
    // This is a singleton.
    private static final ClientBranchManager FACTORY = new ClientBranchManager();
    // Mapped by Server name, Project name, and Branch name.
    private Map<String, Map<String, Map<String, ClientBranchInfo>>> clientBranchInfoByNameMaps = Collections.synchronizedMap(new TreeMap<>());
    private Map<String, Map<String, Map<Integer, ClientBranchInfo>>> clientBranchInfoByIdMaps = Collections.synchronizedMap(new TreeMap<>());

    /**
     * Creates a new instance of ClientBranchManager.
     */
    private ClientBranchManager() {
    }

    /**
     * Get the ClientBranchManager factory singleton.
     *
     * @return the ClientBranchManager factory singleton.
     */
    public static ClientBranchManager getInstance() {
        return FACTORY;
    }

    public void updateBranchInfo(ServerResponseListBranches response) {
        String serverName = response.getServerName();
        String projectName = response.getProjectName();
        for (ClientBranchInfo branchInfo : response.getClientBranchInfoList()) {
            Map<String, Map<String, ClientBranchInfo>> byNameMap = this.clientBranchInfoByNameMaps.get(serverName);
            Map<String, Map<Integer, ClientBranchInfo>> byIdMap = this.clientBranchInfoByIdMaps.get(serverName);
            if (byNameMap == null) {
                byNameMap = Collections.synchronizedMap(new TreeMap<>());
                byIdMap = Collections.synchronizedMap(new TreeMap<>());
                this.clientBranchInfoByNameMaps.put(serverName, byNameMap);
                this.clientBranchInfoByIdMaps.put(serverName, byIdMap);
            }
            Map<String, ClientBranchInfo> branchNameMap = byNameMap.get(projectName);
            Map<Integer, ClientBranchInfo> branchIdMap = byIdMap.get(projectName);
            if (branchNameMap == null) {
                branchNameMap = Collections.synchronizedMap(new TreeMap<>());
                branchIdMap = Collections.synchronizedMap(new TreeMap<>());
                byNameMap.put(projectName, branchNameMap);
                byIdMap.put(projectName, branchIdMap);
            }
            branchNameMap.put(branchInfo.getBranchName(), branchInfo);
            branchIdMap.put(branchInfo.getBranchId(), branchInfo);
        }
    }

    public ClientBranchInfo getClientBranchInfo(String serverName, String projectName, String branchName) {
        ClientBranchInfo clientBranchInfo = null;
        Map<String, Map<String, ClientBranchInfo>> projectMap = this.clientBranchInfoByNameMaps.get(serverName);
        if (projectMap != null) {
            Map<String, ClientBranchInfo> branchMap = projectMap.get(projectName);
            if (branchMap != null) {
                clientBranchInfo = branchMap.get(branchName);
            }
        }
        return clientBranchInfo;
    }

    public ClientBranchInfo getClientBranchInfo(String serverName, String projectName, Integer branchId) {
        ClientBranchInfo clientBranchInfo = null;
        Map<String, Map<Integer, ClientBranchInfo>> projectMap = this.clientBranchInfoByIdMaps.get(serverName);
        if (projectMap != null) {
            Map<Integer, ClientBranchInfo> branchMap = projectMap.get(projectName);
            if (branchMap != null) {
                clientBranchInfo = branchMap.get(branchId);
            }
        }
        return clientBranchInfo;
    }

    public Map<String, ClientBranchInfo> getClientBranchInfoMap(String serverName, String projectName) {
        Map<String, ClientBranchInfo> branchMap = null;
        Map<String, Map<String, ClientBranchInfo>> projectMap = this.clientBranchInfoByNameMaps.get(serverName);
        if (projectMap != null) {
            branchMap = projectMap.get(projectName);
        }
        return branchMap;
    }
}
