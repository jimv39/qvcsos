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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.Utility;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import static com.qumasoft.guitools.qwin.QWinUtility.logMessage;

/**
 * Activity list model.
 * @author Jim Voris
 */
public class ActivityListModel implements javax.swing.ListModel {
    // TODO -- this should be configurable.

    private static final int ACTVITY_PANE_MAXIMUM_SIZE = 5000;  // 5000 rows of info.
    // The backing store here will be a linked list.
    private final List<String> activityList;
    private final Map<ListDataListener, ListDataListener> activityListeners;

    /**
     * Creates a new instance of ActivityListModel.
     */
    public ActivityListModel() {
        this.activityListeners = Collections.synchronizedMap(new HashMap<ListDataListener, ListDataListener>());
        this.activityList = Collections.synchronizedList(new LinkedList<String>());
    }

    private void notifyListeners(final ListDataEvent event) {
        if (SwingUtilities.isEventDispatchThread()) {
            privateNotifyListeners(event);
        } else {
            Runnable postEvent = () -> {
                privateNotifyListeners(event);
            };

            try {
                SwingUtilities.invokeLater(postEvent);
            } catch (Exception e) {
                logMessage(Utility.expandStackTraceToString(e));
            }
        }
    }

    private void privateNotifyListeners(ListDataEvent event) {
        activityListeners.values().stream().forEach((listener) -> {
            listener.contentsChanged(event);
        });
    }

    /**
     * Add a message to the model. Note that new messages are added to the beginning of the model so that new messages always appear at the top of the list.
     * @param logMessage the log message to add.
     */
    public void addMessage(String logMessage) {
        if (activityList.size() > ACTVITY_PANE_MAXIMUM_SIZE) {
            activityList.remove(ACTVITY_PANE_MAXIMUM_SIZE);
        }

        activityList.add(0, logMessage);

        // Let any listeners know about the change.
        notifyListeners(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, activityList.size() - 1));
    }

    @Override
    public void addListDataListener(ListDataListener listener) {
        activityListeners.put(listener, listener);
    }

    @Override
    public Object getElementAt(int index) {
        return activityList.get(index);
    }

    @Override
    public int getSize() {
        return activityList.size();
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        activityListeners.remove(l);
    }
}
