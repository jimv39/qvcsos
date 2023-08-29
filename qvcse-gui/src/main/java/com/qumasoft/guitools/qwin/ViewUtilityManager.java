/*   Copyright 2004-2023 Jim Voris
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

import com.qumasoft.guitools.qwin.dialog.GetViewUtilityCommandDialog;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.ViewUtilityCommandLineData;
import com.qumasoft.qvcslib.ViewUtilityFileExtensionCommandData;
import com.qumasoft.qvcslib.ViewUtilityResponseListenerInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestUpdateViewUtilityCommandData;
import static com.qumasoft.qvcslib.requestdata.ClientRequestUpdateViewUtilityCommandData.ADD_COMMAND_LINE_REQUEST;
import static com.qumasoft.qvcslib.requestdata.ClientRequestUpdateViewUtilityCommandData.ADD_UTILITY_ASSOCIATION_REQUEST;
import static com.qumasoft.qvcslib.requestdata.ClientRequestUpdateViewUtilityCommandData.REMOVE_UTILITY_ASSOCIATION_REQUEST;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseUpdateViewUtilityCommandLine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class to manage the utilities used to view workfiles for QVCS-Enterprise client.
 *
 * @author Jim Voris
 */
public final class ViewUtilityManager implements ViewUtilityResponseListenerInterface {

    private static final ViewUtilityManager VIEW_UTILITY_MANAGER = new ViewUtilityManager();

    /**
     * This map of maps contains the file extensions and their associated command line id for each different server that the client may connect to.
     */
    private final Map<String, Map<String, Integer>> commandLineByExtensionMaps = Collections.synchronizedMap(new TreeMap<>());
    /**
     * This map of maps contains commands that have been used keyed by commandLineId for each separate server. These are not associated with any specific file or extension,
     * but serves as a quick way for the user to re-use commands that they have used in the past.
     */
    private final Map<String, Map<Integer, String>> commandLineByIdMaps = Collections.synchronizedMap(new TreeMap<>());
    /**
     * This map of maps contains command lines with their associated command line id for each separate server. It's used to see if a command line is already known.
     */
    private final Map<String, Map<String, Integer>> commandLineByCommandLineMaps = Collections.synchronizedMap(new TreeMap<>());

    /** Transport proxy map keyed by server name. */
    private Map<String, TransportProxyInterface> transPortProxyMap = Collections.synchronizedMap(new TreeMap<>());

    /**
     * Creates a new instance of ViewUtilityManager.
     */
    private ViewUtilityManager() {
    }

    /**
     * Get the view utility manager singleton.
     * @return the view utility manager singleton.
     */
    public static ViewUtilityManager getInstance() {
        return VIEW_UTILITY_MANAGER;
    }

    /**
     * Initialize the view utility manager.
     * @param serverResponseLogin the login response which has view utility info.
     * @param transportProxy our connection to the server.
     */
    public void initialize(ServerResponseLogin serverResponseLogin, TransportProxyInterface transportProxy) {
        String serverName = serverResponseLogin.getServerName();
        transPortProxyMap.put(serverName, transportProxy);
        TransportProxyFactory.getInstance().addViewUtilityResponseListener(this);
        List<ViewUtilityCommandLineData> commandLineList = serverResponseLogin.getViewUtilityCommandLineList();
        Map<Integer, String> commandLineByIdMap = commandLineByIdMaps.get(serverName);
        Map<String, Integer> commandLineByCommandLineMap = commandLineByCommandLineMaps.get(serverName);
        if (commandLineByIdMap == null) {
            commandLineByIdMap = Collections.synchronizedMap(new TreeMap<>());
            commandLineByIdMaps.put(serverName, commandLineByIdMap);
            commandLineByCommandLineMap = Collections.synchronizedMap(new TreeMap<>());
            commandLineByCommandLineMaps.put(serverName, commandLineByCommandLineMap);
        }
        for (ViewUtilityCommandLineData commandLineData : commandLineList) {
            commandLineByIdMap.put(commandLineData.getCommandLineId(), commandLineData.getCommandLine());
            commandLineByCommandLineMap.put(commandLineData.getCommandLine(), commandLineData.getCommandLineId());
        }
        List<ViewUtilityFileExtensionCommandData> extensionCommandList = serverResponseLogin.getViewUtilityFileExtensionCommandDataList();
        Map<String, Integer> commandLineByExtensionMap = commandLineByExtensionMaps.get(serverName);
        if (commandLineByExtensionMap == null) {
            commandLineByExtensionMap = Collections.synchronizedMap(new TreeMap<>());
            commandLineByExtensionMaps.put(serverName, commandLineByExtensionMap);
        }
        for (ViewUtilityFileExtensionCommandData extensionCommand : extensionCommandList) {
            commandLineByExtensionMap.put(extensionCommand.getFileExtension(), extensionCommand.getCommandLineId());
        }
    }

