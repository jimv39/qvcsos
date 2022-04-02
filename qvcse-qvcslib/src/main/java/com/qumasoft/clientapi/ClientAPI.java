/*   Copyright 2004-2022 Jim Voris
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

import java.util.Date;
import java.util.List;

/**
 * Define the QVCS-Enterprise client API interface.
 * <p>
 * A vanilla Java application can use this API to retrieve data from a
 * QVCS-Enterprise server. Currently, the API only supports retrieval of version
 * control information from the server; it does <i>not</i> support performing
 * any version control operations, nor does it support fetching actual files or
 * file revisions. All calls on this API are fully synchronous.</p>
 * <p>
 * If you need to perform version control operations, you should try using the
 * custom ant task included with QVCS-Enterprise.</p>
 * <p>
 * In typical use, a client would: <ol>
 * <li>Use the
 * <b>ClientAPIFactory.createClientAPIContext</b> static method to create a
 * ClientAPIContext object, and fill in the username/password, serverIP address,
 * and port number values in that object.</li>
 * <li>Use the
 * {@link com.qumasoft.clientapi.ClientAPIFactory#createClientAPI() ClientAPIFactory.createClientAPI()}
 * static method to create an instance of a class that implements this
 * <b>ClientAPI</b> interface.</li>
 * <li>Call the
 * {@link #getProjectList(ClientAPIContext) getProjectList(ClientAPIContext)}
 * method on the ClientAPI object returned by the factory to get a list of
 * projects available from the QVCS-Enterprise server, passing in the
 * ClientAPIContext object that you have filled in with the username/password,
 * etc. data.</li> <li>Choose an interesting project, and set that project name
 * on the ClientAPIContext object.</li> <li>Optionally call the
 * {@link #getBranchList(ClientAPIContext) getBranchList(ClientAPIContext)} method
 * on this interface to get a list of available branches for the given
 * project.</li> <li>Optionally set the branch name on the ClientAPIContext
 * object. (if you choose not to set the branch, then the 'Trunk' branch will be
 * used by default).</li>
 * <li>Call the
 * {@link #getProjectDirectoryList(ClientAPIContext) getProjectDirectoryList(ClientAPIContext)}
 * method on this interface to get a list of all the directories for the given
 * project/branch.</li> <li>Choose an interesting directory, and use that
 * directory string to set the appended path on the ClientAPIContext
 * object.</li> <li>Call the
 * {@link #getFileInfoList(ClientAPIContext) getFileInfoList(ClientAPIContext)}
 * method on this interface to get a list of
 * {@link com.qumasoft.clientapi.FileInfo FileInfo} objects; each object
 * provides summary information for one file.</li> <li>Choose an interesting
 * file, and set the filename on the ClientAPIContext object.</li> <li>Call the
 * {@link #getRevisionInfoList(ClientAPIContext) getRevisionInfoList(ClientAPIContext)}
 * to get a list of {@link com.qumasoft.clientapi.RevisionInfo RevisionInfo}
 * objects; each object provides summary information about a revision associated
 * with the selected file.</li> </ol> </p>
 * <p>
 * The recurse flag in the ClientAPIContext can be used when you want to see all
 * the files in a given directory tree. When the flag is true, then the List of
 * FileInfo objects returned from the <b>getFileInfoList</b> method will include
 * <i>all</i> the files in the appendedPath directory <i>and</i> all the files
 * in <i>all</i> sub-directories beneath the appended path directory. </p>
 *
 * @author Jim Voris
 */
public interface ClientAPI {

    /**
     * Login to the server using the username/password, etc., supplied in the
     * ClientAPIContext.
     *
     * @throws ClientAPIException if there is a problem.
     */
    void login() throws ClientAPIException;

    /**
     * Get the list of projects from the QVCS-Enterprise server. The list of
     * projects returned will depend on the username/password supplied in the
     * clientAPIContext object. Only those projects visible to the given user
     * will be returned. A project is 'visible' to a user if that user has the
     * 'Get File' privilege. Typically, any QVCS-Enterprise user with a READER
     * role, or a DEVELOPER role will have the 'Get File' privilege.
     *
     * @return a List<String> of project names visible to the username defined
     * in the clientAPIContext.
     * @throws ClientAPIException if there are any problems.
     */
    List<String> getProjectList() throws ClientAPIException;

    /**
     * Get the list of branches for a given project. To use this method, you must
     * set the name of the project in the clientAPIContext as well as the other
     * parameters needed for the getProjectList method.
     *
     * @return a List<String> of branch names. At the very least, this List will
     * include the 'Trunk' branch.
     * @throws ClientAPIException if there are any problems, or if the requested
     * project is not found.
     */
    List<String> getBranchList() throws ClientAPIException;

    /**
     * Get the list of directories for the given project/branch. To use this
     * method, you must set the branch name in the clientAPIContext, as well as
     * all the other parameters needed for the getBranchList method.
     *
     * @return a List<String> of appended path strings for all the directories
     * of a project/branch. Each string represents one directory. The empty string
     * "" is used to represent the root directory of the project/branch. The
     * Strings for sub-directories are relative to the project root directory
     * and in QVCS-Enterprise terminology are called the directory's
     * 'appendedPath'.
     * @throws ClientAPIException if there are any problems.
     */
    List<String> getProjectDirectoryList() throws ClientAPIException;

    /**
     * Get the list of {@link FileInfo} objects for the directory specified via
     * the {@link ClientAPIContext#setAppendedPath(java.lang.String)} attribute
     * on the clientAPIContext object. If you set the recurse flag to true (via
     * the {@link ClientAPIContext#setRecurseFlag(boolean)} attribute), then the
     * returned List will contain elements for the directory identified by the
     * appended path, as well as all the directories beneath that directory.
     *
     * flag.
     *
     * @return a List of {@link FileInfo} objects; one per file.
     * @throws ClientAPIException if there are any problems.
     */
    List<FileInfo> getFileInfoList() throws ClientAPIException;

    /**
     * Get the list of {@link RevisionInfo} objects for the file specified by
     * the clientAPIContext.
     *
     * @return the List of {@link RevisionInfo} objects for the file specified
     * by the clientAPIContext. The List could be empty if the file is not under
     * version control. Be careful to identify the location of the file
     * correctly -- i.e. the project name, branch name, appended path, and file
     * name must be correct in order for the server to find the file and report
     * its revision information.
     * @throws ClientAPIException if there are any problems.
     */
    List<RevisionInfo> getRevisionInfoList() throws ClientAPIException;

    /**
     * Get the most recent activity for the project/branch/directory specified by
     * the clientAPIContext. You must be logged in. This method is useful for
     * systems that want to poll the QVCS-Enterprise server to see when the
     * latest activity occurred within the directory tree specified within the
     * clientAPIContext.
     *
     * @return a Date that identifies when the latest change was made for the
     * directory tree identified by the project name, branch name, and appended
     * path in the clientAPIContext.
     * @throws ClientAPIException if there is any problem.
     */
    Date getMostRecentActivity() throws ClientAPIException;
}
