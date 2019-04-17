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
package com.qumasoft.clientapi;

/**
 * Interface that defines the context for QVCS-Enterprise Client API requests.
 * The implementation of this interface will likely be a java bean style class.
 * The methods defined here define the accessor methods needed.
 *
 * @author Jim Voris
 */
public interface ClientAPIContext {

    /**
     * Get the user name. This is the user name used to login to the
     * QVCS-Enterprise server.
     *
     * @return the user name.
     */
    String getUserName();

    /**
     * Set the user name.
     *
     * @param userName the user name used to login to the QVCS-Enterprise
     * server.
     */
    void setUserName(String userName);

    /**
     * Get the password. This is the password for the user name so that the
     * username/password pair can be used to login to the QVCS-Enterprise
     * server.
     *
     * @return the password.
     */
    String getPassword();

    /**
     * Set the password. This is the password for the user name so that the
     * username/password pair can be used to login to the QVCS-Enterprise
     * server.
     *
     * @param password the password.
     */
    void setPassword(String password);

    /**
     * Get the QVCS-Enterprise server IP address. This is the IP address that
     * defines the network location of the QVCS-Enterprise server.
     *
     * @return the IP address of the QVCS-Enterprise server.
     */
    String getServerIPAddress();

    /**
     * Set the QVCS-Enterprise server IP address. The QVCS-Enterprise client API
     * will attempt to communicate with the QVCS-Enterprise server located at
     * this IP address.
     *
     * @param serverIPAddress the server IP address.
     */
    void setServerIPAddress(String serverIPAddress);

    /**
     * Get the port number where the QVCS-Enterprise server is listening for
     * connections from QVCS-Enterprise clients.
     *
     * @return the port number.
     */
    Integer getPort();

    /**
     * Set the port number that the QVCS-Enterprise client API will use when
     * attempting to establish a connection to the QVCS-Enterprise server. The
     * server should be listening for client connections on this port number.
     *
     * @param port the port number used by the QVCS-Enterprise server to listen
     * for client connections.
     */
    void setPort(Integer port);

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    String getProjectName();

    /**
     * Set the project name. This defines the project that you are interested
     * in. The client API requests will request data from the project named
     * here.
     *
     * @param projectName the name of the QVCS-Enterprise project that you are
     * interested in.
     */
    void setProjectName(String projectName);

    /**
     * Get the view name.
     *
     * @return the view name.
     */
    String getViewName();

    /**
     * Set the view name. Typically (and by default), this will be the 'Trunk'
     * view.
     *
     * @param viewName the view that you are interested in.
     */
    void setViewName(String viewName);

    /**
     * Get the appendedPath.
     *
     * @return the appended path.
     */
    String getAppendedPath();

    /**
     * Set the appended path. The 'appended path' identifies the sub-directory
     * of the view that you are interested in. For example, if you are
     * interested in the root directory of a view, then the appended path would
     * be the empty string (e.g. ""); if you are interested in the
     * source/java/com/qumasoft/utility sub-directory, then the appended path
     * would be "source/java/com/qumasoft/utility".
     *
     * @param appendedPath the directory within the view that you are interested
     * in.
     */
    void setAppendedPath(String appendedPath);

    /**
     * Get the file name.
     *
     * @return the file name.
     */
    String getFileName();

    /**
     * Set the file name. This should be the file name for an individual file
     * that you are interested in. It should include no directory components at
     * all (the directory component is specified via the setAppendedPath()
     * method).
     *
     * @param fileName the file name.
     */
    void setFileName(String fileName);

    /**
     * Get the recurse flag. This flag is used to determine whether API request
     * applies to just the directory defined by the appended path, or if it
     * applies recursively to include in addition all the directories beneath
     * the appended path directory.
     *
     * @return the recurse flag.
     */
    boolean getRecurseFlag();

    /**
     * Set the recurse flag. This flag is used to determine whether API request
     * applies to just the directory defined by the appended path, or if it
     * applies recursively to include in addition all the directories beneath
     * the appended path directory.
     *
     * @param flag the value for the recurse flag.
     */
    void setRecurseFlag(boolean flag);

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
    boolean getPreserveStateFlag();

    /**
     * Set the preserveState flag. See
     * {@link #getPreserveStateFlag() getPreserveStateFlag} for a description of
     * how this flag works. You should set this flag before beginning method
     * calls on the ClientAPI.
     *
     * @param flag the new value for the preserveState flag.
     */
    void setPreserveStateFlag(boolean flag);
}
