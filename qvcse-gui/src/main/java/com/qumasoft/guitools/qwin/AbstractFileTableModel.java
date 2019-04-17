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
package com.qumasoft.guitools.qwin;

import static com.qumasoft.guitools.qwin.QWinUtility.traceProblem;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * An abstract base class used for all the different models that could be used for the right file list pane. The basic idea is to have all callers use this as their representation
 * of the active model, irrespective of whether the model is the raw, unfiltered model, a filtered model, or a status-collapsed model.
 *
 * @author Jim Voris
 */
public abstract class AbstractFileTableModel extends javax.swing.table.AbstractTableModel implements javax.swing.event.ChangeListener {
    private static final long serialVersionUID = 6426976312479680201L;

    static final int FILENAME_COLUMN_INDEX = 0;
    static final int FILE_STATUS_COLUMN_INDEX = 1;
    static final int LOCKEDBY_COLUMN_INDEX = 2;
    static final int LASTCHECKIN_COLUMN_INDEX = 3;
    static final int WORKFILEIN_COLUMN_INDEX = 4;
    static final int FILESIZE_COLUMN_INDEX = 5;
    static final int LASTEDITBY_COLUMN_INDEX = 6;
    static final int APPENDED_PATH_INDEX = 7;
    private final String[] columnTitleStrings = {
        "  File name  ",
        "  File status  ",
        "  Locked by  ",
        "  Last Check in  ",
        "  Workfile in  ",
        "  Workfile size  ",
        "  Last Edit by  ",
        "  Appended Path "
    };
    private int sortColumnInt = 0;
    private String sortColumn = QVCSConstants.QVCS_FILENAME_COLUMN;

    /** Is the sort ascending (true), or descending. */
    private boolean ascendingSortFlag = true;

    /** The directory managers that are shown. */
    private DirectoryManagerInterface[] directoryManagers;

    /**
     * Creates a new instance of AbstractFileTableModel.
     */
    AbstractFileTableModel() {
    }

    /**
     * Return the MergedInfoInterface object associated with the given index. If there is no MergedInfoInterface object that is associated with the given index, then this method
     * should return a null.
     *
     * @param index the index to lookup.
     * @return Return the MergedInfoInterface object associated with the given index, or null if there is none.
     */
    public abstract MergedInfoInterface getMergedInfo(int index);

    /**
     * Set the array of directory managers from which the file list is generated.
     * @param managers the directory managers.
     * @param showProgressFlag show progress or not.
     * @param columnHeaderClickedFlag column header clicked flag.
     */
    abstract void setDirectoryManagers(DirectoryManagerInterface[] managers, boolean showProgressFlag, boolean columnHeaderClickedFlag);

    /**
     * Returns true if the cell at <I>rowIndex</I> and <I>columnIndex</I> is editable. Otherwise, setValueAt() on the cell will not change the value of that cell.
     *
     * @param rowIndex the row whose value is to be looked up
     * @param columnIndex the column whose value is to be looked up
     * @return true if the cell is editable.
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    int getSortColumnInteger() {
        return sortColumnInt;
    }

    void setSortColumnInteger(int column) {
        if (column == sortColumnInt) {
            ascendingSortFlag = !ascendingSortFlag;
        } else {
            ascendingSortFlag = true;
        }
        sortColumnInt = column;
        setDirectoryManagers(directoryManagers, false, true);
    }

    /**
     * Get the ascending sort flag.
     * @return the ascending sort flag.
     */
    public boolean getAscendingFlag() {
        return ascendingSortFlag;
    }

    /**
     * Add a mouse listener to the Table to trigger a table sort when a column heading is clicked in the JTable.
     *
     * @param table the file list table.
     */
    public void addMouseListenerToHeaderInTable(JTable table) {
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter columnHeaderMouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1) {
                    traceProblem("Sorting ... for column " + column);
                    String sortByColumn;
                    if (ClientTransactionManager.getInstance().getOpenTransactionCount() == 0) {
                        switch (column) {
                            case FILE_STATUS_COLUMN_INDEX:
                                sortByColumn = QVCSConstants.QVCS_STATUS_COLUMN;
                                break;
                            case LOCKEDBY_COLUMN_INDEX:
                                sortByColumn = QVCSConstants.QVCS_LOCKEDBY_COLUMN;
                                break;
                            case LASTCHECKIN_COLUMN_INDEX:
                                sortByColumn = QVCSConstants.QVCS_LAST_CHECKIN_COLUMN;
                                break;
                            case WORKFILEIN_COLUMN_INDEX:
                                sortByColumn = QVCSConstants.QVCS_WORKFILE_IN_COLUMN;
                                break;
                            case FILESIZE_COLUMN_INDEX:
                                sortByColumn = QVCSConstants.QVCS_WORKFILE_SIZE_COLUMN;
                                break;
                            case LASTEDITBY_COLUMN_INDEX:
                                sortByColumn = QVCSConstants.QVCS_LAST_EDIT_BY_COLUMN;
                                break;
                            case APPENDED_PATH_INDEX:
                                sortByColumn = QVCSConstants.QVCS_APPENDED_PATH_COLUMN;
                                break;
                            default:
                            case FILENAME_COLUMN_INDEX:
                                sortByColumn = QVCSConstants.QVCS_FILENAME_COLUMN;
                                break;
                        }
                        setSortColumn(sortByColumn);
                        setSortColumnInteger(column);
                        tableView.getTableHeader().repaint();
                        QWinFrame.getQWinFrame().setSortColumn(getSortColumn());
                    }
                }
            }
        };
        JTableHeader tableHeader = tableView.getTableHeader();
        tableHeader.addMouseListener(columnHeaderMouseListener);
    }

    /**
     * Get the sort column.
     * @return the sort column
     */
    public String getSortColumn() {
        return sortColumn;
    }

    /**
     * Set the sort column.
     * @param column the sort column.
     */
    public void setSortColumn(String column) {
        sortColumn = column;
    }

    /**
     * Does the file pass the active filter collection.
     * @param mergedInfo the file to test.
     * @return true if it passes the active filter collection; false if not.
     */
    public boolean passesFileFilters(MergedInfoInterface mergedInfo) {
        return true;
    }

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        // Run the update on the Swing thread.
        final AbstractFileTableModel fThis = this;
        Runnable fireChange = () -> {
            fireTableChanged(new javax.swing.event.TableModelEvent(fThis));
        };
        SwingUtilities.invokeLater(fireChange);
    }

    String[] getColumnTitleStrings() {
        return columnTitleStrings;
    }

    boolean getAscendingSortFlag() {
        return ascendingSortFlag;
    }

    DirectoryManagerInterface[] getDirectoryManagers() {
        return directoryManagers;
    }

    void setDirectoryManagers(DirectoryManagerInterface[] dirManagers) {
        this.directoryManagers = dirManagers;
    }
}
