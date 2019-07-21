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
 * View store.
 * @author Jim Voris
 */
public class ViewStore implements Serializable {
    private static final long serialVersionUID = 1036553947513802628L;

    // This is what actually gets serialized
    private final Map<String, Map<String, Properties>> remoteViewProperties = Collections.synchronizedMap(new TreeMap<String, Map<String, Properties>>());
    // This is a convenient map of ProjectBranch objects that are built from
    // the serialized data. Note that the ProjectBranch class is NOT serializable.
    private transient Map<String, Map<String, ProjectBranch>> views = null;
    private transient boolean initCompleteFlag = false;
    // Create our logger object
    private static final transient Logger LOGGER = LoggerFactory.getLogger(ViewStore.class);

    /**
     * Creates a new instance of ViewStore.
     */
    public ViewStore() {
    }

    Collection<ProjectBranch> getViews(final String projectName) {
        initProjectViewMap();
        Collection<ProjectBranch> projectViews = null;
        if (views.get(projectName) != null) {
            projectViews = views.get(projectName).values();
        }
        return projectViews;
    }

    ProjectBranch getView(final String projectName, final String viewName) {
        initProjectViewMap();
        ProjectBranch projectView = null;

        if (views.get(projectName) != null) {
            projectView = views.get(projectName).get(viewName);
        }
        return projectView;
    }

    void addView(ProjectBranch projectView) throws QVCSException {
        initProjectViewMap();

        // Make sure the view name is not in use.
        ProjectBranch existingView = null;
        Map<String, ProjectBranch> localViews = views.get(projectView.getProjectName());
        if (localViews != null) {
            existingView = localViews.get(projectView.getBranchName());
        } else {
            // There are no views for this project yet... we need to make
            // a Map to contain this new view.
            localViews = Collections.synchronizedMap(new TreeMap<String, ProjectBranch>());
            views.put(projectView.getProjectName(), localViews);

            Map<String, Properties> viewPropertiesMap = Collections.synchronizedMap(new TreeMap<String, Properties>());
            remoteViewProperties.put(projectView.getProjectName(), viewPropertiesMap);
        }
        if (existingView == null) {
            localViews.put(projectView.getBranchName(), projectView);

            // And make sure to store a copy of the view's properties in the
            // Map that gets serialized.
            remoteViewProperties.get(projectView.getProjectName()).put(projectView.getBranchName(), projectView.getRemoteBranchProperties().getProjectProperties());
        } else {
            throw new QVCSException("The view named '" + projectView.getBranchName() + " ' is already defined for project " + projectView.getProjectName());
        }
    }

    void removeView(ProjectBranch projectView) {
        initProjectViewMap();

        ProjectBranch existingView;
        Map<String, ProjectBranch> localViews = views.get(projectView.getProjectName());

        // Only bother to remove it if it already exists...
        if (localViews != null) {
            existingView = localViews.get(projectView.getBranchName());
            if (existingView != null) {
                localViews.remove(projectView.getBranchName());

                // Remove it from our serialized container...
                remoteViewProperties.get(projectView.getProjectName()).remove(projectView.getBranchName());
            }
        }
    }

    void initProjectViewMap() {
        if (!initCompleteFlag) {
            views = Collections.synchronizedMap(new TreeMap<String, Map<String, ProjectBranch>>());
            Iterator<String> it = remoteViewProperties.keySet().iterator();
            while (it.hasNext()) {
                String projectName = it.next();
                Map<String, Properties> viewPropertiesMap = remoteViewProperties.get(projectName);
                Iterator<Map.Entry<String, Properties>> propertiesMapIterator = viewPropertiesMap.entrySet().iterator();
                Map<String, ProjectBranch> viewMap = Collections.synchronizedMap(new TreeMap<String, ProjectBranch>());
                while (propertiesMapIterator.hasNext()) {
                    Map.Entry<String, Properties> entry = propertiesMapIterator.next();
                    String viewName = entry.getKey();
                    Properties localRemoteViewProperties = entry.getValue();
                    ProjectBranch projectView = new ProjectBranch();
                    projectView.setProjectName(projectName);
                    projectView.setBranchName(viewName);
                    projectView.setRemoteBranchProperties(new RemoteBranchProperties(projectName, viewName, localRemoteViewProperties));

                    viewMap.put(viewName, projectView);
                }
                views.put(projectName, viewMap);
            }
        }
        initCompleteFlag = true;
    }

    void dump() {
        Iterator<String> it = views.keySet().iterator();
        while (it.hasNext()) {
            String projectName = it.next();
            Iterator<String> viewNameIterator = views.get(projectName).keySet().iterator();
            while (viewNameIterator.hasNext()) {
                String viewName = viewNameIterator.next();
                LOGGER.info("Found view defined for: [{}]: [{}]", projectName, viewName);
            }
        }
    }
}
