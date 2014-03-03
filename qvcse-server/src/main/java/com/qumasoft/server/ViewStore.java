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

import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * View store.
 * @author Jim Voris
 */
public class ViewStore implements Serializable {
    private static final long serialVersionUID = 1036553947513802628L;

    // This is what actually gets serialized
    private final Map<String, Map<String, Properties>> remoteViewProperties = Collections.synchronizedMap(new TreeMap<String, Map<String, Properties>>());
    // This is a convenient map of ProjectView objects that are built from
    // the serialized data. Note that the ProjectView class is NOT serializable.
    private transient Map<String, Map<String, ProjectView>> views = null;
    private transient boolean initCompleteFlag = false;
    // Create our logger object
    private static final transient Logger LOGGER = Logger.getLogger("com.qumasoft.server");

    /**
     * Creates a new instance of ViewStore.
     */
    public ViewStore() {
    }

    Collection<ProjectView> getViews(final String projectName) {
        initProjectViewMap();
        Collection<ProjectView> projectViews = null;
        if (views.get(projectName) != null) {
            projectViews = views.get(projectName).values();
        }
        return projectViews;
    }

    ProjectView getView(final String projectName, final String viewName) {
        initProjectViewMap();
        ProjectView projectView = null;

        if (views.get(projectName) != null) {
            projectView = views.get(projectName).get(viewName);
        }
        return projectView;
    }

    void addView(ProjectView projectView) throws QVCSException {
        initProjectViewMap();

        // Make sure the view name is not in use.
        ProjectView existingView = null;
        Map<String, ProjectView> localViews = views.get(projectView.getProjectName());
        if (localViews != null) {
            existingView = localViews.get(projectView.getViewName());
        } else {
            // There are no views for this project yet... we need to make
            // a Map to contain this new view.
            localViews = Collections.synchronizedMap(new TreeMap<String, ProjectView>());
            views.put(projectView.getProjectName(), localViews);

            Map<String, Properties> viewPropertiesMap = Collections.synchronizedMap(new TreeMap<String, Properties>());
            remoteViewProperties.put(projectView.getProjectName(), viewPropertiesMap);
        }
        if (existingView == null) {
            localViews.put(projectView.getViewName(), projectView);

            // And make sure to store a copy of the view's properties in the
            // Map that gets serialized.
            remoteViewProperties.get(projectView.getProjectName()).put(projectView.getViewName(), projectView.getRemoteViewProperties().getProjectProperties());
        } else {
            throw new QVCSException("The view named '" + projectView.getViewName() + " ' is already defined for project " + projectView.getProjectName());
        }
    }

    void removeView(ProjectView projectView) {
        initProjectViewMap();

        ProjectView existingView;
        Map<String, ProjectView> localViews = views.get(projectView.getProjectName());

        // Only bother to remove it if it already exists...
        if (localViews != null) {
            existingView = localViews.get(projectView.getViewName());
            if (existingView != null) {
                localViews.remove(projectView.getViewName());

                // Remove it from our serialized container...
                remoteViewProperties.get(projectView.getProjectName()).remove(projectView.getViewName());
            }
        }
    }

    void initProjectViewMap() {
        if (!initCompleteFlag) {
            views = Collections.synchronizedMap(new TreeMap<String, Map<String, ProjectView>>());
            Iterator<String> it = remoteViewProperties.keySet().iterator();
            while (it.hasNext()) {
                String projectName = it.next();
                Map<String, Properties> viewPropertiesMap = remoteViewProperties.get(projectName);
                Iterator<Map.Entry<String, Properties>> propertiesMapIterator = viewPropertiesMap.entrySet().iterator();
                Map<String, ProjectView> viewMap = Collections.synchronizedMap(new TreeMap<String, ProjectView>());
                while (propertiesMapIterator.hasNext()) {
                    Map.Entry<String, Properties> entry = propertiesMapIterator.next();
                    String viewName = entry.getKey();
                    Properties localRemoteViewProperties = entry.getValue();
                    ProjectView projectView = new ProjectView();
                    projectView.setProjectName(projectName);
                    projectView.setViewName(viewName);
                    projectView.setRemoteViewProperties(new RemoteViewProperties(projectName, viewName, localRemoteViewProperties));

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
                LOGGER.log(Level.INFO, "Found view defined for: " + projectName + ":" + viewName);
            }
        }
    }
}
