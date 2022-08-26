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

import com.qumasoft.qvcslib.TagInfoData;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class TagInfoTableModel extends javax.swing.table.AbstractTableModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagInfoTableModel.class);

    private final JLabel cellLabel = new JLabel();
    private final String[] columnTitleStrings = {
        "  Tag  ",
        "  Description  ",
        "  Commit Id  ",
        "  Date  ",
        "  User  "
    };
    static final int TAG_COLUMN_INDEX = 0;
    static final int DESCRIPTION_INDEX = 1;
    static final int COMMIT_ID_INDEX = 2;
    static final int DATE_INDEX = 3;
    static final int USER_INDEX = 4;

    private List<TagInfoData> tagDataList = new ArrayList<>();

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
     * Get the class for the given column. We've got to supply this method in
     * order for our cell renderer to work correctly.
     *
     * @param columnIndex the column index.
     * @return the JLabel class, since that's the kind of component we'll always
     * be rendering.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
        return javax.swing.JLabel.class;
    }

    @Override
    public int getRowCount() {
        return tagDataList.size();
    }

    @Override
    public int getColumnCount() {
        return columnTitleStrings.length;
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        cellLabel.setText("");
        cellLabel.setIcon(null);
        if (rowIndex <= tagDataList.size()) {
            TagInfoData tagInfoData = tagDataList.get(rowIndex);
            switch (columnIndex) {
                case TAG_COLUMN_INDEX -> {
                    cellLabel.setText(tagInfoData.getTagText());
                    LOGGER.debug("tag: [{}]", tagInfoData.getTagText());
                }
                case DESCRIPTION_INDEX ->
                    cellLabel.setText(tagInfoData.getDescription());
                case COMMIT_ID_INDEX ->
                    cellLabel.setText(tagInfoData.getCommitId().toString());
                case DATE_INDEX ->
                    cellLabel.setText(tagInfoData.getCreationDate().toString());
                case USER_INDEX ->
                    cellLabel.setText(tagInfoData.getCreatorName());
                default -> {
                }
            }
        }
        return cellLabel;
    }

    public void setTagDataList(List<TagInfoData> tagData) {
        tagDataList = tagData;
        final TagInfoTableModel fThis = this;
        Runnable fireChange = () -> {
            fireTableChanged(new javax.swing.event.TableModelEvent(fThis));
        };
        SwingUtilities.invokeLater(fireChange);
    }

}