    /**
     * Lookup the utility to be used for viewing the given workfile.
     * @param fullWorkfileName the full workfile name.
     * @return the command line to use to view the given workfile.
     */
    public String[] getViewUtilityCommandLine(String fullWorkfileName) {
        String serverName = QWinFrame.getQWinFrame().getServerName();
        // Look to see if there is a command for this file's extension.
        String extension = Utility.getFileExtension(fullWorkfileName);
        Map<String, Integer> commandLineByExtensionMap = commandLineByExtensionMaps.get(serverName);
        Map<Integer, String> commandLineByIdMap = commandLineByIdMaps.get(serverName);
        String command = null;
        Integer commandId = commandLineByExtensionMap.get(extension);
        if (commandId != null) {
            command = commandLineByIdMap.get(commandId);
        }
        String[] commandLine;

        // If the command line is still null, we need to ask the user to provide
        // a command for us.
        if (command == null) {
            commandLine = getCommandLine(fullWorkfileName, extension);
        } else {
            commandLine = new String[2];
            commandLine[0] = command;
            commandLine[1] = fullWorkfileName;
        }
        return commandLine;
    }

    /**
     * Return true if there is a utility associated with the given workfile.
     * @param fullWorkfileName the full workfile name.
     * @return true if there is a utility known for the given workfile; false otherwise.
     */
    public boolean getHasAssociatedUtility(final String fullWorkfileName) {
        String serverName = QWinFrame.getQWinFrame().getServerName();
        boolean retVal = false;

        // Look to see if there is a command for this file's extension.
        String extension = Utility.getFileExtension(fullWorkfileName);
        Map<String, Integer> commandLineByExtensionMap = commandLineByExtensionMaps.get(serverName);
        Integer commandLine = commandLineByExtensionMap.get(extension);
        if (commandLine != null) {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Remove the association between the file extension type, and a utility.
     * @param fullWorkfileName the full workfile name.
     */
    public void removeUtilityAssociation(final String fullWorkfileName) {
        String serverName = QWinFrame.getQWinFrame().getServerName();
        String extension = Utility.getFileExtension(fullWorkfileName);
        Map<String, Integer> commandLineByExtensionMap = commandLineByExtensionMaps.get(serverName);
        Map<Integer, String> commandLineByIdMap = commandLineByIdMaps.get(serverName);
        String commandLine = commandLineByIdMap.get(commandLineByExtensionMap.get(extension));
        updateDatabaseToRemoveUtilityAssociation(serverName, extension, commandLine);
        commandLineByExtensionMap.remove(extension);
    }

    private String[] getCommandLine(String fullWorkfileName, String extension) {
        String serverName = QWinFrame.getQWinFrame().getServerName();
        String[] existingCommands = getExistingCommands();
        GetViewUtilityCommandDialog getUtilityCommandDialog = new GetViewUtilityCommandDialog(QWinFrame.getQWinFrame(), true, existingCommands);
        getUtilityCommandDialog.setVisible(true);
        String[] commandLine = null;

        if (getUtilityCommandDialog.getIsOK()) {
            String selectedUtility = getUtilityCommandDialog.getSelectedUtility();

            // See if this is a new command line. If it is, we need to add it to our local maps,
            // and add it to the database.
            Map<String, Integer> commandLineByCommandLineMap = commandLineByCommandLineMaps.get(serverName);
            if (!commandLineByCommandLineMap.containsKey(selectedUtility)) {
                // Update the database. Receipt of the response will update our local maps.
                updateDatabaseWithAddedCommandLine(serverName, selectedUtility, extension, getUtilityCommandDialog.getUseForFilesOfThisExtensionFlag());
            } else {
                // The command already exists. See if we need to add as the default for the extension.
                if (getUtilityCommandDialog.getUseForFilesOfThisExtensionFlag()) {
                    updateDatabaseToAddUtilityAssociation(serverName, selectedUtility, extension);
                }
            }

            commandLine = new String[2];
            commandLine[0] = selectedUtility;
            commandLine[1] = fullWorkfileName;
        }

        return commandLine;
    }

    private String[] getExistingCommands() {
        String serverName = QWinFrame.getQWinFrame().getServerName();
        List<String> existingCommandList = new ArrayList<>();
        Map<Integer, String> commandLineByIdMap = commandLineByIdMaps.get(serverName);
        for (String commandLine : commandLineByIdMap.values()) {
            existingCommandList.add(commandLine);
        }

        return existingCommandList.toArray(String[]::new);
    }

    private void updateDatabaseWithAddedCommandLine(String serverName, String selectedUtility, String extension, boolean useForFilesOfThisExtensionFlag) {
        ClientRequestUpdateViewUtilityCommandData request = new ClientRequestUpdateViewUtilityCommandData();
        String userName = QWinFrame.getQWinFrame().getLoggedInUserName();
        request.setRequestType(ADD_COMMAND_LINE_REQUEST);
        request.setCommandLine(selectedUtility);
        request.setExtension(extension);
        request.setAssociateCommandWithExtension(useForFilesOfThisExtensionFlag);
        request.setServerName(serverName);
        request.setUserName(userName);
        request.setClientComputerName(Utility.getComputerName());
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transPortProxyMap.get(serverName));
        SynchronizationManager.getSynchronizationManager().waitOnToken(transPortProxyMap.get(serverName), request);
        ClientTransactionManager.getInstance().sendEndTransaction(transPortProxyMap.get(serverName), transactionID);
    }

    private void updateDatabaseToRemoveUtilityAssociation(String serverName, String extension, String commandLine) {
        ClientRequestUpdateViewUtilityCommandData request = new ClientRequestUpdateViewUtilityCommandData();
        String userName = QWinFrame.getQWinFrame().getLoggedInUserName();
        request.setRequestType(REMOVE_UTILITY_ASSOCIATION_REQUEST);
        request.setExtension(extension);
        request.setServerName(serverName);
        request.setUserName(userName);
        request.setCommandLine(commandLine);
        request.setClientComputerName(Utility.getComputerName());
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transPortProxyMap.get(serverName));
        SynchronizationManager.getSynchronizationManager().waitOnToken(transPortProxyMap.get(serverName), request);
        ClientTransactionManager.getInstance().sendEndTransaction(transPortProxyMap.get(serverName), transactionID);
    }

