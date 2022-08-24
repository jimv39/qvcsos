/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.clientapi;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyInterface;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of the ClientAPIContext interface. Use the
 * {@link ClientAPIFactory#createClientAPIContext() createClientAPIContext}
 * method to create an instance of this class.
 * @author Jim Voris
 */
class ClientAPIContextImpl implements ClientAPIContext {

    private final AtomicReference<String> appendedPath = new AtomicReference<>();
    private final AtomicReference<String> fileName = new AtomicReference<>();
    private final AtomicReference<String> password = new AtomicReference<>();
    private final AtomicReference<String> projectName = new AtomicReference<>();
    private final AtomicReference<String> serverIPAddress = new AtomicReference<>();
    private final AtomicReference<String> branchName = new AtomicReference<>();
    private final AtomicReference<String> userName = new AtomicReference<>();
    private final AtomicReference<Boolean> recurseFlag = new AtomicReference<>(false);
    private final AtomicReference<Boolean> preserveStateFlag = new AtomicReference<>(false);
    private final AtomicReference<Boolean> loggedInFlag = new AtomicReference<>(false);
    private final AtomicReference<Integer> port = new AtomicReference<>(0);
    /**
     * The list of project names we get from the server.
     */
    private String[] serverProjectNames = null;
    /**
     * The list of branch names for the current project.
     */
    private String[] projectBranchNames = null;
    /**
     * The project properties for the respective projects.
     */
    private Properties[] projectProperties = null;
    /**
     * The map of appended paths.
     */
    private Set<String> appendedPathSet = null;
    /*
     * The map of archiveDirManagerProxy objects.
     */
    private Map<String, ArchiveDirManagerProxy> archiveDirManagerProxyMap = null;
    /**
     * The transport proxy. Used to communicate with the server.
     */
    private TransportProxyInterface transportProxy = null;
    /**
     * An object we use for synchronization.
     */
    private final Object syncObject = new Object();
    /**
     * Server properties.
     */
    private ServerProperties serverProperties = null;
    /**
     * The most recent activity.
     */
    private Date mostRecentActivity = null;

    /**
     * Default constructor. Set the branch name to 'Trunk' by default.
     */
    ClientAPIContextImpl() {
        setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
    }

    /**
     * Get the user name.
     *
     * @return the user name.
     */
    @Override
    public String getUserName() {
        return this.userName.get();
    }

    /**
     * Set the user name.
     *
     * @param user the user name used to login to the QVCS-Enterprise
     * server.
     */
    @Override
    public void setUserName(String user) {
        this.userName.set(user);
    }

    /**
     * Get the password. This is the password for the user name so that the
     * username/password pair can be used to login to the QVCS-Enterprise
     * server.
     *
     * @return the password.
     */
    @Override
    public String getPassword() {
        return this.password.get();
    }

    /**
     * Set the password. This is the password for the user name so that the
     * username/password pair can be used to login to the QVCS-Enterprise
     * server.
     *
     * @param pw the password.
     */
    @Override
    public void setPassword(String pw) {
        this.password.set(pw);
    }

    /**
     * Get the QVCS-Enterprise server IP address. This is the IP address that
     * defines the network location of the QVCS-Enterprise server.
     *
     * @return the IP address of the QVCS-Enterprise server.
     */
    @Override
    public String getServerIPAddress() {
        return this.serverIPAddress.get();
    }

    /**
     * Set the QVCS-Enterprise server IP address. The QVCS-Enterprise client API
     * will attempt to communicate with the QVCS-Enterprise server located at
     * this IP address.
     *
     * @param serverIPAddr the server IP address.
     */
    @Override
    public void setServerIPAddress(String serverIPAddr) {
        this.serverIPAddress.set(serverIPAddr);
    }

    /**
     * Get the port number where the QVCS-Enterprise server is listening for
     * connections from QVCS-Enterprise clients.
     *
     * @return the port number.
     */
    @Override
    public Integer getPort() {
        return this.port.get();
    }

    /**
     * Set the port number that the QVCS-Enterprise client API will use when
     * attempting to establish a connection to the QVCS-Enterprise server. The
     * server should be listening for client connections on this port number.
     *
     * @param prt the port number used by the QVCS-Enterprise server to listen
     * for client connections.
     */
    @Override
    public void setPort(Integer prt) {
        this.port.set(prt);
    }

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    @Override
    public String getProjectName() {
        return this.projectName.get();
    }

    /**
     * Set the project name. This defines the project that you are interested
     * in. The client API requests will request data from the project named
     * here.
     *
     * @param project the name of the QVCS-Enterprise project that you are
     * interested in.
     */
    @Override
    public void setProjectName(String project) {
        this.projectName.set(project);
    }

    /**
     * Get the branch name.
     *
     * @return the branch name.
     */
    @Override
    public String getBranchName() {
        return this.branchName.get();
    }

    /**
     * Set the branch name. Typically (and by default), this will be the 'Trunk'
     * branch.
     *
     * @param branch the branch that you are interested in.
     */
    @Override
    public final void setBranchName(String branch) {
        this.branchName.set(branch);
    }

    /**
     * Get the appendedPath.
     *
     * @return the appended path.
     */
    @Override
    public String getAppendedPath() {
        return this.appendedPath.get();
    }

    /**
     * Set the appended path. The 'appended path' identifies the sub-directory
     * of the branch that you are interested in. For example, if you are
     * interested in the root directory of a branch, then the appended path would
     * be the empty string (e.g. ""); if you are interested in the
     * source/java/com/qumasoft/utility sub-directory, then the appended path
     * would be "source/java/com/qumasoft/utility".
     *
     * @param path the directory within the branch that you are interested
     * in.
     */
    @Override
    public void setAppendedPath(String path) {
        this.appendedPath.set(path);
    }

    /**
     * Get the file name.
     *
     * @return the file name.
     */
    @Override
    public String getFileName() {
        return this.fileName.get();
    }

    /**
     * Set the file name. This should be the file name for an individual file
     * that you are interested in. It should include no directory components at
     * all (the directory component is specified via the setAppendedPath()
     * method).
     *
     * @param fileNm the file name.
     */
    @Override
    public void setFileName(String fileNm) {
        this.fileName.set(fileNm);
    }

    /**
     * Get the recurse flag. This flag is used to determine whether API request
     * applies to just the directory defined by the appended path, or if it
     * applies recursively to include in addition all the directories beneath
     * the appended path directory.
     *
     * @return the recurse flag.
     */
    @Override
    public boolean getRecurseFlag() {
        return this.recurseFlag.get();
    }

    /**
     * Set the recurse flag. This flag is used to determine whether API request
     * applies to just the directory defined by the appended path, or if it
     * applies recursively to include in addition all the directories beneath
     * the appended path directory.
     *
     * @param flag the value for the recurse flag.
     */
    @Override
    public void setRecurseFlag(boolean flag) {
        this.recurseFlag.set(flag);
    }

    /**
     * <p>
     * Get the preserveState flag. This flag indicates whether calls through the
     * client API are stateless (the default), or stateful. If the calls are
     * stateless, then no state is preserved from one call on the ClientAPI to
     * the next: the client logs in to the server for each call, and logs out
     * from the server when the call is completed. If this flag is set to
     * preserve state (true), then the client api will preserve state across
     * calls when it can. Performance will be better if this flag is set to
     * true. By default, its value is <b>false</b>.</p>
     * <p>
     * Some calls will force the client to disconnect from the server and state
     * will be discarded. For example, if the preserve state is set to true, and
     * the client performs a
     * {@link com.qumasoft.clientapi.ClientAPI#getProjectList(com.qumasoft.clientapi.ClientAPIContext) getProjectList}
     * call followed by a
     * {@link #setServerIPAddress(java.lang.String) setServerIPAddress} call on
     * this class, then state will be lost after the <b>setServerIPAddress</b>
     * call since the server end point has now changed.</p>
     *
     * @return the current value of the preserveState flag.
     */
    @Override
    public boolean getPreserveStateFlag() {
        return this.preserveStateFlag.get();
    }

    /**
     * Set the preserveState flag. See
     * {@link #getPreserveStateFlag() getPreserveStateFlag} for a description of
     * how this flag works. You should set this flag before beginning method
     * calls on the ClientAPI.
     *
     * @param flag the new value for the preserveState flag.
     */
    @Override
    public void setPreserveStateFlag(boolean flag) {
        this.preserveStateFlag.set(flag);
    }

    boolean getLoggedInFlag() {
        return this.loggedInFlag.get();
    }

    void setLoggedInFlag(boolean flag) {
        this.loggedInFlag.set(flag);
    }

    TransportProxyInterface getTransportProxy() {
        synchronized (getSyncObject()) {
            return this.transportProxy;
        }
    }

    void setTransportProxy(TransportProxyInterface transportPxy) {
        synchronized (getSyncObject()) {
            this.transportProxy = transportPxy;
        }
    }

    Properties[] getProjectProperties() {
        synchronized (getSyncObject()) {
            return this.projectProperties;
        }
    }

    void setProjectProperties(Properties[] properties) {
        synchronized (getSyncObject()) {
            if (properties != null) {
                Properties[] localPropertiesArray = new Properties[properties.length];
                System.arraycopy(properties, 0, localPropertiesArray, 0, properties.length);
                this.projectProperties = localPropertiesArray;
            } else {
                this.projectProperties = null;
            }
        }
    }

    String[] getServerProjectNames() {
        synchronized (getSyncObject()) {
            return this.serverProjectNames;
        }
    }

    void setServerProjectNames(String[] projectNames) {
        synchronized (getSyncObject()) {
            if (projectNames != null) {
                String[] localProjectNames = new String[projectNames.length];
                System.arraycopy(projectNames, 0, localProjectNames, 0, projectNames.length);
                this.serverProjectNames = localProjectNames;
            } else {
                this.serverProjectNames = null;
            }
        }
    }

    Set<String> getAppendedPathSet() {
        synchronized (getSyncObject()) {
            return this.appendedPathSet;
        }
    }

    void setAppendedPathSet(Set<String> set) {
        synchronized (getSyncObject()) {
            this.appendedPathSet = set;
        }
    }

    Map<String, ArchiveDirManagerProxy> getArchiveDirManagerProxyMap() {
        synchronized (getSyncObject()) {
            return this.archiveDirManagerProxyMap;
        }
    }

    void setArchiveDirManagerProxyMap(Map<String, ArchiveDirManagerProxy> map) {
        synchronized (getSyncObject()) {
            this.archiveDirManagerProxyMap = map;
        }
    }

    ServerProperties getServerProperties() {
        synchronized (getSyncObject()) {
            return this.serverProperties;
        }
    }

    void setServerProperties(ServerProperties props) {
        synchronized (getSyncObject()) {
            this.serverProperties = props;
        }
    }

    Object getSyncObject() {
        return this.syncObject;
    }

    String[] getProjectBranchNames() {
        synchronized (getSyncObject()) {
            return this.projectBranchNames;
        }
    }

    void setProjectBranchNames(String[] projectVwNames) {
        synchronized (getSyncObject()) {
            if (projectVwNames != null) {
                String[] localProjectBranchNames = new String[projectVwNames.length];
                System.arraycopy(projectVwNames, 0, localProjectBranchNames, 0, projectVwNames.length);
                this.projectBranchNames = localProjectBranchNames;
            } else {
                this.projectBranchNames = null;
            }
        }
    }

    /**
     * @return the mostRecentActivity
     */
    public Date getMostRecentActivity() {
        synchronized (getSyncObject()) {
            return mostRecentActivity;
        }
    }

    /**
     * @param mostRecentActvity the mostRecentActivity to set
     */
    public void setMostRecentActivity(Date mostRecentActvity) {
        synchronized (getSyncObject()) {
            this.mostRecentActivity = mostRecentActvity;
        }
    }
}
