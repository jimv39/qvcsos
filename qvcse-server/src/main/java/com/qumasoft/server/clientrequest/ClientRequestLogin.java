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
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.CommonFilterFile;
import com.qumasoft.qvcslib.CommonFilterFileCollection;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.UpdateManager;
import com.qumasoft.qvcslib.UserPropertyData;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.ViewUtilityCommandLineData;
import com.qumasoft.qvcslib.ViewUtilityFileExtensionCommandData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLoginData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.LicenseManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.FilterFileCollectionDAO;
import com.qvcsos.server.dataaccess.FilterFileDAO;
import com.qvcsos.server.dataaccess.FilterTypeDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.dataaccess.UserPropertyDAO;
import com.qvcsos.server.dataaccess.ViewUtilityByExtensionDAO;
import com.qvcsos.server.dataaccess.ViewUtilityCommandLineDAO;
import com.qvcsos.server.dataaccess.impl.FilterFileCollectionDAOImpl;
import com.qvcsos.server.dataaccess.impl.FilterFileDAOImpl;
import com.qvcsos.server.dataaccess.impl.FilterTypeDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.dataaccess.impl.UserDAOImpl;
import com.qvcsos.server.dataaccess.impl.UserPropertyDAOImpl;
import com.qvcsos.server.dataaccess.impl.ViewUtilityByExtensionDAOImpl;
import com.qvcsos.server.dataaccess.impl.ViewUtilityCommandLineDAOImpl;
import com.qvcsos.server.datamodel.FilterFile;
import com.qvcsos.server.datamodel.FilterFileCollection;
import com.qvcsos.server.datamodel.FilterType;
import com.qvcsos.server.datamodel.Project;
import com.qvcsos.server.datamodel.User;
import com.qvcsos.server.datamodel.UserProperty;
import com.qvcsos.server.datamodel.ViewUtilityCommandLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle client login requests.
 *
 * @author Jim Voris
 */
