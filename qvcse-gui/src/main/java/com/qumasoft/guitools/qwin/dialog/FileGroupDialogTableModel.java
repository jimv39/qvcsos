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
package com.qumasoft.guitools.qwin.dialog;

import com.qumasoft.guitools.qwin.FileGroupDataElement;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JLabel;

/**
 * File group dialog table model.
 * @author Jim Voris
 */
public final class FileGroupDialogTableModel extends javax.swing.table.AbstractTableModel {
    private static final long serialVersionUID = -6393907172418291965L;

    private final String[] columnTitleStrings;
    private final JLabel label = new JLabel();
    private final Map<String, FileGroupDataElement> fileGroupsMap;

    /**
     * Creates a new instance of FileGroupDialogTableModel.
     * @param fileGroups a map of file group data elements.
     */
    public FileGroupDialogTableModel(Map<String, FileGroupDataElement> fileGroups) {
        this.columnTitleStrings = new String[]{"  File Group Name  ", "  Extensions in group  "};
        fileGroupsMap = fileGroups;
    }

    @Override
    public int getRowCount() {
        return fileGroupsMap.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return javax.swing.JLabel.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        label.setText("");
        label.setIcon(null);
        FileGroupDataElement fileGroupDataElement = getRow(row);
        switch (column) {
            case 0:
                // Need to get the group name
                if (fileGroupDataElement != null) {
                    String groupName = fileGroupDataElement.getGroupName();
                    label.setText(groupName);
                }
                break;
            case 1:
                // Need to get the group extensions
                if (fileGroupDataElement != null) {
                    String[] extensions = fileGroupDataElement.getExtensions();
                    String extensionsString = getExtensionsString(extensions);
                    label.setText(extensionsString);
                }
                break;
            default:
                throw new QVCSRuntimeException("Unexpected column: [" + column + "]");
        }
        return label;
    }

    /**
     * Get the file group name for the given index.
     * @param index the index of the group we're interested in.
     * @return the given group's name.
     */
    public String getGroupName(int index) {
        FileGroupDataElement fileGroupDataElement = getRow(index);
        String groupName = "";
        if (fileGroupDataElement != null) {
            groupName = fileGroupDataElement.getGroupName();
        }
        return groupName;
    }

    /**
     * Get the file extensions that are members of a given file group.
     * @param index the index of the group we're interested in.
     * @return an array of file extensions that compose the given file group.
     */
    public String[] getExtensions(int index) {
        String[] extensions;
        FileGroupDataElement fileGroupDataElement = getRow(index);
        if (fileGroupDataElement != null) {
            extensions = fileGroupDataElement.getExtensions();
        } else {
            extensions = new String[0];
        }
        return extensions;
    }

    /**
     * Aggregate the extensions into a single comma separated string.
     * @param extensions the extensions that we'll aggregate.
     * @return an aggregated, comma separated string of extensions.
     */
    static String getExtensionsString(String[] extensions) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < extensions.length; i++) {
            stringBuffer.append(extensions[i]);
            if (i < (extensions.length - 1)) {
                stringBuffer.append(",");
            }
        }
        return stringBuffer.toString();
    }

    private FileGroupDataElement getRow(int row) {
        FileGroupDataElement fileGroupDataElement = null;
        if (row < getRowCount()) {
            Iterator<FileGroupDataElement> it = fileGroupsMap.values().iterator();
            int i = 0;
            while (it.hasNext()) {
                FileGroupDataElement element = it.next();
                if (i == row) {
                    fileGroupDataElement = element;
                    break;
                }
                i++;
            }
        }
        return fileGroupDataElement;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnTitleStrings[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
