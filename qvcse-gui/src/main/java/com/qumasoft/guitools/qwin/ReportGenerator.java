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

import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.guitools.qwin.filefilter.FileFilterInterface;
import com.qumasoft.guitools.qwin.operation.OperationBaseClass;
import com.qumasoft.guitools.qwin.revisionfilter.FilteredRevisionInfo;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterFactory;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterInterface;
import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Report generator. Create .html reports. This is a singleton.
 * @author Jim Voris
 */
public final class ReportGenerator {

    private static final ReportGenerator REPORT_GENERATOR = new ReportGenerator();
    private FilteredRevisionInfo previousFilteredRevisionInfo;
    private List<RevisionFilterInterface> revisionFilterArrayList;
    private int revisionCount = 0;
    private final String fileSeparator;
    private final String startParagraph;
    private final String endParagraph;
    private String reportFileName;
    private int fileCount = 0;
    private int totalRevisionCount = 0;
    private final String reportHeader;
    private final DecimalFormat longFormatter;

    /**
     * Creates a new instance of ReportGenerator.
     */
    private ReportGenerator() {
        this.fileSeparator = "\n<hr>\n<p>";
        this.startParagraph = "<p><b>";
        this.endParagraph = "</b></p>";
        this.longFormatter = new DecimalFormat("000000000000");
        this.reportHeader = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><head><title>QVCS Enterprise Report</title></head><body bgcolor=\"#FFFFFF\">";
    }

    /**
     * Get the report generator singleton.
     * @return the report generator singleton.
     */
    public static ReportGenerator getReportGenerator() {
        return REPORT_GENERATOR;
    }

    synchronized void generateReport() {
        previousFilteredRevisionInfo = null;
        revisionCount = 0;
        final ProgressDialog progressDialog = OperationBaseClass.createProgressDialog("Generate Report", 10);
        progressDialog.setAutoClose(false);

        Runnable worker = () -> {
            Collection revisions = collectRevisions(progressDialog);
            if (revisions != null) {
                reportRevisions(progressDialog, revisions);
                progressDialog.close();

                // If the user has defined a viewer for .html files, then view
                // the report using that utility.
                java.io.File reportFilename = new java.io.File(reportFileName);
                String canonicalReportFileName;
                try {
                    canonicalReportFileName = reportFilename.getCanonicalPath();
                } catch (IOException e) {
                    // Could not get the canonical name for the give report
                    // file.  I'm not sure how this can happen, but report
                    // the problem to the user.
                    final String errorMessage = "There was a problem in figuring out the report filename: " + e.getMessage();

                    // Run the update on the Swing thread.
                    Runnable update = () -> {
                        JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), errorMessage, "Report Generation Error", JOptionPane.WARNING_MESSAGE);
                    };
                    SwingUtilities.invokeLater(update);
                    return;
                }
                Utility.openURL(canonicalReportFileName);
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }

    private Collection collectRevisions(final ProgressDialog progressDialog) {
        FilteredFileTableModel filteredFileTableModel = (FilteredFileTableModel) QWinFrame.getQWinFrame().getFileTable().getModel();
        ArrayList sortedMergedInfoCollection = filteredFileTableModel.getSortedCollection();
        TreeMap<Comparable, FilteredRevisionInfo> sortedCollection = new TreeMap<>();

        // Build the collection of filters based on the set of file filter collection.
        buildRevisionFilterCollection(filteredFileTableModel);

        // Create the revision collection.
        Iterator mergedInfoIterator = sortedMergedInfoCollection.iterator();
        OperationBaseClass.initProgressDialog("Collecting revision information: ", 0, sortedMergedInfoCollection.size(), progressDialog);

        // Zero out the counters we use, so we can report totals in the report header.
        fileCount = 0;
        totalRevisionCount = 0;

        int k = 0;
        while (mergedInfoIterator.hasNext() && !progressDialog.getIsCancelled()) {
            MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoIterator.next();
            ArchiveInfoInterface archiveInfo = mergedInfo.getArchiveInfo();
            OperationBaseClass.updateProgressDialog(k++, "Working with: " + mergedInfo.getShortWorkfileName(), progressDialog);
            if (archiveInfo != null) {
                fileCount++;
                LogfileInfo logfileInfo = archiveInfo.getLogfileInfo();
                int revCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
                for (int i = 0; i < revCount; i++) {
                    RevisionHeader revisionHeader = logfileInfo.getRevisionInformation().getRevisionHeader(i);
                    FilteredRevisionInfo filteredRevisionInfo = new FilteredRevisionInfo(mergedInfo, revisionHeader, i);
                    if (passesRevisionFilters(filteredRevisionInfo)) {
                        sortedCollection.put(getSortKey(filteredRevisionInfo), filteredRevisionInfo);
                        totalRevisionCount++;
                    }
                }
            }
        }
        if (progressDialog.getIsCancelled()) {
            return null;
        } else {
            return sortedCollection.values();
        }
    }

