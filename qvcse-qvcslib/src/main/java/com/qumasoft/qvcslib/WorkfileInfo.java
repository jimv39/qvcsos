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

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Workfile info. We use this class to snag all the information that we need for a given workfile.
 *
 * @author Jim Voris
 */
public class WorkfileInfo implements WorkfileInfoInterface, Comparable, java.io.Serializable {
    private static final long serialVersionUID = 6793972153135601944L;

    private String fullWorkfileName = null;
    private String shortWorkfileName = null;
    private long workfileSize = -1;
    private Date lastChanged = null;
    private boolean keywordExpansionAttribute = false;
    private boolean binaryFileAttribute = false;
    private String projectName = null;
    /** This is the date that this workfile was fetched from the server. */
    private long fetchedDate = 0L;
    /** This is the revision that was fetched from the server. */
    private String workfileRevisionString = null;
    private transient ArchiveInfoInterface archiveInformation = null;
    private transient File workfile = null;

    /**
     * Default constructor.
     */
    public WorkfileInfo() {
    }

    /**
     * Constructor that uses a File object.
     * @param wrkfile a File object that represents the workfile.
     * @param keywordExpAttribute is keyword expansion enabled.
     * @param binFileAttribute is this a binary file.
     * @param project the project name.
     * @throws IOException if the canonical path cannot be derived.
     */
    public WorkfileInfo(File wrkfile, boolean keywordExpAttribute, boolean binFileAttribute, String project) throws IOException {
        fullWorkfileName = wrkfile.getCanonicalPath();
        shortWorkfileName = wrkfile.getName();
        lastChanged = new Date(wrkfile.lastModified());
        workfileSize = wrkfile.length();
        workfile = wrkfile;
        keywordExpansionAttribute = keywordExpAttribute;
        binaryFileAttribute = binFileAttribute;
        projectName = project;
    }

    /**
     * Constructor that uses the full workfile name.
     * @param fullWorkName the full workfile name.
     * @param keywordExpAttribute is keyword expansion enabled.
     * @param binFileAttribute is this a binary file.
     * @param project the project name.
     * @throws IOException if the canonical path cannot be derived.
     */
    public WorkfileInfo(String fullWorkName, boolean keywordExpAttribute, boolean binFileAttribute, String project) throws IOException {
        workfile = new File(fullWorkName);
        fullWorkfileName = workfile.getCanonicalPath();
        shortWorkfileName = workfile.getName();
        lastChanged = new Date(workfile.lastModified());
        workfileSize = workfile.length();
        keywordExpansionAttribute = keywordExpAttribute;
        binaryFileAttribute = binFileAttribute;
        projectName = project;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullWorkfileName() {
        return fullWorkfileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getWorkfileLastChangedDate() {
        return lastChanged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getWorkfileSize() {
        return workfileSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getWorkfile() {
        if (workfile == null) {
            workfile = new File(getFullWorkfileName());
        }
        return workfile;
    }

    /**
     * {@inheritDoc}
     * @param o the object we compare to.
     */
    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof WorkfileInfo) {
            WorkfileInfo workfileInfo = (WorkfileInfo) o;
            retVal = getFullWorkfileName().equals(workfileInfo.getFullWorkfileName());
        }
        return retVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 0;
        if (this.fullWorkfileName != null) {
            hash = this.fullWorkfileName.hashCode();
        }
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object o) {
        WorkfileInfo workfileInfo = (WorkfileInfo) o;

        return getFullWorkfileName().compareTo(workfileInfo.getFullWorkfileName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectName() {
        return projectName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getKeywordExpansionAttribute() {
        return keywordExpansionAttribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setKeywordExpansionAttribute(boolean flag) {
        keywordExpansionAttribute = flag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBinaryFileAttribute() {
        return binaryFileAttribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBinaryFileAttribute(boolean flag) {
        binaryFileAttribute = flag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getFetchedDate() {
        return fetchedDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFetchedDate(long time) {
        fetchedDate = time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorkfileRevisionString() {
        return workfileRevisionString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorkfileRevisionString(String revisionString) {
        workfileRevisionString = revisionString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveInfoInterface getArchiveInfo() {
        return archiveInformation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setArchiveInfo(ArchiveInfoInterface archiveInfo) {
        this.archiveInformation = archiveInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getWorkfileExists() {
        return getWorkfile().exists();
    }
}
