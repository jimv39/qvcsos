/*
 * Copyright 2014-2021 JimVoris.
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JimVoris
 */
public class FileHistoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHistoryTest.class);

    public FileHistoryTest() {
    }

    /**
     * Test of toStream method, of class FileHistory.
     * @throws java.io.FileNotFoundException If we don't find an expected test file.
     */
    @Test
    public void testToStreamFromStream() throws FileNotFoundException, IOException {
        LOGGER.info("testToStreamFromStream");
        File tempFile = File.createTempFile("fileHistory", "unitTest");
        FileHistory writeInstance = new FileHistory();
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {
            populateFileHistory(writeInstance);
            writeInstance.toStream(dataOutputStream);
        }
        assertTrue(tempFile.length() > 0);
        FileHistory readInstance = new FileHistory();
        try (FileInputStream fileInputStream = new FileInputStream(tempFile);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {
            readInstance.fromStream(dataInputStream);
        }
        assertTrue(writeInstance.equals(readInstance));
        tempFile.delete();
    }

    @Test
    public void testCopy() throws IOException {
        LOGGER.info("testCopy");
        File fromFile = File.createTempFile("fileHistory", "unitTestFrom");
        File toFile = File.createTempFile("fileHistory", "unitTestTo");
        FileHistory writeInstance = new FileHistory();
        try (FileOutputStream fileOutputStream = new FileOutputStream(fromFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {
            populateFileHistory(writeInstance);
            writeInstance.toStream(dataOutputStream);
        }
        assertTrue(fromFile.length() > 0);
        try (FileInputStream fileInputStream = new FileInputStream(fromFile);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
                FileOutputStream fileOutputStream = new FileOutputStream(toFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {
            FileHistory.copy(dataInputStream, dataOutputStream);
        }
        FileHistory readInstance = new FileHistory();
        try (FileInputStream fileInputStream = new FileInputStream(toFile);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {
            readInstance.fromStream(dataInputStream);
        }
        assertTrue(writeInstance.equals(readInstance));
        fromFile.delete();
        toFile.delete();
    }

    @Test
    public void testGetFileHistorySummary() throws IOException {
        LOGGER.info("testGetFileHistorySummary");
        File tempFile = File.createTempFile("fileHistory", "unitTestSummary");
        FileHistory writeInstance = new FileHistory();
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {
            populateFileHistory(writeInstance);
            writeInstance.toStream(dataOutputStream);
        }
        assertTrue(tempFile.length() > 0);
        Path path = tempFile.toPath();
        FileHistory summary = new FileHistory(path);
        FileHistorySummary historySummary = summary.getFileHistorySummary();
        assertTrue(historySummary.getHeader().equals(writeInstance.getHeader()));
        assertEquals(historySummary.getRevisionHeaderList().size(), writeInstance.getRevisionByIdMap().size());
    }

    /**
     * We want to verify that the 'natural' order of revisions in FileHistory puts the newest (i.e. highest revision id) at the start of the collection of revisions; with older
     * revisions following in reverse revisionId order.
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testRevisionOrdering() throws UnsupportedEncodingException {
        FileHistory fileHistory = new FileHistory();
        populateFileHistory(fileHistory);
        Integer revisionId = Integer.MAX_VALUE;
        for (Revision r : fileHistory.getRevisionByIdMap().values()) {
            assertTrue(r.getId() < revisionId);
            revisionId = r.getId();
        }
    }

    private void populateFileHistory(FileHistory instance) throws UnsupportedEncodingException {
        FileHistoryHeader header = new FileHistoryHeader();
        populateFileHistoryHeader(header);
        instance.setHeader(header);
        populateRevisions(instance);
    }

    private void populateFileHistoryHeader(FileHistoryHeader header) {
        header.setCreator("Unit Test");
        header.setDescription("Unit test file");
        header.setFileId(10);
        header.setIsBinaryFileFlag(false);
    }

    private void populateRevisions(FileHistory instance) throws UnsupportedEncodingException {
        Map<Integer, Revision> revisionMap = instance.getRevisionByIdMap();
        {
            Revision revision1 = new Revision();
            RevisionHeader revHeader1 = new RevisionHeader();
            byte[] buffer1 = "Test revision data".getBytes("UTF-8");

            populateRevisionHeader(revHeader1, buffer1, 1, -1);
            revision1.setHeader(revHeader1);
            revision1.setRevisionData(buffer1);
            revisionMap.put(revHeader1.getId(), revision1);
        }
        {
            Revision revision2 = new Revision();
            RevisionHeader revHeader2 = new RevisionHeader();
            byte[] buffer2 = "Test revision data2".getBytes("UTF-8");

            populateRevisionHeader(revHeader2, buffer2, 2, 1);
            revision2.setHeader(revHeader2);
            revision2.setRevisionData(buffer2);
            revisionMap.put(revHeader2.getId(), revision2);
        }

    }

    private void populateRevisionHeader(RevisionHeader revHeader, byte[] buffer, Integer revisionId, Integer ancestorRevisionId) throws UnsupportedEncodingException {
        revHeader.setId(revisionId);
        revHeader.setAncestorRevisionId(ancestorRevisionId);
        revHeader.setReverseDeltaRevisionId(-1);
        revHeader.setAttributes(new WorkfileAttributes());
        revHeader.setAuthor("Unit Test");
        revHeader.setCommitDate(new Date());
        revHeader.setWorkfileEditDate(new Date());
        revHeader.setCommitIdentifier(new CommitIdentifier(1, 1, 1, 1));
        revHeader.setCompressionType(CompressionType.NOT_COMPRESSED);
        revHeader.setDescription("Unit test");
        revHeader.setDataSize(buffer.length);
    }

}
