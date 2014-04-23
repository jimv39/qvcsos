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

import com.qumasoft.qvcslib.CompressionFactory;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.RevisionCompressionHeader;
import com.qumasoft.qvcslib.ZlibCompressor;
import com.qumasoft.server.filehistory.BehaviorContext;
import com.qumasoft.server.filehistory.CommitIdentifier;
import com.qumasoft.server.filehistory.CompressionType;
import com.qumasoft.server.filehistory.FileHistory;
import com.qumasoft.server.filehistory.FileHistorySummary;
import com.qumasoft.server.filehistory.Revision;
import com.qumasoft.server.filehistory.RevisionHeader;
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
     *
     * @param f the file containing file history.
     */
    public FileHistoryManager(File f) {
        this.fileHistory = null;
        this.fileHistoryFile = f;
    }

    /**
     * Read the {@link FileHistory}.
     *
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
        // TODO -- this is where we would need to check if this revision has a reverse delta id, and if so, we have to walk the hydration path in order to hydrate this
        // revision.
        result.setValue(revisionData);
        return true;
    }

    @Override
    public Integer storeRevision(FileHistorySummary summary, BehaviorContext context, Revision revisionToAdd) {
        if (revisionToAdd.getHeader().getAncestorRevisionId() != -1) {
            // Need to verify that the ancestor actually exists.
            Revision ancestor = fileHistory.getRevisionByIdMap().get(revisionToAdd.getHeader().getAncestorRevisionId());
            if (ancestor == null) {
                throw new QVCSRuntimeException("Ancestor not found: [" + revisionToAdd.getHeader().getAncestorRevisionId() + "]");
            }
        }
        if (revisionToAdd.getHeader().getReverseDeltaRevisionId() != -1) {
            // Need to verify that the reverse delta revision id actually exists.
            Revision reverseDeltaRevision = fileHistory.getRevisionByIdMap().get(revisionToAdd.getHeader().getReverseDeltaRevisionId());
            if (reverseDeltaRevision == null) {
                throw new QVCSRuntimeException("Reverse delta revision not found: [" + revisionToAdd.getHeader().getReverseDeltaRevisionId() + "]");
            }
        }
        // TODO -- this is where we would update the ancestor revision to point to this new revision as its reverse-delta revision, compute the delta and replace
        // that ancestor revision with the newly computed content.

        compressContent(revisionToAdd);
        fileHistory.getRevisionByIdMap().put(revisionToAdd.getId(), revisionToAdd);
        return revisionToAdd.getHeader().getCommitIdentifier().getCommitId();
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

    private void compressContent(Revision revisionToAdd) {
        byte[] revisionContent = revisionToAdd.getRevisionData();
        RevisionHeader revisionHeader = revisionToAdd.getHeader();
        RevisionCompressionHeader revisionCompressionHeader = new RevisionCompressionHeader();
        revisionCompressionHeader.setCompressionType(RevisionCompressionHeader.COMPRESS_ALGORITHM_2);
        revisionCompressionHeader.setInputSize(revisionContent.length);
        Compressor compressor = CompressionFactory.getCompressor(revisionCompressionHeader);
        if (compressor.compress(revisionContent)) {
            revisionHeader.setCompressionType(CompressionType.ZLIB_COMPRESSED);
            revisionToAdd.setRevisionData(compressor.getCompressedBuffer());
            revisionHeader.setDataSize(compressor.getCompressedBuffer().length);
        } else {
            revisionHeader.setCompressionType(CompressionType.NOT_COMPRESSED);
            revisionHeader.setDataSize(revisionContent.length);
            revisionToAdd.setRevisionData(revisionContent);
        }
    }

}
