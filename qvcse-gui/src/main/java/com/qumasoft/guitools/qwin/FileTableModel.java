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

import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.MergedInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.Utility;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * File table model.
 *
 * @author Jim Voris
 */
public class FileTableModel extends AbstractFileTableModel {
    private static final long serialVersionUID = 5105921748667131281L;

    private final DecimalFormat longFormatter;
    private final DecimalFormat sizeFormatter;
    private final ImageIcon[] fileIcons;
    private final ImageIcon[] lockIcons;
    private final ImageIcon[] workfileIcons;
    private final JLabel jLabel;
    private Map<Comparable, MergedInfoInterface> sortedMap;
    private ArrayList<MergedInfoInterface> arrayList;

    FileTableModel() {
        this.jLabel = new JLabel();
        this.workfileIcons = new ImageIcon[]{new ImageIcon(ClassLoader.getSystemResource("images/workfile.png"), "Not controlled file")};
        this.lockIcons = new ImageIcon[] {
            new ImageIcon(ClassLoader.getSystemResource("images/lock.png"), "Locked file"),
            new ImageIcon(ClassLoader.getSystemResource("images/lockgreen.png"), "Locked file"),
            new ImageIcon(ClassLoader.getSystemResource("images/lockyellow.png"), "Locked file"),
            new ImageIcon(ClassLoader.getSystemResource("images/lockdelta.png"), "Locked file")
        };
        this.fileIcons = new ImageIcon[] {
            new ImageIcon(ClassLoader.getSystemResource("images/file.png"), "Normal file"),
            new ImageIcon(ClassLoader.getSystemResource("images/filegreen.png"), "Normal file"),
            new ImageIcon(ClassLoader.getSystemResource("images/fileyellow.png"), "Normal file"),
            new ImageIcon(ClassLoader.getSystemResource("images/filedelta.png"), "Normal file")
        };
        this.sizeFormatter = new DecimalFormat("###,###,###,###,###");
        this.longFormatter = new DecimalFormat("000000000000");
    }

    /**
     * Returns the number of records managed by the data source object. A <B>JTable</B> uses this method to determine how many rows it should create and display. This method should
     * be quick, as it is call by <B>JTable</B> quite frequently.
     *
     * @return the number or rows in the model
     * @see #getColumnCount
     */
    @Override
    public synchronized int getRowCount() {
        int rowCount = 0;
        if (sortedMap != null) {
            rowCount = sortedMap.size();
        }
        return rowCount;
    }

    /**
     * Returns the number of columns managed by the data source object. A <B>JTable</B> uses this method to determine how many columns it should create and display on
     * initialization.
     *
     * @return the number or columns in the model
     * @see #getRowCount
     */
    @Override
    public int getColumnCount() {
        return getColumnTitleStrings().length;
    }

    /**
     * Returns the name of the column at <i>columnIndex</i>. This is used to initialize the table's column header name. Note, this name does not need to be unique. Two columns on a
     * table can have the same name.
     *
     * @param columnIndex the index of column
     * @return the name of the column
     */
    @Override
    public String getColumnName(int columnIndex) {
        return getColumnTitleStrings()[columnIndex];
    }

    /**
     * Returns the lowest common denominator Class in the column. This is used by the table to set up a default renderer and editor for the column.
     * @param columnIndex the column that we're interested in.
     * @return the common ancestor class of the object values in the model.
     */
    @Override
    public Class getColumnClass(int columnIndex) {
        return javax.swing.JLabel.class;
    }

    /**
     * Returns an attribute value for the cell at <I>columnIndex</I> and <I>rowIndex</I>.
     *
     * @param rowIndex the row whose value is to be looked up
     * @param columnIndex the column whose value is to be looked up
     * @return the value Object at the specified cell
     */
    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        jLabel.setText("");
        jLabel.setIcon(null);

        if (getDirectoryManagers() == null) {
            return jLabel;
        }

        if (getIsDirectoryManagersChanged()) {
            setDirectoryManagers(getDirectoryManagers(), false, false);
        }

        if (rowIndex >= arrayList.size()) {
            return jLabel;
        }

        MergedInfoInterface mergedInfo = getMergedInfo(rowIndex);
        // The following line needs to stay.
        FilteredFileTableModel filteredFileTableModel = (FilteredFileTableModel) QWinFrame.getQWinFrame().getRightFilePane().getModel();

