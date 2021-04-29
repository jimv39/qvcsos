/*
 * Copyright 2014 Jim Voris.
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
package com.qumasoft.server.filehistory;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * File history. This class supplies file history behavior. For each file that exists under source control, there is an associated FileHistory object that captures the history of
 * changes made to that file. One of the key design goals of this class is that the revisions that compose the file can be sent piecemeal instead of having to send the entire file
 * to some listener. For example, suppose some merge activity requires just 3 revisions in order to perform the merge operation. We should be able to send just those 3 revisions to
 * the consumer of them, and the consumer can then use them to update their instance of a FileHistory object.
 * <p>
 * The intent is for this to be solely an entity class, with no behavior aside from storing and retrieving the data it contains. To actually retrieve a file revision, the
 * {@link FileHistoryManager} class will use the data contained here to build the requested file revision.
 * <p>
 * As a design choice, FileHistory <i>always</i> uses reverse-delta format for non-tip revisions. This means that all tip revisions, whether they are on the trunk, or whether they
 * are on a branch will have the full content of the workfile, instead of a forward delta. This differs from the original approach used in QVCS/QVCS-Pro where forward deltas were
 * used for branch tip revisions. The result will be a larger history file, but one that is much easier to navigate.
 *
 * @author Jim Voris
 */
public class FileHistory implements ToFromStreamInterface {

    private FileHistoryHeader header;
    private Map<Integer, Revision> revisionByIdMap;
    private Path fileHistoryPath;
    private FileHistoryComparator fileHistoryComparator;

    /**
     * Default constructor.
     */
    public FileHistory() {
        header = new FileHistoryHeader();
        fileHistoryComparator = new FileHistoryComparator();
        revisionByIdMap = new TreeMap<>(fileHistoryComparator);
    }

    /**
     * Constructor with Path.
     *
     * @param path identify the backing file for this FileHistory.
     */
    public FileHistory(Path path) {
        header = new FileHistoryHeader();
        fileHistoryPath = path;
        revisionByIdMap = new TreeMap<>();
    }

    @Override
    public void toStream(DataOutputStream o) throws IOException {
        getHeader().toStream(o);
        o.writeInt(revisionByIdMap.size());
        for (Revision revision : revisionByIdMap.values()) {
            revision.toStream(o);
        }
    }

    @Override
    public void fromStream(DataInputStream i) throws IOException {
        getHeader().fromStream(i);
        Integer revisionCount = i.readInt();
        revisionByIdMap = new TreeMap<>(fileHistoryComparator);
        for (int index = 0; index < revisionCount; index++) {
            Revision revision = new Revision();
            revision.fromStream(i);
            revisionByIdMap.put(revision.getId(), revision);
        }
    }

    /**
     * Read from the Path. This basically does the same thing as the {@link #fromStream(java.io.DataInputStream) } method, except it reads from the member Path location instead
     * of from a passed-in DataInputStream.
     * @throws IOException if there are problems reading from the file.
     */
    public void fromPath() throws IOException {
        if (fileHistoryPath != null) {
            File fileHistoryFile = fileHistoryPath.toFile();
            try (FileInputStream fileInputStream = new FileInputStream(fileHistoryFile);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                    DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {
                fromStream(dataInputStream);
            }
        } else {
            throw new QVCSRuntimeException("Undefined fileHistoryPath!");
        }
    }

    /**
     * Get the FileHistorySummary for this FileHistory.
     * @return the FileHistorySummary for this FileHistory.
     * @throws IOException if the file (defined by the path associated with this instance) does not exist, or if there are problems reading from the path.
     */
    public FileHistorySummary getFileHistorySummary() throws IOException {
        FileHistorySummary summary = null;
        if (fileHistoryPath != null) {
            File fileHistoryFile = fileHistoryPath.toFile();
            try (FileInputStream fileInputStream = new FileInputStream(fileHistoryFile);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                    DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {
                summary = summaryFromStream(dataInputStream);
            }
        } else {
            throw new QVCSRuntimeException("Undefined fileHistoryPath!");
        }
        return summary;
    }

    /**
     * Copy revision history from one input stream to an output stream. This method is meant to be used to copy FileHistory files in a way that doesn't require the whole history to
     * be on the heap at the same time.
     *
     * @param i the input stream from which we create the copy.
     * @param o the output stream to which we copy the data.
     * @throws IOException if there is an I/O problem.
     */
    public static void copy(DataInputStream i, DataOutputStream o) throws IOException {
        FileHistory from = new FileHistory();
        from.getHeader().fromStream(i);
        from.getHeader().toStream(o);

        Integer revisionCount = i.readInt();
        o.writeInt(revisionCount);
        Revision revision = new Revision();
        for (int index = 0; index < revisionCount; index++) {
            revision.fromStream(i);
            revision.toStream(o);
        }
    }

    private FileHistorySummary summaryFromStream(DataInputStream i) throws IOException {
        FileHistorySummary summary = new FileHistorySummary();
        summary.getHeader().fromStream(i);
        Integer revisionCount = i.readInt();
        List<RevisionHeader> headerList = new ArrayList<>(revisionCount);
        summary.setRevisionHeaderList(headerList);
        for (int index = 0; index < revisionCount; index++) {
            Revision revision = new Revision();
            revision.fromStream(i);
            headerList.add(revision.getHeader());
        }
        return summary;
    }

    /**
     * Get the file history header.
     * @return the file history header.
     */
    public FileHistoryHeader getHeader() {
        return header;
    }

    /**
     * Set the file history header.
     * @param h the file history header.
     */
    void setHeader(FileHistoryHeader h) {
        this.header = h;
    }

    /**
     * Get the map of revisions (keyed by revision id).
     * @return the map of revisions (keyed by revision id).
     */
    public Map<Integer, Revision> getRevisionByIdMap() {
        return revisionByIdMap;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.header);
        hash = 71 * hash + Objects.hashCode(this.revisionByIdMap);
        // </editor-fold>
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileHistory other = (FileHistory) obj;
        if (!Objects.equals(this.header, other.header)) {
            return false;
        }
        for (Revision r : this.revisionByIdMap.values()) {
            Revision o = other.revisionByIdMap.get(r.getId());
            if (!Objects.equals(r, o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Comparator so we can sort the revisions from high to low, so the newest revisions appear toward the beginning of the file, and older revisions are at the end.
     */
    static class FileHistoryComparator implements Comparator<Integer>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }

}
}
