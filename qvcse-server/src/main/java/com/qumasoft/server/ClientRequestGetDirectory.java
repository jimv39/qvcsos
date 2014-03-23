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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetDirectoryData;
import com.qumasoft.qvcslib.LogFileOperationGetDirectoryCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationGetRevisionCommandArgs;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseGetRevision;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request get directory.
 * @author Jim Voris
 */
public class ClientRequestGetDirectory implements ClientRequestInterface, DirectoryOperationInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final Map<String, String> directoryMap = new TreeMap<>();
    private final ClientRequestGetDirectoryData request;

    /**
     * Creates a new instance of ClientRequestGetDirectory.
     *
     * @param data the request data.
     */
    public ClientRequestGetDirectory(ClientRequestGetDirectoryData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        DirectoryOperationHelper directoryOperationHelper = new DirectoryOperationHelper(this);
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        LogFileOperationGetDirectoryCommandArgs commandArgs = request.getCommandArgs();
        String appendedPath = request.getAppendedPath();
        try {
            if (0 == viewName.compareTo(QVCSConstants.QVCS_TRUNK_VIEW)) {
                // We have to do this directory at least...
                directoryMap.put(appendedPath, appendedPath);

                if (commandArgs.getRecurseFlag()) {
                    directoryOperationHelper.addChildDirectories(directoryMap, viewName, appendedPath, response);
                }

                if (commandArgs.getByDateFlag()) {
                    directoryOperationHelper.processDirectoryCollectionByDate(viewName, directoryMap, response, commandArgs.getByDateValue());
                } else if (commandArgs.getByLabelFlag()) {
                    directoryOperationHelper.processDirectoryCollectionByLabel(viewName, directoryMap, response, commandArgs.getLabelString());
                } else {
                    directoryOperationHelper.processDirectoryCollection(viewName, directoryMap, response);
                }
            } else {
                // TODO
                ServerResponseMessage message = new ServerResponseMessage("Directory level get is not supported for non-Trunk views.", projectName, viewName, appendedPath,
                        ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName("");
                returnObject = message;
            }
        } finally {
            LOGGER.log(Level.INFO, "Completed get directory for: [" + appendedPath + "]");
        }

        return returnObject;
    }

    @Override
    public ServerResponseInterface processFile(ArchiveDirManagerInterface archiveDirManager, ArchiveInfoInterface archiveInfo, String appendedPath,
            ServerResponseFactoryInterface response) {
        ServerResponseInterface resultObject;
        LogFileOperationGetRevisionCommandArgs commandArgs = new LogFileOperationGetRevisionCommandArgs();
        LogFileOperationGetDirectoryCommandArgs directoryCommandArgs = request.getCommandArgs();
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        FileInputStream fileInputStream = null;

        try {
            if ((archiveInfo != null) && (archiveInfo instanceof LogFile)) {
                LogFile logfile = (LogFile) archiveInfo;
                String fullWorkfileName = directoryCommandArgs.getWorkfileBaseDirectory() + File.separator + appendedPath + File.separator + archiveInfo.getShortWorkfileName();
                commandArgs.setUserName(directoryCommandArgs.getUserName());
                commandArgs.setOutputFileName(fullWorkfileName);
                commandArgs.setFullWorkfileName(fullWorkfileName);
                commandArgs.setShortWorkfileName(archiveInfo.getShortWorkfileName());

                if (directoryCommandArgs.getByLabelFlag()) {
                    commandArgs.setByLabelFlag(true);
                    commandArgs.setLabel(directoryCommandArgs.getLabelString());
                } else if (directoryCommandArgs.getByDateFlag()) {
                    commandArgs.setByDateFlag(true);
                    commandArgs.setByDateValue(directoryCommandArgs.getByDateValue());
                } else {
                    commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
                }

                java.io.File tempFile = java.io.File.createTempFile("QVCS", ".tmp");
                if (logfile.getRevision(commandArgs, tempFile.getAbsolutePath())) {
                    // Things worked.  Create a response object to contain the information the client needs.
                    ServerResponseGetRevision serverResponse = new ServerResponseGetRevision();

                    // Need to read the resulting file into a buffer that we can send to the client.
                    fileInputStream = new FileInputStream(tempFile);
                    byte[] buffer = new byte[(int) tempFile.length()];
                    fileInputStream.read(buffer);

                    serverResponse.setBuffer(buffer);
                    serverResponse.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logfile.getLogfileInfo(), File.separator, logfile.getIsObsolete(), logfile.getDefaultRevisionDigest(),
                            logfile.getShortWorkfileName(), archiveInfo.getIsOverlap()));
                    serverResponse.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverResponse.setShortWorkfileName(logfile.getShortWorkfileName());
                    serverResponse.setProjectName(projectName);
                    serverResponse.setViewName(viewName);
                    serverResponse.setAppendedPath(appendedPath);
                    serverResponse.setRevisionString(commandArgs.getRevisionString());
                    serverResponse.setLabelString(commandArgs.getLabel());
                    serverResponse.setOverwriteBehavior(directoryCommandArgs.getOverwriteBehavior());
                    serverResponse.setTimestampBehavior(directoryCommandArgs.getTimeStampBehavior());
                    serverResponse.setDirectoryLevelOperationFlag(true);
                    serverResponse.setDirectoryLevelTransactionID(request.getTransactionID());

                    // Figure out the timestamp that we send back.
                    ServerUtility.setTimestampData(logfile, serverResponse, directoryCommandArgs.getTimeStampBehavior());

                    // Send back the logfile info if it's needed for
                    // keyword expansion.
                    if (logfile.getAttributes().getIsExpandKeywords()) {
                        serverResponse.setLogfileInfo(logfile.getLogfileInfo());
                    }

                    // Send a message to indicate that we're getting the file.
                    ServerResponseMessage message = new ServerResponseMessage("Retrieving revision " + commandArgs.getRevisionString() + " for " + appendedPath + File.separator
                            + archiveInfo.getShortWorkfileName() + " from server.", projectName, viewName, appendedPath, ServerResponseMessage.MEDIUM_PRIORITY);
                    message.setShortWorkfileName(archiveInfo.getShortWorkfileName());
                    response.createServerResponse(message);

                    resultObject = serverResponse;
                } else {
                    // Log the error
                    if ((commandArgs.getFailureReason() != null) && (commandArgs.getFailureReason().length() > 0)) {
                        ServerResponseError error = new ServerResponseError("Failed to get revision for " + logfile.getShortWorkfileName() + ". "
                                + commandArgs.getFailureReason(), projectName, viewName, appendedPath);
                        resultObject = error;
                    } else {
                        ServerResponseError error = new ServerResponseError("Failed to get revision " + commandArgs.getRevisionString() + " for "
                                + logfile.getShortWorkfileName(), projectName, viewName, appendedPath);
                        resultObject = error;
                    }
                }
                tempFile.delete();
            } else {
                // Log a command error.
                ServerResponseError error = new ServerResponseError("Archive not found for " + commandArgs.getShortWorkfileName(), projectName, viewName, appendedPath);
                resultObject = error;
            }
        } catch (QVCSException e) {
            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            resultObject = message;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Caught exception on getDirectory: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            ServerResponseMessage message = new ServerResponseMessage(e.getLocalizedMessage(), projectName, viewName, appendedPath, ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(commandArgs.getShortWorkfileName());
            resultObject = message;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
        return resultObject;
    }

    @Override
    public String getProjectName() {
        return request.getProjectName();
    }
}