        switch (columnIndex) {
            case FILENAME_COLUMN_INDEX:
                jLabel.setText(mergedInfo.getShortWorkfileName());
                jLabel.setIcon(deduceFileGraphic(mergedInfo));
                break;
            case FILE_STATUS_COLUMN_INDEX:
                jLabel.setText(mergedInfo.getStatusString());
                jLabel.setIcon(null);
                break;
            case LOCKEDBY_COLUMN_INDEX:
                jLabel.setText(mergedInfo.getLockedByString());
                jLabel.setIcon(null);
                break;
            case LASTCHECKIN_COLUMN_INDEX:
                String lastCheckInDateString;
                if (mergedInfo.getArchiveInfo() != null) {
                    Date lastCheckInDate = mergedInfo.getLastCheckInDate();
                    lastCheckInDateString = lastCheckInDate.toString();
                } else {
                    lastCheckInDateString = "";
                }
                jLabel.setText(lastCheckInDateString);
                jLabel.setIcon(null);
                break;
            case WORKFILEIN_COLUMN_INDEX:
                jLabel.setText(mergedInfo.getWorkfileInLocation());
                jLabel.setIcon(null);
                break;
            case FILESIZE_COLUMN_INDEX:
                if (mergedInfo.getWorkfile() != null) {
                    String formattedSize = sizeFormatter.format(mergedInfo.getWorkfileSize());
                    jLabel.setText(formattedSize);
                } else {
                    jLabel.setText("");
                }
                jLabel.setIcon(null);
                break;
            case LASTEDITBY_COLUMN_INDEX:
                jLabel.setText(mergedInfo.getLastEditBy());
                jLabel.setIcon(null);
                break;
            case APPENDED_PATH_INDEX:
                jLabel.setText(mergedInfo.getArchiveDirManager().getAppendedPath());
                jLabel.setIcon(null);
                break;
            default:
                throw new QVCSRuntimeException("Invalid column index: [" + columnIndex + "]");
        }