    private void reportRevisions(final ProgressDialog progressDialog, Collection revisions) {
        OutputStream outputStream = null;
        try {
            outputStream = createOutputStream();
            writeReportHeader(outputStream);
            Iterator it = revisions.iterator();
            int size = revisions.size();
            int k = 0;
            OperationBaseClass.initProgressDialog("Generating report: ", 0, size, progressDialog);
            while (it.hasNext()) {
                FilteredRevisionInfo filteredRevisionInfo = (FilteredRevisionInfo) it.next();
                MergedInfoInterface mergedInfo = filteredRevisionInfo.getMergedInfo();
                OperationBaseClass.updateProgressDialog(k++, "Working with: " + mergedInfo.getShortWorkfileName(), progressDialog);
                writeRevision(outputStream, filteredRevisionInfo);
            }
        } catch (java.io.IOException e) {
            warnProblem(Utility.expandStackTraceToString(e));
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (java.io.IOException e) {
                    warnProblem(Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    private OutputStream createOutputStream() throws java.io.IOException {
        FileOutputStream fileStream;

        // The output stream has a file name based on the current date/time.
        Calendar now = Calendar.getInstance();
        int month = 1 + now.get(Calendar.MONTH);
        String dateTime = now.get(Calendar.YEAR) + "_"
                + month + "_"
                + now.get(Calendar.DAY_OF_MONTH) + "_"
                + now.get(Calendar.HOUR_OF_DAY) + "_"
                + now.get(Calendar.MINUTE) + "_"
                + now.get(Calendar.SECOND);
        reportFileName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_REPORTS_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_REPORT_NAME_PREFIX + dateTime + ".html";
        File reportFile = new File(reportFileName);

        // Make sure the needed directories exists
        if (!reportFile.getParentFile().exists()) {
            reportFile.getParentFile().mkdirs();
        }
        fileStream = new FileOutputStream(reportFile);
        return fileStream;
    }

    private void writeReportHeader(OutputStream outputStream) throws java.io.IOException {
        outputStream.write(reportHeader.getBytes());
        FilteredFileTableModel filteredFileTableModel = (FilteredFileTableModel) QWinFrame.getQWinFrame().getFileTable().getModel();
        Date now = new Date();

        // Note the project name, and the sort order.
        StringBuilder projectDescription = new StringBuilder("<p><h2>");
        projectDescription.append("Project: <i>").append(QWinFrame.getQWinFrame().getProjectName()).append("</i></h2>\n");
        projectDescription.append("Report generated: <i>").append(now.toString()).append("</i><br>\n");
        projectDescription.append("Appended Path: <i>").append(QWinFrame.getQWinFrame().getAppendedPath()).append("</i><br>\n");
        String recurseFlag;
        if (QWinFrame.getQWinFrame().getRecurseFlag()) {
            recurseFlag = "Yes";
        } else {
            recurseFlag = "No";
        }
        projectDescription.append("Recurse Flag: <i>").append(recurseFlag).append("</i><br>\n");
        String ascendingFlag;
        if (filteredFileTableModel.getAscendingFlag()) {
            ascendingFlag = " (Ascending)";
        } else {
            ascendingFlag = " (Decending)";
        }
        projectDescription.append("Sort Order: <i>").append(filteredFileTableModel.getSortColumn()).append(ascendingFlag).append("</i><br>\n");
        projectDescription.append("File Count: <i>").append(Integer.toString(fileCount)).append("</i><br>\n");
        projectDescription.append("Revision Count: <i>").append(Integer.toString(totalRevisionCount)).append("</i><br></p>\n");
        outputStream.write(projectDescription.toString().getBytes());

        // Include a description of the filters that are active.
        FilterCollection fileFilterCollection = filteredFileTableModel.getFilterCollection();
        StringBuilder filterDescriptions = new StringBuilder("<p><b>Active Filter Collection: <i>" + fileFilterCollection.getCollectionName() + "</i></b><br>\n");
        FileFilterInterface[] fileFilters = fileFilterCollection.listFilters();
        for (FileFilterInterface fileFilter : fileFilters) {
            filterDescriptions.append("\tType: <i>").append(fileFilter.getFilterType()).append("</i> Criteria: <i>").append(fileFilter.getFilterData()).append("</i><br>\n");
        }
        filterDescriptions.append("</p>\n<hr>\n");
        outputStream.write(filterDescriptions.toString().getBytes());
    }

    private void writeRevision(OutputStream outputStream, FilteredRevisionInfo filteredRevisionInfo) throws java.io.IOException {
        MergedInfoInterface previousMergedInfo = null;
        MergedInfoInterface mergedInfo = filteredRevisionInfo.getMergedInfo();
        if (previousFilteredRevisionInfo != null) {
            previousMergedInfo = previousFilteredRevisionInfo.getMergedInfo();
        }

        if (previousMergedInfo != null) {
            if (previousMergedInfo != mergedInfo) {
                writeFileSeparator(outputStream);
                outputStream.write(startParagraph.getBytes());
                String longWorkfileName = mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator + mergedInfo.getArchiveInfo().getShortWorkfileName();
                outputStream.write(longWorkfileName.getBytes());
                outputStream.write(endParagraph.getBytes());
            }
        } else {
            outputStream.write(startParagraph.getBytes());
            String longWorkfileName = mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator + mergedInfo.getArchiveInfo().getShortWorkfileName();
            outputStream.write(longWorkfileName.getBytes());
            outputStream.write(endParagraph.getBytes());
        }

        StringBuilder revisionInfo = new StringBuilder();
        revisionInfo.append("\n<p><i><u>Revision: ").append(filteredRevisionInfo.getRevisionHeader().getRevisionString()).append("</u></i><br>");
        revisionInfo.append("Checkin time: ").append(filteredRevisionInfo.getRevisionHeader().getCheckInDate().toString()).append("<br>");
        revisionInfo.append("Workfile edit date: ").append(filteredRevisionInfo.getRevisionHeader().getEditDate().toString()).append("<br>");
        AccessList accessList = new AccessList(filteredRevisionInfo.getMergedInfo().getLogfileInfo().getLogFileHeaderInfo().getModifierList());
        String revisionCreator = accessList.indexToUser(filteredRevisionInfo.getRevisionHeader().getCreatorIndex());
        revisionInfo.append("Created by: ").append(revisionCreator).append("<br>");
        revisionInfo.append(filteredRevisionInfo.getRevisionHeader().getRevisionDescription()).append("</p>\n");
        outputStream.write(revisionInfo.toString().getBytes());

        previousFilteredRevisionInfo = filteredRevisionInfo;
    }

    private void buildRevisionFilterCollection(FilteredFileTableModel filteredFileTableModel) {
        // Get the file filter collection
        FilterCollection fileFilterCollection = filteredFileTableModel.getFilterCollection();
        FileFilterInterface[] fileFilters = fileFilterCollection.listFilters();
        revisionFilterArrayList = new ArrayList<>();
        for (FileFilterInterface fileFilter : fileFilters) {
            RevisionFilterInterface revisionFilter = RevisionFilterFactory.buildFilter(fileFilter.getFilterType(), fileFilter.getRawFilterData(), fileFilter.getIsANDFilter());
            if (revisionFilter != null) {
                revisionFilterArrayList.add(revisionFilter);
            }
        }
    }

    private boolean passesRevisionFilters(FilteredRevisionInfo filteredRevisionInfo) {
        // Default to passing filter
        boolean retVal = true;

        // Process the AND filters first...
        for (RevisionFilterInterface filter : revisionFilterArrayList) {
            if (filter.getIsANDFilter()) {
                boolean flag = filter.passesFilter(filteredRevisionInfo);
                if (!flag) {
                    retVal = false;
                    break;
                }
            }
        }

        if (retVal) {
            boolean flag = false;
            boolean orFilterFound = false;

            for (RevisionFilterInterface filter : revisionFilterArrayList) {
                if (filter.getIsORFilter()) {
                    orFilterFound = true;
                    if (filter.passesFilter(filteredRevisionInfo)) {
                        flag = true;
                        break;
                    }
                }
            }
            if (orFilterFound) {
                retVal = flag;
            }
        }

        return retVal;
    }

    private Comparable getSortKey(FilteredRevisionInfo filteredRevisionInfo) {
        FilteredFileTableModel filteredFileTableModel = (FilteredFileTableModel) QWinFrame.getQWinFrame().getFileTable().getModel();
        boolean ascendingFlag = filteredFileTableModel.getAscendingFlag();
        MergedInfoInterface mergedInfo = filteredRevisionInfo.getMergedInfo();
        RevisionHeader revHeader = filteredRevisionInfo.getRevisionHeader();

        int column = filteredFileTableModel.getSortColumnInteger();
        String appendedPath = mergedInfo.getArchiveDirManager().getAppendedPath();
        String sortKey = mergedInfo.getShortWorkfileName() + appendedPath + Integer.toString(revisionCount);

        switch (column) {
            case AbstractFileTableModel.LOCKEDBY_COLUMN_INDEX:
                // Sort by locked by, then status, then filename
                if (mergedInfo.getLockedByString().length() > 0) {
                    sortKey = "000000" + mergedInfo.getLockedByString() + mergedInfo.getStatusValue() + mergedInfo.getShortWorkfileName()
                            + appendedPath + Integer.toString(revisionCount);
                } else {
                    sortKey = mergedInfo.getStatusValue() + mergedInfo.getShortWorkfileName() + appendedPath + Integer.toString(revisionCount);
                }
                break;
            case AbstractFileTableModel.FILE_STATUS_COLUMN_INDEX:
                // Need to sort by filename also, since the locked by value may
                // be an empty string.
                sortKey = mergedInfo.getStatusValue() + mergedInfo.getShortWorkfileName() + appendedPath + Integer.toString(revisionCount);
                break;
            case AbstractFileTableModel.LASTCHECKIN_COLUMN_INDEX:
                sortKey = Long.toString(Long.MAX_VALUE - revHeader.getCheckInDate().getTime()) + mergedInfo.getShortWorkfileName() + appendedPath;
                break;
            case AbstractFileTableModel.WORKFILEIN_COLUMN_INDEX:
                // Need to sort by filename also, since the workfile in value may
                // be an empty string.
                sortKey = mergedInfo.getWorkfileInLocation() + mergedInfo.getShortWorkfileName() + appendedPath + Integer.toString(revisionCount);
                break;
            case AbstractFileTableModel.FILESIZE_COLUMN_INDEX:
                // Need to sort by filename also, since the workfile size may
                // be an empty string.
                String sizeString;
                if (mergedInfo.getWorkfile() == null) {
                    sizeString = "";
                } else {
                    long sortableSize = Long.MAX_VALUE - mergedInfo.getWorkfileSize();
                    sizeString = longFormatter.format(sortableSize);
                }
                sortKey = sizeString + mergedInfo.getShortWorkfileName() + appendedPath + Integer.toString(revisionCount);
                break;
            case AbstractFileTableModel.LASTEDITBY_COLUMN_INDEX:
                sortKey = mergedInfo.getLastEditBy() + mergedInfo.getShortWorkfileName() + appendedPath + Integer.toString(revisionCount);
                break;
            case AbstractFileTableModel.APPENDED_PATH_INDEX:
                sortKey = appendedPath + "/" + mergedInfo.getShortWorkfileName() + Integer.toString(revisionCount);
                break;
            default:
            case AbstractFileTableModel.FILENAME_COLUMN_INDEX:
                break;
        }
        revisionCount++;

        return new AscendDecendSortKey(sortKey, ascendingFlag);
    }

    private void writeFileSeparator(OutputStream outputStream) throws java.io.IOException {
        outputStream.write(fileSeparator.getBytes());
    }
}