public class ClientRequestLogin extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestLogin.class);
    private boolean authenticationFailedFlag = false;
    private String message = null;
    private final DatabaseManager databaseManager;
    private final String schemaName;
    private final SourceControlBehaviorManager sourceControlBehaviorManager;

    /**
     * Creates a new instance of ClientLoginRequest.
     *
     * @param data an instance of the super class that contains command line arguments, etc.
     */
    public ClientRequestLogin(ClientRequestLoginData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        this.sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        setRequest(data);
    }

    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        AbstractServerResponse returnObject;
        LOGGER.info("ClientRequestLogin.execute user name: [{}]", getRequest().getUserName());
        ServerResponseLogin serverResponseLogin = new ServerResponseLogin();
        serverResponseLogin.setWebServerPort(AuthenticationManager.getAuthenticationManager().getWebServerPort());
        serverResponseLogin.setSyncToken(getRequest().getSyncToken());
        serverResponseLogin.setUserName(getRequest().getUserName());
        serverResponseLogin.setServerName(getRequest().getServerName());
        if (AuthenticationManager.getAuthenticationManager().authenticateUser(getRequest().getUserName(), getRequest().getPassword())) {
            AtomicReference<String> mutableMessage = new AtomicReference<>();
            if (LicenseManager.getInstance().loginUser(mutableMessage, getRequest().getUserName(), response.getClientIPAddress())) {
                ClientRequestLoginData clientRequestLoginData = (ClientRequestLoginData) getRequest();
                // Make sure the client version is one we support.
                // For now, we only support the same version as the server.
                if (clientRequestLoginData.getVersion().equals(QVCSConstants.QVCS_RELEASE_VERSION)) {
                    serverResponseLogin.setLoginResult(true);
                    serverResponseLogin.setVersionsMatchFlag(true);
                } else {
                    LOGGER.warn("Login for: " + getRequest().getUserName() + ". Client version [" + clientRequestLoginData.getVersion() + "] not supported.");
                    serverResponseLogin.setLoginResult(true);
                    serverResponseLogin.setVersionsMatchFlag(false);
                    serverResponseLogin.setFailureReason("Server version: '" + QVCSConstants.QVCS_RELEASE_VERSION + "' does not support client version: '"
                            + clientRequestLoginData.getVersion() + "'.");
                    // Put the client zip and the autoUpdate jar files into the response message so the client can perform the update.
                    serverResponseLogin.setClientZip(UpdateManager.getClientZipAsByteArray());
                    serverResponseLogin.setAutoUpdateJar(UpdateManager.getUpdateJarAsByteArray());
                }
            } else {
                serverResponseLogin.setLoginResult(false);
                serverResponseLogin.setFailureReason(mutableMessage.get());
            }
            message = mutableMessage.get();
        } else {
            LOGGER.info("Login failed for: [" + getRequest().getUserName() + "]. Invalid username/password.");
            serverResponseLogin.setLoginResult(false);
            serverResponseLogin.setFailureReason("Invalid username/password.");
            authenticationFailedFlag = true;
        }

        if (serverResponseLogin.getVersionsMatchFlag()) {
            // Populate the response with the user's remote properties...
            UserPropertyDAO userPropertyDAO = new UserPropertyDAOImpl(schemaName);
            List<UserProperty> userPropertyList = userPropertyDAO.findUserProperties(createUserAndComputerKey());
            List<UserPropertyData> userPropertyDataList = new ArrayList<>();
            for (UserProperty userProperty : userPropertyList) {
                UserPropertyData userPropertyData = new UserPropertyData();
                userPropertyData.setId(userProperty.getId());
                userPropertyData.setUserAndComputer(userProperty.getUserAndComputer());
                userPropertyData.setPropertyName(userProperty.getPropertyName());
                userPropertyData.setPropertyValue(userProperty.getPropertyValue());
                userPropertyDataList.add(userPropertyData);
            }
            serverResponseLogin.setUserPropertyList(userPropertyDataList);

            // Populate response with user's view utilities...
            ViewUtilityCommandLineDAO vuclDAO = new ViewUtilityCommandLineDAOImpl(schemaName);
            List<ViewUtilityCommandLine> viewUtilityCommandLineList = vuclDAO.findCommandLinesForUserComputer(createUserAndComputerKey());
            List<ViewUtilityCommandLineData> vucldList = new ArrayList<>();
            for (ViewUtilityCommandLine vucl : viewUtilityCommandLineList) {
                ViewUtilityCommandLineData vucld = new ViewUtilityCommandLineData();
                vucld.setCommandLine(vucl.getCommandLine());
                vucld.setCommandLineId(vucl.getId());
                vucldList.add(vucld);
            }
            serverResponseLogin.setViewUtilityCommandLineList(vucldList);

            // Populate response with user's file extension --> view utility id association.
            ViewUtilityByExtensionDAO vubeDAO = new ViewUtilityByExtensionDAOImpl(schemaName);
            List<ViewUtilityFileExtensionCommandData> vuclList = vubeDAO.findCommandLineExtensionList(createUserAndComputerKey());
            serverResponseLogin.setViewUtilityFileExtensionCommandDataList(vuclList);

            // Populate the response with the user's file filter collections...
            List<CommonFilterFileCollection> filterCollectionList = buildFilterCollectionList();
            serverResponseLogin.setFilterCollectionList(filterCollectionList);
        }
        returnObject = serverResponseLogin;
        returnObject.setSyncToken(getRequest().getSyncToken());
        sourceControlBehaviorManager.clearThreadLocals();
        return returnObject;
    }

    /**
     * Get the authentication failed flag.
     * @return the authentication failed flag.
     */
    public boolean getAuthenticationFailedFlag() {
        return authenticationFailedFlag;
    }

    /**
     * Get the message (for a login failure).
     * @return the message for a login failure.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return getRequest().getServerName();
    }

    private String createUserAndComputerKey() {
        ClientRequestLoginData rqst = (ClientRequestLoginData) getRequest();
        String key = Utility.createUserAndComputerKey(rqst.getUserName(), rqst.getClientComputerName());
        return key;
    }

    private List<CommonFilterFileCollection> buildFilterCollectionList() {
        List<CommonFilterFileCollection> filterCollectionList = new ArrayList<>();
        ClientRequestLoginData rqst = (ClientRequestLoginData) getRequest();
        String userName = rqst.getUserName();
        UserDAO userDAO = new UserDAOImpl(this.schemaName);
        User user = userDAO.findByUserName(userName);
        Map<Integer, String> projectIdMap = buildProjectIdMap();
        Map<Integer, String> filterTypeMap = buildFilterTypeMap();
        FilterFileCollectionDAO filterFileCollectionDAO = new FilterFileCollectionDAOImpl(this.schemaName);
        List<FilterFileCollection> filterFileCollectionList = filterFileCollectionDAO.findAllByUserId(user.getId());
        for (FilterFileCollection filterFileCollection : filterFileCollectionList) {
            CommonFilterFileCollection commonFilterFileCollection = new CommonFilterFileCollection();
            commonFilterFileCollection.setCollectionName(filterFileCollection.getCollectionName());
            commonFilterFileCollection.setBuiltInFlag(filterFileCollection.getBuiltInFlag());
            commonFilterFileCollection.setId(filterFileCollection.getId());
            commonFilterFileCollection.setAssociatedProjectId(filterFileCollection.getAssociatedProjectId());
            if (filterFileCollection.getAssociatedProjectId() != null && filterFileCollection.getAssociatedProjectId() != 0) {
                commonFilterFileCollection.setAssociatedProjectName(projectIdMap.get(filterFileCollection.getAssociatedProjectId()));
            } else {
                commonFilterFileCollection.setAssociatedProjectName(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME);
            }
            commonFilterFileCollection.setUserId(user.getId());
            commonFilterFileCollection.setFilterFileList(buildFilterList(filterFileCollection.getId(), filterTypeMap));
            filterCollectionList.add(commonFilterFileCollection);
        }
        return filterCollectionList;
    }

    private List<CommonFilterFile> buildFilterList(Integer collectionId, Map<Integer, String> filterTypeMap) {
        List<CommonFilterFile> filterList = new ArrayList<>();
        FilterFileDAO filterFileDAO = new FilterFileDAOImpl(this.schemaName);
        List<FilterFile> filterFileList = filterFileDAO.findByCollectionId(collectionId);
        for (FilterFile filterFile : filterFileList) {
            CommonFilterFile commonFilterFile = new CommonFilterFile();
            commonFilterFile.setFilterCollectionId(filterFile.getId());
            commonFilterFile.setFilterData(filterFile.getFilterData());
            commonFilterFile.setFilterTypeId(filterFile.getFilterTypeId());
            commonFilterFile.setFilterType(filterTypeMap.get(filterFile.getFilterTypeId()));
            commonFilterFile.setId(filterFile.getId());
            commonFilterFile.setIsAndFlag(filterFile.getIsAndFlag());
            filterList.add(commonFilterFile);
        }
        return filterList;
    }

    private Map<Integer, String> buildProjectIdMap() {
        Map<Integer, String> projectIdMap = new TreeMap<>();
        ProjectDAO projectDAO = new ProjectDAOImpl(this.schemaName);
        List<Project> projectList = projectDAO.findAll();
        for (Project project : projectList) {
            projectIdMap.put(project.getId(), project.getProjectName());
        }
        return projectIdMap;
    }

    private Map<Integer, String> buildFilterTypeMap() {
        Map<Integer, String> filterTypeMap = new TreeMap<>();
        FilterTypeDAO filterTypeDAO = new FilterTypeDAOImpl(this.schemaName);
        List<FilterType> filterTypeList = filterTypeDAO.findAll();
        for (FilterType filterType: filterTypeList) {
            filterTypeMap.put(filterType.getId(), filterType.getFilterType());
        }
        return filterTypeMap;
    }
}
