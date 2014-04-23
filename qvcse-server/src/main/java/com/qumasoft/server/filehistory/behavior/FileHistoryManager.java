/*
 * Copyright 2014 JimVoris.
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
package com.qumasoft.server.filehistory.behavior;

import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.ZlibCompressor;
import com.qumasoft.server.filehistory.BehaviorContext;
import com.qumasoft.server.filehistory.CommitIdentifier;
import com.qumasoft.server.filehistory.CompressionType;
import com.qumasoft.server.filehistory.FileHistory;
import com.qumasoft.server.filehistory.FileHistorySummary;
import com.qumasoft.server.filehistory.Revision;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class supplies behavior for a {@link FileHistory} object. Use instances of this class to perform source control behaviors for a given {@link FileHistory} instance.
 *
 * @author Jim Voris
 */
public class FileHistoryManager implements SourceControlBehaviorInterface {

    private FileHistory fileHistory;
    private final File fileHistoryFile;

    /**
     * Create a {@link FileHistory} instance for the given file.
     * @param f the file containing file history.
     */
    public FileHistoryManager(File f) {
        this.fileHistory = null;
        this.fileHistoryFile = f;
    }

    /**
     * Read the {@link FileHistory}.
     * @return the {@link FileHistory} associated with this FileHistoryManager.
     * @throws IOException if the file doesn't exist; or if there are problems reading the file.
     */
    public FileHistory readFileHistory() throws IOException {
        if (this.fileHistory == null) {
            this.fileHistory = new FileHistory();
            if (this.fileHistoryFile.length() > 0) {
                try (FileInputStream fileInputStream = new FileInputStream(fileHistoryFile);
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {
                    fileHistory.fromStream(dataInputStream);
                }
            }
        }
        return this.fileHistory;
    }

    @Override
    public boolean fetchRevision(FileHistorySummary summary, Integer revisionId, MutableByteArray result) {
        Revision revision = fileHistory.getRevisionByIdMap().get(revisionId);
        byte[] revisionData;
        if (revision.getHeader().getCompressionType().equals(CompressionType.ZLIB_COMPRESSED)) {
            Compressor compressor = new ZlibCompressor();
            revisionData = compressor.expand(revision.getRevisionData());
        } else {
            revisionData = revision.getRevisionData();
        }
        result.setValue(revisionData);
        return true;
    }

    @Override
    public Integer storeRevision(FileHistorySummary summary, BehaviorContext context, Revision revisionToAdd) {
        fileHistory.getRevisions().add(revisionToAdd);
        fileHistory.getRevisionByIdMap().put(revisionToAdd.getId(), revisionToAdd);
        return revisionToAdd.getId();
    }

    @Override
    public boolean commit(CommitIdentifier commitIdentifier) {
        boolean flag = true;
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileHistoryFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {
            fileHistory.toStream(dataOutputStream);
        }
        catch (IOException ex) {
            Logger.getLogger(FileHistoryManager.class.getName()).log(Level.SEVERE, null, ex);
            flag = false;
        }
        return flag;
    }

}
