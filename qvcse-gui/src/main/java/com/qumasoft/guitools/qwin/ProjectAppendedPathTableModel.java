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
package com.qumasoft.guitools.qwin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jim Voris
 */
public class ProjectAppendedPathTableModel extends javax.swing.table.AbstractTableModel {
    static final int APPENDED_PATH_INDEX = 5;

    private final Set<String> appendedPathSet;
    private final List<String> appendedPathList = new ArrayList<>();
    private final String[] columnTitleStrings = {
        "  Appended Path  "
    };

    public ProjectAppendedPathTableModel(Set<String> appendedPaths) {
        this.appendedPathSet = appendedPaths;
    }

    @Override
    public int getRowCount() {
        return appendedPathList.size();
    }

    @Override
    public int getColumnCount() {
        return columnTitleStrings.length;
    }

    @Override
    public Object getValueAt(int i, int i1) {
        return appendedPathList.get(i);
    }

    /**
     * Get the column name for the given column index.
     *
     * @param columnIndex the column index.
     * @return the name of the given column.
     */
    @Override
    public String getColumnName(int columnIndex) {
        return columnTitleStrings[columnIndex];
    }

    /**
     * Get the class for the given column. We've got to supply this method in order for our cell renderer to work correctly.
     *
     * @param columnIndex the column index.
     * @return the JLabel class, since that's the kind of component we'll always be rendering.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
        return javax.swing.JLabel.class;
    }

    public void initialize() {
        for (String appendedPath : appendedPathSet) {
            appendedPathList.add(appendedPath);
        }
    }

}
