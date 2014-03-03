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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.KeywordProperties;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
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
public class RevAndLabelInfoModel implements javax.swing.ListModel {

    private static final String HEADER_TAG = "Header-";
    private static final String LOCK_TAG = "Lock-";
    private static final String LABEL_TAG = "Label-";
    private static final String FLOATLABEL_TAG = "FloatLabel-";
    private static final String PARSER_TAG = "-";
    private static final String SEPARATOR_TAG = "Separator------------------------------------------------";
    // The backing store here will be a linked list.
    private List<String> revAndLabelList = Collections.synchronizedList(new LinkedList<String>());
    private Map<ListDataListener, ListDataListener> listeners = Collections.synchronizedMap(new HashMap<ListDataListener, ListDataListener>());
    private KeywordProperties keywordProperties;
    private int wordWrapColumn;
    private static final int DEFAULT_WORD_WRAP_COLUMN = 82;

    /**
     * Creates a new instance of RevAndLabelInfoModel.
     */
    public RevAndLabelInfoModel() {
    }

    /**
     * Create a new instance, using the given mergedInfo.
     * @param mergedInfo the file from which we get the revision and label information.
     */
    public RevAndLabelInfoModel(MergedInfoInterface mergedInfo) {
        try {
            keywordProperties = new KeywordProperties();
            wordWrapColumn = keywordProperties.getWordWrapColumn();
        } catch (Exception e) {
            wordWrapColumn = DEFAULT_WORD_WRAP_COLUMN;
        }
        if (mergedInfo.getArchiveInfo() != null) {
            addRevisionInformation(mergedInfo);
        }
    }

    private void addRevisionInformation(MergedInfoInterface mergedInfo) {
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        int revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
        AccessList accessList = new AccessList(logfileInfo.getLogFileHeaderInfo().getModifierList());

        // Add header info at the beginning...
        LogFileHeaderInfo logfileHeaderInfo = logfileInfo.getLogFileHeaderInfo();
        revAndLabelList.add(HEADER_TAG + "File Description:");
        addWordWrappedDescription(logfileHeaderInfo.getModuleDescription());
        revAndLabelList.add(SEPARATOR_TAG);

        for (int i = 0; i < revisionCount; i++) {
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
            String revisionCreator = accessList.indexToUser(revHeader.getCreatorIndex());
            revAndLabelList.add(PARSER_TAG + revHeader.getRevisionString() + " check in time: " + revHeader.getCheckInDate().toString() + " by " + revisionCreator + "\n");
            if (revHeader.isLocked()) {
                String locker = accessList.indexToUser(revHeader.getLockerIndex());
                revAndLabelList.add(LOCK_TAG + "Locked by: " + locker);
            }
            revAndLabelList.add(PARSER_TAG + "Workfile edit date: " + revHeader.getEditDate().toString());
            addLabelInfo(i, mergedInfo);
            addWordWrappedDescription(revHeader.getRevisionDescription());
            revAndLabelList.add(SEPARATOR_TAG);
        }
    }

    private void addLabelInfo(int revisionIndex, MergedInfoInterface mergedInfo) {
        LabelInfo[] labelInfo = mergedInfo.getArchiveInfo().getLogfileInfo().getLogFileHeaderInfo().getLabelInfo();
        if (labelInfo != null) {
            RevisionHeader revisionHeader = mergedInfo.getArchiveInfo().getLogfileInfo().getRevisionInformation().getRevisionHeader(revisionIndex);
            String revisionString = revisionHeader.getRevisionString();
            int revisionDepth = revisionHeader.getDepth();
            boolean isTipRevision = revisionHeader.isTip();

            // Add any floating labels first.
            if (isTipRevision) {
                for (LabelInfo labelInfo1 : labelInfo) {
                    if (labelInfo1.isFloatingLabel() && (revisionDepth == labelInfo1.getDepth())) {
                        String labelRevisionString = labelInfo1.getLabelRevisionString();
                        if (revisionString.startsWith(labelRevisionString)) {
                            revAndLabelList.add(FLOATLABEL_TAG + labelInfo1.getLabelString());
                        }
                    }
                }
            }
            for (LabelInfo labelInfo1 : labelInfo) {
                String labelRevisionString = labelInfo1.getLabelRevisionString();
                if (labelRevisionString.equals(revisionString)) {
                    revAndLabelList.add(LABEL_TAG + labelInfo1.getLabelString());
                }
            }
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
                if ((preceedingSpaceIndex > 0) && (column > wordWrapColumn)) {
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