    private void updateDatabaseToAddUtilityAssociation(String serverName, String selectedUtility, String extension) {
        ClientRequestUpdateViewUtilityCommandData request = new ClientRequestUpdateViewUtilityCommandData();
        String userName = QWinFrame.getQWinFrame().getLoggedInUserName();
        request.setRequestType(ADD_UTILITY_ASSOCIATION_REQUEST);
        request.setCommandLine(selectedUtility);
        request.setExtension(extension);
        request.setAssociateCommandWithExtension(Boolean.TRUE);
        request.setServerName(serverName);
        request.setUserName(userName);
        request.setClientComputerName(Utility.getComputerName());
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transPortProxyMap.get(serverName));
        SynchronizationManager.getSynchronizationManager().waitOnToken(transPortProxyMap.get(serverName), request);
        ClientTransactionManager.getInstance().sendEndTransaction(transPortProxyMap.get(serverName), transactionID);
    }

    @Override
    public void notifyViewUtilityResponse(ServerResponseUpdateViewUtilityCommandLine response) {
        String serverName = response.getServerName();
        Map<String, Integer> commandLineByExtensionMap = commandLineByExtensionMaps.get(serverName);
        Map<Integer, String> commandLineByIdMap = commandLineByIdMaps.get(serverName);
        Map<String, Integer> commandLineByCommandLineMap = commandLineByCommandLineMaps.get(serverName);

        switch (response.getRequestType()) {
            case ADD_COMMAND_LINE_REQUEST:
                commandLineByIdMap.put(response.getCommandLineId(), response.getCommandLine());
                commandLineByCommandLineMap.put(response.getCommandLine(), response.getCommandLineId());
                if (response.getAssociateCommandWithExtension()) {
                    commandLineByExtensionMap.put(response.getExtension(), response.getCommandLineId());
                }
                break;
            case REMOVE_UTILITY_ASSOCIATION_REQUEST:
                commandLineByExtensionMap.remove(response.getExtension());
                break;
            case ADD_UTILITY_ASSOCIATION_REQUEST:
                commandLineByExtensionMap.put(response.getExtension(), response.getCommandLineId());
                break;
            default:
                throw new QVCSRuntimeException("Invalid response request type: " + response.getRequestType());
        }
    }
}
