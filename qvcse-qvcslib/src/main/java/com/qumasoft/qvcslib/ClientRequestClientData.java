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
package com.qumasoft.qvcslib;

/**
 *
 * @author Jim Voris
 */
public abstract class ClientRequestClientData implements ClientRequestOperationDataInterface {
    private static final long serialVersionUID = -1274551125601794496L;

    private String serverName;
    private String projectName;
    private String viewName;
    private String appendedPath;
    private String shortWorkfileName;
    private String revisionString;
    private String userName;
    private RoleType role;
    private Integer transactionId;
    private Integer fileId;
    private byte[] password;

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        if (!containsElement(ValidRequestElementType.SERVER_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getServerName");
        }
        return serverName;
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
    public void setServerName(String server) {
        if (!containsElement(ValidRequestElementType.SERVER_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setServerName");
        }
        serverName = server;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    @Override
    public String getProjectName() {
        if (!containsElement(ValidRequestElementType.PROJECT_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getProjectName");
        }
        return projectName;
    }

    /**
     * Set the project name.
     * @param project the project name.
     */
    public void setProjectName(String project) {
        if (!containsElement(ValidRequestElementType.PROJECT_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setProjectName");
        }
        projectName = project;
    }

    /**
     * Get the view name.
     * @return the view name.
     */
    @Override
    public String getViewName() {
        if (!containsElement(ValidRequestElementType.VIEW_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getViewName");
        }
        return viewName;
    }

    /**
     * Set the view name.
     * @param view the view name.
     */
    public void setViewName(String view) {
        if (!containsElement(ValidRequestElementType.VIEW_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setViewName");
        }
        viewName = view;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        if (!containsElement(ValidRequestElementType.APPENDED_PATH, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getAppendedPath");
        }
        return appendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setAppendedPath(String path) {
        if (!containsElement(ValidRequestElementType.APPENDED_PATH, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setAppendedPath");
        }
        appendedPath = path;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        if (!containsElement(ValidRequestElementType.SHORT_WORKFILE_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getShortWorkfileName");
        }
        return shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setShortWorkfileName(String shortName) {
        if (!containsElement(ValidRequestElementType.SHORT_WORKFILE_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getShortWorkfileName");
        }
        shortWorkfileName = shortName;
    }

    /**
     * Get the transaction id.
     * @return the transaction id.
     */
    public Integer getTransactionID() {
        if (!containsElement(ValidRequestElementType.TRANSACTION_ID, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getTransactionID");
        }
        return transactionId;
    }

    /**
     * Set the transaction id.
     * @param transactionID the transaction id.
     */
    public void setTransactionID(int transactionID) {
        if (!containsElement(ValidRequestElementType.TRANSACTION_ID, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setTransactionID");
        }
        transactionId = Integer.valueOf(transactionID);
    }

    /**
     * Get the file id.
     * @return the file id.
     */
    public Integer getFileID() {
        if (!containsElement(ValidRequestElementType.FILE_ID, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getFileID");
        }
        return fileId;
    }

    /**
     * Set the file id.
     * @param fileID the file id.
     */
    public void setFileID(Integer fileID) {
        if (!containsElement(ValidRequestElementType.FILE_ID, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setFileID");
        }
        fileId = fileID;
    }

    /**
     * Get the revision string.
     * @return the revision string.
     */
    public String getRevisionString() {
        if (!containsElement(ValidRequestElementType.REVISION_STRING, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getRevisionString");
        }
        return revisionString;
    }

    /**
     * Set the revision string.
     * @param revString the revision string.
     */
    public void setRevisionString(String revString) {
        if (!containsElement(ValidRequestElementType.REVISION_STRING, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setRevisionString");
        }
        revisionString = revString;
    }

    /**
     * Get the user name.
     * @return the user name.
     */
    public String getUserName() {
        if (!containsElement(ValidRequestElementType.USER_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getUserName");
        }
        return userName;
    }

    /**
     * Set the user name.
     * @param user the user name.
     */
    public void setUserName(String user) {
        if (!containsElement(ValidRequestElementType.USER_NAME, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setUserName");
        }
        userName = user;
    }

    /**
     * Get the password.
     * @return the password.
     */
    public byte[] getPassword() {
        if (!containsElement(ValidRequestElementType.PASSWORD, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getPassword");
        }
        return password;
    }

    /**
     * Set the password.
     * @param pw the password.
     */
    public void setPassword(byte[] pw) {
        if (!containsElement(ValidRequestElementType.PASSWORD, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setPassword");
        }
        password = pw;
    }

    /**
     * Get the role.
     * @return the role.
     */
    public RoleType getRole() {
        if (!containsElement(ValidRequestElementType.ROLE, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to getRole");
        }
        return role;
    }

    /**
     * Set the role.
     * @param r the role.
     */
    public void setRole(RoleType r) {
        if (!containsElement(ValidRequestElementType.ROLE, getValidElements())) {
            throw new QVCSRuntimeException("Unexpected call to setRole");
        }
        role = r;
    }

    private boolean containsElement(ValidRequestElementType validRequestElementType, ValidRequestElementType[] validElements) {
        boolean isValidElementFlag = false;
        if (validElements != null) {
            for (ValidRequestElementType validType : validElements) {
                if (validType.equals(validRequestElementType)) {
                    isValidElementFlag = true;
                    break;
                }
            }
        }
        return isValidElementFlag;
    }

}