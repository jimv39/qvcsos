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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.FileTableModel;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import java.awt.Frame;
import javax.swing.JTable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for OperationGet.
 *
 * @author JimVoris
 */
public class OperationGetTest {

    public OperationGetTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of executeOperation method, of class OperationGet.
     * @param fileTable mocked JTable.
     * @param userLocationProperties mocked UserLocationProperties.
     * @param fileTableModel mocked FileTableModel.
     * @param mergedInfo mocked MergedInfo.
     * @param progressDialog mocked ProgressDialog.
     */
    @Test
    public void testExecuteOperation(@Mocked final JTable fileTable,
            @Mocked final UserLocationProperties userLocationProperties,
            @Mocked final FileTableModel fileTableModel,
            @Mocked final MergedInfoInterface mergedInfo,
            @Mocked final ProgressDialog progressDialog,
            @Mocked final ArchiveDirManagerProxy archiveDirManager,
            @Mocked final TransportProxyInterface transportProxy,
            @Mocked final ArchiveInfoInterface archiveInfo) throws InterruptedException, QVCSException {
        System.out.println("executeOperation");
        OperationGet operationGet = new OperationGet(fileTable, "TestServerName", "TestProjectName", "TestViewName",
                userLocationProperties, false);
        final int[] selectedRows = new int[]{1};
        new NonStrictExpectations() {
            {
                fileTable.getSelectedRows();
                result = selectedRows;
                fileTable.getModel();
                result = fileTableModel;
                fileTableModel.getMergedInfo(anyInt);
                result = mergedInfo;
                new ProgressDialog((Frame) any, true, 0, 1);
                result = progressDialog;
                progressDialog.setAction(anyString);
                mergedInfo.getArchiveDirManager();
                result = archiveDirManager;
                archiveDirManager.getTransportProxy();
                result = transportProxy;
                transportProxy.getServerProperties().getServerName();
                result = "UnitTest";
                transportProxy.write(any);
                mergedInfo.getArchiveInfo();
                result = archiveInfo;
                mergedInfo.getArchiveDirManager().getAppendedPath();
                result = "testDir";
                userLocationProperties.getWorkfileLocation(anyString, anyString, anyString);
                result = "/tmp/qvcsos/testProject/workfiles";
                mergedInfo.getShortWorkfileName();
                result = "testFile.txt";
                mergedInfo.getArchiveInfo().getShortWorkfileName();
                result = "testFile.txt";
                mergedInfo.getIsRemote();
                result = true;
                mergedInfo.getRevision((GetRevisionCommandArgs) any, anyString);
                result = true;
                progressDialog.close();
            }
        };
        operationGet.executeOperation();
        while (true) {
            if (operationGet.isWorkCompleted()) {
                break;
            }
            Thread.sleep(1000L);
        }
    }

}
