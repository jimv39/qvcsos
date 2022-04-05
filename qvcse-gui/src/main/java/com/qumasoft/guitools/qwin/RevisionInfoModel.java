/*   Copyright 2004-2021 Jim Voris
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

import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.ListDataListener;

/**
 * Revision and label information model.
 * @author Jim Voris
 */
public class RevisionInfoModel implements javax.swing.ListModel {

    private static final String PARSER_TAG = "-";
    private static final String SEPARATOR_TAG = "Separator------------------------------------------------";
    // The backing store here will be a linked list.
    private final List<String> revAndLabelList = Collections.synchronizedList(new LinkedList<>());
    private final Map<ListDataListener, ListDataListener> listeners = Collections.synchronizedMap(new HashMap<>());
    private static final int DEFAULT_WORD_WRAP_COLUMN = 80;

    /**
     * Creates a new instance of RevisionInfoModel.
     */
    public RevisionInfoModel() {
    }

    /**
     * Create a new instance, using the given mergedInfo.
     * @param mergedInfo the file from which we get the revision and label information.
     */
    public RevisionInfoModel(MergedInfoInterface mergedInfo) {
        if (mergedInfo.getArchiveInfo() != null) {
            addRevisionInformation(mergedInfo);
        }
    }

    public RevisionInfoModel(LogfileInfo logfileInfo) {
        if (logfileInfo != null) {
            addRevisionInformation(logfileInfo);
        }
    }

    private void addRevisionInformation(MergedInfoInterface mergedInfo) {
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        addRevisionInformation(logfileInfo);
    }

    private void addRevisionInformation(LogfileInfo logfileInfo) {
        int revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();

        for (int i = 0; i < revisionCount; i++) {
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
            String revisionCreator = revHeader.getCreator();
            revAndLabelList.add(PARSER_TAG + revHeader.getRevisionString() + " commit id: " + revHeader.getCommitId() + " check in time: "
                    + revHeader.getCheckInDate().toString() + " by " + revisionCreator + "\n");
            revAndLabelList.add(PARSER_TAG + "Workfile edit date: " + revHeader.getEditDate().toString());
            addWordWrappedDescription(revHeader.getRevisionDescription());
            revAndLabelList.add(SEPARATOR_TAG);
        }
    }

    private void addWordWrappedDescription(String desc) {
        byte[] descriptionBuffer = desc.getBytes();

        // First convert any newline characters to spaces so we can put
        // word wrap in whatever column the property file says.
        for (int i = 0; i < descriptionBuffer.length; i++) {
            if (descriptionBuffer[i] == '\n') {
                descriptionBuffer[i] = ' ';
            }
        }

        // Figure out where to word wrap.
        int column = 0;
        int preceedingSpaceIndex = 0;
        int stringAnchorIndex = 0;
        for (int i = 0; i < descriptionBuffer.length; i++, column++) {
            if (descriptionBuffer[i] == ' ') {
                if ((preceedingSpaceIndex > 0) && (column > DEFAULT_WORD_WRAP_COLUMN)) {
                    String description = new String(descriptionBuffer, stringAnchorIndex, preceedingSpaceIndex - stringAnchorIndex);
                    revAndLabelList.add(PARSER_TAG + description);
                    column = 0;
                    stringAnchorIndex = preceedingSpaceIndex + 1;
                }
                preceedingSpaceIndex = i;
            }
        }
        String finalLine = new String(descriptionBuffer, stringAnchorIndex, descriptionBuffer.length - stringAnchorIndex);
        revAndLabelList.add(PARSER_TAG + finalLine);
    }

    /**
     * Adds a listener to the list that's notified each time a change to the data model occurs.
     *
     * @param l the
     * <code>ListDataListener</code> to be added
     *
     */
    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.put(l, l);
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the requested index
     * @return the value at
     * <code>index</code>
     *
     */
    @Override
    public Object getElementAt(int index) {
        return revAndLabelList.get(index);
    }

    /**
     * Returns the length of the list.
     *
     * @return the length of the list
     *
     */
    @Override
    public int getSize() {
        return revAndLabelList.size();
    }

    /**
     * Removes a listener from the list that's notified each time a change to the data model occurs.
     *
     * @param l the
     * <code>ListDataListener</code> to be removed
     *
     */
    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }
}
