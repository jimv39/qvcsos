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
package com.qumasoft.qvcslib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class that holds the header elements of a QVCS archive file.
 * @author Jim Voris
 */
public final class LogFileHeaderInfo implements java.io.Serializable {
    private static final long serialVersionUID = 4972338412531779081L;

    /*
     * Data members that get serialized
     */
    private LogFileHeader logFileHeader = null;
    private RevisionDescriptor revisionDescriptor = null;
    private LabelInfo[] labelInfoArray = null;
    private String accessList = null;
    private String modifierList = null;
    private String commentPrefix = null;
    private String owner = null;
    private String moduleDescription = null;
    private SupplementalHeaderInfo supplementalInfo = null;
    private transient boolean sizeChanged = false;

    /**
     * Default constructor.
     */
    public LogFileHeaderInfo() {
        logFileHeader = new LogFileHeader();
    }

    /**
     * For use in branches only.
     * @param header the header from the LogFile.
     */
    public LogFileHeaderInfo(LogFileHeader header) {
        logFileHeader = header;
    }

    /**
     * Read header information from a logfile.
     * @param inStream the stream to read from.
     * @return true if the read was successful; false otherwise.
     * @throws com.qumasoft.qvcslib.LogFileReadException if there are QVCS related problems reading the file.
     */
    public boolean read(RandomAccessFile inStream) throws LogFileReadException {
        boolean returnValue = true;
        try {
            long bytesAvailable = inStream.length();
            if (bytesAvailable > 0) {
                returnValue = logFileHeader.read(inStream);
                if (returnValue) {
                    // Read the variable length information.
                    returnValue = readVariableInfo(inStream);
                }
            }
        } catch (FileNotFoundException notFound) {
            returnValue = false;
        } catch (IOException ioProblem) {
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Write the header information to an archive file.
     * @param outStream the stream to write to.
     * @return true if the write was successful; false otherwise.
     */
    public boolean write(RandomAccessFile outStream) {
        boolean returnValue;
        try {
            returnValue = logFileHeader.write(outStream);
            if (returnValue) {
                // Write the variable length information.
                returnValue = writeVariableInfo(outStream);
            }
        } catch (FileNotFoundException notFound) {
            returnValue = false;
        } catch (IOException ioProblem) {
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Method to read the variable length information from a logfile header. Assume the stream is correctly positioned.
     * @param inStream the stream to read from.
     * @return true if the read was successful; false otherwise.
     * @throws java.io.IOException on read problems
     */
    boolean readVariableInfo(RandomAccessFile inStream) throws IOException {
        boolean returnValue = true;

        // Read in the default branch information
        if (logFileHeader.defaultDepth() > 0) {
            revisionDescriptor = new RevisionDescriptor(logFileHeader.defaultDepth());
            revisionDescriptor.read(inStream);
        }

        // Read in the access list
        if (logFileHeader.accessSize() > 0) {
            byte[] accessLst = new byte[logFileHeader.accessSize()];
            int bytesRead = inStream.read(accessLst);
            accessList = new String(accessLst, 0, accessLst.length - 1);
        }

        // Read in the modifier list
        if (logFileHeader.modifierSize() > 0) {
            byte[] modifierLst = new byte[logFileHeader.modifierSize()];
            int bytesRead = inStream.read(modifierLst);
            modifierList = new String(modifierLst, 0, modifierLst.length - 1);
        }

        // Read in the comment prefix
        if (logFileHeader.commentSize() > 0) {
            byte[] commentPfx = new byte[logFileHeader.commentSize()];
            int bytesRead = inStream.read(commentPfx);
            commentPrefix = new String(commentPfx, 0, commentPfx.length - 1);
        }

        // Read in the owner
        if (logFileHeader.ownerSize() > 0) {
            byte[] ownr = new byte[logFileHeader.ownerSize()];
            int bytesRead = inStream.read(ownr);
            owner = new String(ownr, 0, ownr.length - 1);
        }

        // Read in the Module description
        if (logFileHeader.descriptionSize() > 0) {
            byte[] moduleDesc = new byte[logFileHeader.descriptionSize()];
            int bytesRead = inStream.read(moduleDesc);
            moduleDescription = new String(moduleDesc, 0, moduleDesc.length - 1);
        }

        // Read in the supplemental information
        if (logFileHeader.supplementalInfoSize() > 0) {
            supplementalInfo = new SupplementalHeaderInfo(logFileHeader.supplementalInfoSize());
            supplementalInfo.read(inStream);

            // For really old QVCS/QVCS-Pro archives, we may need to adjust the size
            // of the supplemental info area.
            if (logFileHeader.supplementalInfoSize() != supplementalInfo.getSize()) {
                logFileHeader.getSupplementalInfoSize().setValue(supplementalInfo.getSize());
            }
        }

        // Read in any label information
        if (logFileHeader.versionCount() > 0) {
            labelInfoArray = new LabelInfo[logFileHeader.versionCount()];

            for (int i = 0; i < logFileHeader.versionCount(); i++) {
                labelInfoArray[i] = new LabelInfo();
                labelInfoArray[i].read(inStream);
            }
        }

        return returnValue;
    }

    /**
     * Method to write the variable length information of a logfile header.
     * @param outStream the stream to write to.
     * @return true if write of variable information succeeds; false otherwise.
     * @throws java.io.IOException if there is a write error.
     */
    boolean writeVariableInfo(RandomAccessFile outStream) throws IOException {
        boolean returnValue = true;

        // Write out the default branch information
        if (logFileHeader.defaultDepth() > 0) {
            revisionDescriptor.write(outStream);
        }

        // Write out the access list
        if (logFileHeader.accessSize() > 0) {
            outStream.write(accessList.getBytes());
            outStream.writeByte(0);
        }

        // Write out the modifier list
        if (logFileHeader.modifierSize() > 0) {
            outStream.write(modifierList.getBytes());
            outStream.writeByte(0);
        }

        // Write out the comment prefix
        if (logFileHeader.commentSize() > 0) {
            outStream.write(commentPrefix.getBytes());
            outStream.writeByte(0);
        }

        // Write out the owner
        if (logFileHeader.ownerSize() > 0) {
            outStream.write(owner.getBytes());
            outStream.writeByte(0);
        }

        // Write out the Module description
        if (logFileHeader.descriptionSize() > 0) {
            outStream.write(moduleDescription.getBytes());
            outStream.writeByte(0);
        }

        // Write out the supplemental information
        if (logFileHeader.supplementalInfoSize() > 0) {
            supplementalInfo.write(outStream);
        }

        // Write out any label information
        if (logFileHeader.versionCount() > 0) {
            for (int i = 0; i < logFileHeader.versionCount(); i++) {
                labelInfoArray[i].write(outStream);
            }
        }

        return returnValue;
    }

    /**
     * Return the access list as a String.
     * @return a String representation of the access list (comma separated user names).
     */
    public String getAccessList() {
        return accessList;
    }

    /**
     * Set the access list.
     * @param accessLst a comma separated list of QVCS user names.
     */
    public void setAccessList(String accessLst) {
        if (accessList != null) {
            if (accessList.length() != accessLst.length()) {
                sizeChanged = true;
            }
        } else {
            sizeChanged = true;
        }
        accessList = accessLst;
        logFileHeader.getAccessSize().setValue(1 + accessLst.getBytes().length);
    }

    /**
     * Return the modifier list for this archive.
     * @return the modifier list for this archive.
     */
    public String getModifierList() {
        return modifierList;
    }

    /**
     * Set the modifier list.
     * @param modifierLst a comma separated list of QVCS user names.
     */
    public void setModifierList(String modifierLst) {
        if (modifierList != null) {
            if (modifierList.length() != modifierLst.length()) {
                sizeChanged = true;
            }
        } else {
            sizeChanged = true;
        }
        modifierList = modifierLst;
        logFileHeader.getModifierSize().setValue(1 + modifierLst.getBytes().length);
    }

    /**
     * Return the module description as a String.
     * @return the module description as a String.
     */
    public String getModuleDescription() {
        return moduleDescription;
    }

    /**
     * Set the module description.
     * @param moduleDesc the module description.
     */
    public void setModuleDescription(String moduleDesc) {
        if (moduleDescription != null) {
            if (moduleDescription.length() != moduleDesc.length()) {
                sizeChanged = true;
            }
        } else {
            sizeChanged = true;
        }
        moduleDescription = moduleDesc;
        logFileHeader.getDescSize().setValue(1 + moduleDesc.getBytes().length);
    }

    /**
     * Get the owner as a String.
     * @return the owner as a String.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Set the module owner.
     * @param ownr the module owner.
     */
    public void setOwner(String ownr) {
        if (owner != null) {
            if (owner.length() != ownr.length()) {
                sizeChanged = true;
            }
        } else {
            sizeChanged = true;
        }
        owner = ownr;
        logFileHeader.getOwnerSize().setValue(1 + ownr.getBytes().length);
    }

    /**
     * Get the comment prefix as a String.
     * @return the comment prefix.
     */
    public String getCommentPrefix() {
        return commentPrefix;
    }

    /**
     * Set the comment prefix as a String.
     * @param commentPfx the comment prefix as a String.
     */
    public void setCommentPrefix(String commentPfx) {
        if (commentPrefix != null) {
            if (commentPrefix.length() != commentPfx.length()) {
                sizeChanged = true;
            }
        } else {
            sizeChanged = true;
        }
        commentPrefix = commentPfx;
        logFileHeader.getCommentSize().setValue(1 + commentPfx.getBytes().length);
    }

    /**
     * Get the array of label information.
     * @return the array of label information.
     */
    public LabelInfo[] getLabelInfo() {
        return labelInfoArray;
    }

    /**
     * Set the label information.
     * @param newLabelInfoArray the new label information.
     */
    public void setLabelInfo(LabelInfo[] newLabelInfoArray) {
        labelInfoArray = newLabelInfoArray;
        if (newLabelInfoArray != null) {
            logFileHeader.setVersionCount(newLabelInfoArray.length);
        } else {
            logFileHeader.setVersionCount(0);
        }
    }

    /**
     * Get the supplemental info for this header.
     * @return the supplemental header info for this header.
     */
    public SupplementalHeaderInfo getSupplementalHeaderInfo() {
        return supplementalInfo;
    }

    /**
     * Return true if this file has the given label; false otherwise.
     * @param label the label to search for.
     * @return true if the label has been applied to this file; false otherwise.
     */
    public boolean hasLabel(String label) {
        boolean flag = false;
        LabelInfo[] labelInfo = getLabelInfo();
        if (labelInfo != null) {
            for (LabelInfo labelInfo1 : labelInfo) {
                if (0 == labelInfo1.getLabelString().compareTo(label)) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * Is this file obsolete.
     * @deprecated we don't use the obsolete marker anymore.
     * @return true if the obsolete marker label has been applied to this file; false otherwise.
     */
    public boolean getIsObsolete() {
        boolean retVal = false;

        if (labelInfoArray != null) {
            for (LabelInfo labelInfo : labelInfoArray) {
                if (labelInfo.getIsObsolete()) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Set the obsolete state for this file.
     * @deprecated we don't use the obsolete marker anymore.
     * @param flag true to mark obsolete; false to mark not obsolete.
     * @param creatorIndex the user index of the QVCS user making this change.
     */
    @SuppressWarnings("ManualArrayToCollectionCopy")
    public void setIsObsolete(boolean flag, int creatorIndex) {
        if (flag) {
            // Mark the file obsolete
            if (!getIsObsolete()) {
                sizeChanged = true;

                // We need to add the obsolete marker label.
                LabelInfo[] labelInfo = getLabelInfo();
                int currentSize = 0;
                if (labelInfo != null) {
                    currentSize = labelInfo.length;
                }

                // Create the obsolete marker.
                LabelInfo[] newLabelInfo = new LabelInfo[currentSize + 1];
                LabelInfo obsoleteMarker = new LabelInfo();
                obsoleteMarker.setIsObsolete(true, creatorIndex);

                // Put the obsolete marker as the 1st label in the label array
                newLabelInfo[0] = obsoleteMarker;
                for (int i = 0; i < currentSize; i++) {
                    newLabelInfo[i + 1] = labelInfo[i];
                }
                setLabelInfo(newLabelInfo);
            }
        } else {
            // Mark the file not obsolete
            if (getIsObsolete()) {
                sizeChanged = true;

                LabelInfo[] currentLabels = getLabelInfo();
                LabelInfo[] newLabelInfo = null;
                if (currentLabels.length > 1) {
                    newLabelInfo = new LabelInfo[currentLabels.length - 1];

                    // Remove the obsolete marker label...
                    int j = 0;
                    int removeCount = 0;
                    for (LabelInfo currentLabel : currentLabels) {
                        if (currentLabel.getIsObsolete()) {
                            removeCount++;
                        } else {
                            newLabelInfo[j++] = currentLabel;
                        }
                    }

                    if (removeCount > 1) {
                        // Oops.  We had more than a single obsolete marker
                        // (which could happen because of an earlier bug!).
                        // We need to make the newLableInfo to be the correct
                        // size.
                        LabelInfo[] compactedNewLabelInfo = new LabelInfo[currentLabels.length - removeCount];
                        for (int i = 0; i < compactedNewLabelInfo.length; i++) {
                            compactedNewLabelInfo[i] = newLabelInfo[i];
                        }
                        newLabelInfo = compactedNewLabelInfo;
                    }
                }
                setLabelInfo(newLabelInfo);
            }
        }
    }

    /**
     * Get the header.
     * @return the header.
     */
    public LogFileHeader getLogFileHeader() {
        return logFileHeader;
    }

    /**
     * Get the default revision's revision descriptor.
     * @return the default revision's revision descriptor.
     */
    public RevisionDescriptor getDefaultRevisionDescriptor() {
        return revisionDescriptor;
    }

    /**
     * Set the default revision descriptor.
     * <p>
     * <b>THIS SHOULD ONLY BE USED BY BRANCH ARCHIVE INFO CLASSES TO BUILD THE FAKE LOGFILEHEADERINFO THAT DESCRIBES THE
     * BRANCH WAY OF LOOKING AT THE ASSOCIATED FILE</b></p>
     * @param revisionDesc the revision descriptor to use here.
     */
    public void setDefaultRevisionDescriptor(RevisionDescriptor revisionDesc) {
        this.revisionDescriptor = revisionDesc;
    }

    /**
     * Get the workfile name. Used to capture the checkout to location.
     * @return the workfile name.
     */
    public String getWorkfileName() {
        if (supplementalInfo != null) {
            return supplementalInfo.getWorkfileCheckedOutToLocation();
        } else {
            return null;
        }
    }

    /**
     * Set the workfile name.
     * @param workfileName the workfile name.
     */
    public void setWorkfileName(String workfileName) {
        if (supplementalInfo == null) {
            supplementalInfo = new SupplementalHeaderInfo();
            logFileHeader.getSupplementalInfoSize().setValue(supplementalInfo.getSize());
        }
        supplementalInfo.setWorkfileCheckedOutToLocation(workfileName);
    }

    /**
     * Get the revision count... i.e. the number of revisions in this archive file.
     * @return the revision count.
     */
    public int getRevisionCount() {
        return logFileHeader.revisionCount();
    }

    /**
     * Update the header 'in-place'. This can only be used if the size of the header has not changed. Assume the stream is positioned correctly.
     * @param ioStream the stream to write to.
     * @return true if the write succeeds.
     */
    public boolean updateInPlace(java.io.RandomAccessFile ioStream) {
        if (!sizeChanged) {
            try {
                ioStream.seek(0);
            } catch (java.io.IOException e) {
                return false;
            }
            return write(ioStream);
        } else {
            return false;
        }
    }

    /**
     * Get the revision string associated with the given label.
     * @param label the label string.
     * @return the revision associated with the label or an empty string if no revision is associated with the given label.
     */
    public String getRevisionStringForLabel(String label) {
        String revisionString = "";
        LabelInfo[] labelInfo = getLabelInfo();
        for (LabelInfo labelInfo1 : labelInfo) {
            if (0 == labelInfo1.getLabelString().compareTo(label)) {
                revisionString = labelInfo1.getLabelRevisionString();
                break;
            }
        }
        return revisionString;
    }

    /**
     * Supply a useful String representation of the logfile header.
     * @return a useful String representation of the logfile header.
     */
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();

        // show archive name
        returnString.append("\nQVCS workfile name:\t").append(supplementalInfo.getWorkfileCheckedOutToLocation());

        // show the owner ID
        returnString.append("\nOwner Name:\t").append(owner);

        // show the revision count
        returnString.append("\nRevision Count:\t").append(logFileHeader.revisionCount());

        // show default branch
        returnString.append("\nDefault branch:\t");
        if (logFileHeader.defaultDepth() == 0) {
            returnString.append("TRUNK");
        } else {
            returnString.append(revisionDescriptor);
        }

        // show the latest trunk revision
        returnString.append("\nLatest trunk revision:\t").append(logFileHeader.latestTrunkRevision());

        // show the lock count
        returnString.append("\nLock count:\t").append(logFileHeader.lockCount());

        // show the comment prefix
        returnString.append("\nComment prefix:\t'").append(getCommentPrefix()).append("'");

        // show the access list
        returnString.append("\nAccess List:\t").append(getAccessList());

        // show the modifier list
        returnString.append("\nModifier List:\t").append(getModifierList()).append("\n");

        // show the attributes
        returnString.append(logFileHeader.attributes());

        // show the labels
        if (logFileHeader.versionCount() > 0) {
            returnString.append("Labels:\n");
            for (int i = 0; i < logFileHeader.versionCount(); i++) {
                returnString.append(labelInfoArray[i]);
            }
        }
        returnString.append("Archive description:\n");
        returnString.append(moduleDescription);

        return returnString.toString();
    }
}
