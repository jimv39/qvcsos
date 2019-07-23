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

import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Branch store.
 * @author Jim Voris
 */
public class BranchStore implements Serializable {
    private static final long serialVersionUID = 1036553947513802628L;

    // This is what actually gets serialized
    private final Map<String, Map<String, Properties>> remoteBranchProperties = Collections.synchronizedMap(new TreeMap<>());
    // This is a convenient map of ProjectBranch objects that are built from
    // the serialized data. Note that the ProjectBranch class is NOT serializable.
    private transient Map<String, Map<String, ProjectBranch>> branches = null;
    private transient boolean initCompleteFlag = false;
    // Create our logger object
    private static final transient Logger LOGGER = LoggerFactory.getLogger(BranchStore.class);

    /**
     * Creates a new instance of BranchStore.
     */
    public BranchStore() {
    }

    Collection<ProjectBranch> getBranches(final String projectName) {
        initProjectBranchMap();
        Collection<ProjectBranch> projectBranches = null;
        if (branches.get(projectName) != null) {
            projectBranches = branches.get(projectName).values();
        }
        return projectBranches;
    }

    ProjectBranch getBranch(final String projectName, final String branchName) {
        initProjectBranchMap();
        ProjectBranch projectBranch = null;

        if (branches.get(projectName) != null) {
            projectBranch = branches.get(projectName).get(branchName);
        }
        return projectBranch;
    }

    void addBranch(ProjectBranch projectBranch) throws QVCSException {
        initProjectBranchMap();

        // Make sure the branch name is not in use.
        ProjectBranch existingBranch = null;
        Map<String, ProjectBranch> localBranches = branches.get(projectBranch.getProjectName());
        if (localBranches != null) {
            existingBranch = localBranches.get(projectBranch.getBranchName());
        } else {
            // There are no branches for this project yet... we need to make
            // a Map to contain this new branch.
            localBranches = Collections.synchronizedMap(new TreeMap<String, ProjectBranch>());
            branches.put(projectBranch.getProjectName(), localBranches);

            Map<String, Properties> branchPropertiesMap = Collections.synchronizedMap(new TreeMap<String, Properties>());
            remoteBranchProperties.put(projectBranch.getProjectName(), branchPropertiesMap);
        }
        if (existingBranch == null) {
            localBranches.put(projectBranch.getBranchName(), projectBranch);

            // And make sure to store a copy of the branch's properties in the
            // Map that gets serialized.
            remoteBranchProperties.get(projectBranch.getProjectName()).put(projectBranch.getBranchName(), projectBranch.getRemoteBranchProperties().getProjectProperties());
        } else {
            throw new QVCSException("The branch named '" + projectBranch.getBranchName() + " ' is already defined for project " + projectBranch.getProjectName());
        }
    }

    void removeBranch(ProjectBranch projectBranch) {
        initProjectBranchMap();

        ProjectBranch existingBranch;
        Map<String, ProjectBranch> localBranches = branches.get(projectBranch.getProjectName());

        // Only bother to remove it if it already exists...
        if (localBranches != null) {
            existingBranch = localBranches.get(projectBranch.getBranchName());
            if (existingBranch != null) {
                localBranches.remove(projectBranch.getBranchName());

                // Remove it from our serialized container...
                remoteBranchProperties.get(projectBranch.getProjectName()).remove(projectBranch.getBranchName());
            }
        }
    }

    void initProjectBranchMap() {
        if (!initCompleteFlag) {
            branches = Collections.synchronizedMap(new TreeMap<>());
            Iterator<String> it = remoteBranchProperties.keySet().iterator();
            while (it.hasNext()) {
                String projectName = it.next();
                Map<String, Properties> branchPropertiesMap = remoteBranchProperties.get(projectName);
                Iterator<Map.Entry<String, Properties>> propertiesMapIterator = branchPropertiesMap.entrySet().iterator();
                Map<String, ProjectBranch> branchMap = Collections.synchronizedMap(new TreeMap<>());
                while (propertiesMapIterator.hasNext()) {
                    Map.Entry<String, Properties> entry = propertiesMapIterator.next();
                    String branchName = entry.getKey();
                    Properties localRemoteBranchProperties = entry.getValue();
                    ProjectBranch projectBranch = new ProjectBranch();
                    projectBranch.setProjectName(projectName);
                    projectBranch.setBranchName(branchName);
                    projectBranch.setRemoteBranchProperties(new RemoteBranchProperties(projectName, branchName, localRemoteBranchProperties));

                    branchMap.put(branchName, projectBranch);
                }
                branches.put(projectName, branchMap);
            }
        }
        initCompleteFlag = true;
    }

    void dump() {
        Iterator<String> it = branches.keySet().iterator();
        while (it.hasNext()) {
            String projectName = it.next();
            Iterator<String> branchNameIterator = branches.get(projectName).keySet().iterator();
            while (branchNameIterator.hasNext()) {
                String branchName = branchNameIterator.next();
                LOGGER.info("Found branch defined for: [{}]: [{}]", projectName, branchName);
            }
        }
    }
}