        return jLabel;
    }

    @Override
    public synchronized MergedInfoInterface getMergedInfo(int index) {
        MergedInfoInterface mergedInfo = null;
        if (index < arrayList.size()) {
            mergedInfo = arrayList.get(index);
        }
        return mergedInfo;
    }

    ArrayList getSortedCollection() {
        return arrayList;
    }

    private boolean getIsDirectoryManagersChanged() {
        boolean retVal = false;
        if (getDirectoryManagers() != null) {
            for (DirectoryManagerInterface directoryManager : getDirectoryManagers()) {
                if (directoryManager.getHasChanged()) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    static MergedInfo createBogusMergedInfo(MergedInfoInterface mergedInfo) {
        MergedInfo bogusMergedInfo = new MergedInfo(mergedInfo.getArchiveInfo(), mergedInfo.getArchiveDirManager(), mergedInfo.getProjectProperties(), mergedInfo.getUserName());
        bogusMergedInfo.setWorkfileInfo(mergedInfo.getWorkfileInfo());
        bogusMergedInfo.setArchiveInfo(null);
        return bogusMergedInfo;
    }

    private javax.swing.ImageIcon deduceFileGraphic(MergedInfoInterface mergedInfo) {
        if (QWinFrame.getQWinFrame().getUserProperties().getUseColoredFileIconsFlag()) {
            if (mergedInfo.getArchiveInfo() == null) {
                return workfileIcons[0];
            } else if (mergedInfo.getLockCount() == 0) {
                javax.swing.ImageIcon imageIcon = fileIcons[0];
                switch (mergedInfo.getStatusIndex()) {
                    case MergedInfoInterface.CURRENT_STATUS_INDEX:
                        imageIcon = fileIcons[1];
                        break;
                    case MergedInfoInterface.DIFFERENT_STATUS_INDEX:
                    case MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX:
                    case MergedInfoInterface.STALE_STATUS_INDEX:
                    case MergedInfoInterface.MISSING_STATUS_INDEX:
                        imageIcon = fileIcons[2];
                        break;
                    case MergedInfoInterface.YOUR_COPY_CHANGED_STATUS_INDEX:
                        // <editor-fold>  TODO
                        imageIcon = fileIcons[3];
                        // </editor-fold>
                        break;
                    default:
                        break;
                }
                return imageIcon;
            } else {
                javax.swing.ImageIcon imageIcon = lockIcons[0];
                switch (mergedInfo.getStatusIndex()) {
                    case MergedInfoInterface.CURRENT_STATUS_INDEX:
                        imageIcon = lockIcons[1];
                        break;
                    case MergedInfoInterface.DIFFERENT_STATUS_INDEX:
                    case MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX:
                    case MergedInfoInterface.STALE_STATUS_INDEX:
                        imageIcon = lockIcons[2];
                        break;
                    case MergedInfoInterface.YOUR_COPY_CHANGED_STATUS_INDEX:
                        // <editor-fold>  TODO
                        imageIcon = lockIcons[3];
                        // </editor-fold>
                        break;
                    default:
                        break;
                }
                return imageIcon;
            }
        } else {
            if (mergedInfo.getArchiveInfo() == null) {
                return workfileIcons[0];
            } else if (mergedInfo.getLockCount() == 0) {
                return fileIcons[0];
            } else {
                return lockIcons[0];
            }
        }
    }

    @Override
    public void setDirectoryManagers(final DirectoryManagerInterface[] managers, final boolean showProgressFlag, final boolean columnHeaderClickedFlag) {
        // Don't bother to do anything here while there is stuff in-progress.
        if (ClientTransactionManager.getInstance().getOpenTransactionCount() > 0) {
            return;
        }

        // Make sure we are not listeners to the old directory managers
        if (getDirectoryManagers() != null) {
            for (DirectoryManagerInterface m_DirectoryManager : getDirectoryManagers()) {
                if (m_DirectoryManager != null) {
                    m_DirectoryManager.removeChangeListener(this);
                }
            }
        }
        final FileTableModel finalThis = this;

        // Maybe display the progress dialog.
        ParentChildProgressDialog progressDialog = null;
        if (showProgressFlag) {
            progressDialog = OperationBaseClass.createParentProgressDialog("Updating file information...", 10);
            progressDialog.setAutoClose(false);
        }
        final ParentChildProgressDialog progressMonitor = progressDialog;
        final String fServerName = QWinFrame.getQWinFrame().getServerName();
        int transactionID = 0;

        // Show busy
        if (columnHeaderClickedFlag) {
            transactionID = ClientTransactionManager.getInstance().beginTransaction(fServerName);
        }
        final int fTransactionID = transactionID;

        Runnable worker = new Runnable() {

            @Override
            public void run() {
                try {
                    // Create a TreeMap that is sorted in the current sort order.
                    sortedMap = Collections.synchronizedMap(new TreeMap<Comparable, MergedInfoInterface>());

                    if (managers != null) {
                        if (showProgressFlag) {
                            // Run this on the Swing thread.
                            Runnable fireChange = new Runnable() {

                                @Override
                                public void run() {
                                    progressMonitor.initParentProgressBar(0, managers.length);
                                }
                            };
                            SwingUtilities.invokeLater(fireChange);
                        }

                        for (int i = 0; i < managers.length; i++) {
                            if (showProgressFlag && progressMonitor.getIsCancelled()) {
                                break;
                            }
                            DirectoryManagerInterface manager = managers[i];
                            synchronized (manager) {
                                manager.setHasChanged(false);

                                // Get the collection of merged info.
                                Collection collection = manager.getMergedInfoCollection();

                                // If there is anything in the collection yet... (at initialization
                                // the collection may not exist yet...).
                                if (collection != null) {
                                    int max = collection.size();
                                    int count = 0;
                                    if (showProgressFlag) {
                                        OperationBaseClass.updateParentChildProgressDialog(i, "Updating directory: " + manager.getAppendedPath(), progressMonitor);
                                        OperationBaseClass.initProgressDialog("Updating: " + manager.getAppendedPath(), 0, max, progressMonitor);
                                    }

                                    Iterator it = collection.iterator();
                                    while (it.hasNext()) {
                                        MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();
                                        if (passesFileFilters(mergedInfo)) {
                                            sortedMap.put(getSortKey(mergedInfo), mergedInfo);
                                        }
                                        if (showProgressFlag) {
                                            OperationBaseClass.updateProgressDialog(count++, "Updating information for: " + mergedInfo.getShortWorkfileName(), progressMonitor);
                                        }
                                    }
                                }

                                manager.addChangeListener(finalThis);
                            }
                        }
                    }
                } catch (Exception e) {
                    QWinUtility.logProblem(Level.WARNING, "Caught exception when updating file information: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                } finally {
                    synchronized (finalThis) {
                        setDirectoryManagers(managers);

                        // And put this in the array we associate with the screen display
                        arrayList = new ArrayList<>(sortedMap.values());

                        // Other threads can now proceed.
                        finalThis.notifyAll();
                    }

                    // Run the update on the Swing thread.
                    Runnable fireChange = new Runnable() {

                        @Override
                        public void run() {
                            QWinFrame.getQWinFrame().getStatusBar().updateStatusInfo();
                            if (showProgressFlag) {
                                progressMonitor.close();
                            }
                            if (columnHeaderClickedFlag) {
                                ClientTransactionManager.getInstance().endTransaction(fServerName, fTransactionID);
                            }
                            fireTableChanged(new javax.swing.event.TableModelEvent(FileTableModel.this));
                        }
                    };
                    SwingUtilities.invokeLater(fireChange);
                }
            }
        };
        // Put all this on a separate worker thread.
        Thread workerThread = new Thread(worker);

        // Wait for the bulk of the work to get done here
        synchronized (finalThis) {
            try {
                workerThread.start();
                finalThis.wait();
            } catch (InterruptedException e) {
                QWinUtility.logProblem(Level.WARNING, "Caught Interrupted exception waiting on setDirectoryManagers worker thread: " + e.getLocalizedMessage());
            }
        }
    }

    private Comparable getSortKey(MergedInfoInterface mergedInfo) {
        int column = getSortColumnInteger();
        String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
        Comparable sortKey = mergedInfo.getMergedInfoKey() + appendedPath;

        FilteredFileTableModel filteredFileTableModel = (FilteredFileTableModel) QWinFrame.getQWinFrame().getRightFilePane().getModel();
        FilterCollection filterCollection = filteredFileTableModel.getFilterCollection();

        switch (column) {
            case LOCKEDBY_COLUMN_INDEX:
                // Sort by locked by, then status, then filename
                if (mergedInfo.getLockedByString().length() > 0) {
                    sortKey = "000000" + mergedInfo.getLockedByString() + mergedInfo.getStatusString() + mergedInfo.getMergedInfoKey() + appendedPath;
                } else {
                    sortKey = mergedInfo.getStatusValue() + mergedInfo.getMergedInfoKey() + appendedPath;
                }
                break;
            case FILE_STATUS_COLUMN_INDEX:
                sortKey = mergedInfo.getStatusValue() + mergedInfo.getMergedInfoKey() + appendedPath;
                break;
            case LASTCHECKIN_COLUMN_INDEX:
                sortKey = Long.toString(Long.MAX_VALUE - mergedInfo.getLastCheckInDate().getTime()) + mergedInfo.getMergedInfoKey() + appendedPath;
                break;
            case WORKFILEIN_COLUMN_INDEX:
                // Need to sort by filename also, since the workfile in value may
                // be an empty string.
                sortKey = mergedInfo.getWorkfileInLocation() + mergedInfo.getMergedInfoKey() + appendedPath;
                break;
            case FILESIZE_COLUMN_INDEX:
                // Need to sort by filename also, since the workfile size may
                // be an empty string.
                String sizeString;
                if (mergedInfo.getWorkfile() == null) {
                    sizeString = "";
                } else {
                    long sortableSize = Long.MAX_VALUE - mergedInfo.getWorkfileSize();
                    sizeString = longFormatter.format(sortableSize);
                }
                sortKey = sizeString + mergedInfo.getMergedInfoKey() + appendedPath;
                break;
            case LASTEDITBY_COLUMN_INDEX:
                sortKey = mergedInfo.getLastEditBy() + mergedInfo.getMergedInfoKey() + appendedPath;
                break;
            case APPENDED_PATH_INDEX:
                sortKey = appendedPath + "/" + mergedInfo.getMergedInfoKey();
                break;
            default:
            case FILENAME_COLUMN_INDEX:
                break;
        }
        return new AscendDecendSortKey(sortKey, getAscendingSortFlag());
    }
}
