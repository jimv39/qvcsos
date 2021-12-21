/*   Copyright 2004-2014 Jim Voris
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

import java.io.Serializable;

/**
 * QVCS archive attributes.
 * @author Jim Voris
 */
public class ArchiveAttributes implements Serializable {
    private static final long serialVersionUID = 1742270041468539923L;

    // Attribute bit definitions
    private static final short QVCS_DELETEWORK_BIT = 0x02;
    private static final short QVCS_PROTECTARCHIVE_BIT = 0x08;
    private static final short QVCS_PROTECTWORKFILE_BIT = 0x10;
    private static final short QVCS_BINARYFILE_BIT = 0x20;
    private static final short QVCS_JOURNALFILE_BIT = 0x40;
    private static final short QVCS_COMPRESSION_BIT = 0x80;
    private static final short QVCS_AUTOMERGE_BIT = 0x100;
    private static final short QVCS_COMPUTEDELTA_BIT = 0x200;
    private static final short QVCS_LATESTREVONLY_BIT = 0x400;
    // Attribute strings
    private static final String DELETE_WORK_STRING = "DELETEWORK";
    private static final String PROTECT_ARCHIVE_STRING = "PROTECTARCHIVE";
    private static final String PROTECT_WORKFILE_STRING = "PROTECTWORKFILE";
    private static final String BINARY_FILE_STRING = "BINARYFILE";
    private static final String JOURNAL_FILE_STRING = "JOURNALFILE";
    private static final String COMPRESSION_STRING = "COMPRESSION";
    private static final String AUTO_MERGE_STRING = "AUTOMERGE";
    private static final String COMPUTE_DELTA_STRING = "COMPUTEDELTA";
    private static final String LATEST_REV_ONLY_STRING = "LATESTREVONLY";
    private int attributes;

    /**
     * Create from the bits set in the parameter.
     * @param attribs an integer with bits set to indicate which attributes are enabled/disabled.
     */
    public ArchiveAttributes(int attribs) {
        attributes = attribs;
    }

    /**
     * Construct a default set of attributes. By default, these attributes are enabled:
     * <ul>
     * <li>Journal enabled</li>
     * <li>Compute Delta enabled</li>
     * </ul>
     */
    public ArchiveAttributes() {
        attributes = QVCS_COMPUTEDELTA_BIT | QVCS_JOURNALFILE_BIT;
    }

    /**
     * A copy constructor.
     * @param attribs the attributes to copy.
     */
    public ArchiveAttributes(ArchiveAttributes attribs) {
        attributes = attribs.attributes;
    }

    /**
     * Get the integer bit mask representation of QVCS attributes.
     * @return the integer bit mask representation of QVCS attributes.
     */
    public int getAttributesAsInt() {
        return attributes;
    }

    /**
     * Do we delete the workfile?
     * @return true if the delete workfile attribute is set; false otherwise.
     */
    public boolean getIsDeleteWork() {
        return ((attributes & QVCS_DELETEWORK_BIT) != 0);
    }

    /**
     * Set delete workfile on or off.
     * @param flag true to enable deletion of workfile; false otherwise.
     */
    public void setIsDeleteWork(boolean flag) {
        setAttributeBit(flag, QVCS_DELETEWORK_BIT);
    }

    /**
     * Do we protect the archive?
     * @return true if the protect archive file is enabled; false otherwise.
     * @deprecated we don't need to support this anymore, since the archive files are located on the server only.
     */
    public boolean getIsProtectArchive() {
        return ((attributes & QVCS_PROTECTARCHIVE_BIT) != 0);
    }

    /**
     * Set protection of archive on or off.
     * @param flag true to enable protection of the archive file; false otherwise.
     * @deprecated we don't need to support this anymore, since the archive files are located on the server only.
     */
    public void setIsProtectArchive(boolean flag) {
        setAttributeBit(flag, QVCS_PROTECTARCHIVE_BIT);
    }

    /**
     * Do we protect the workfile?
     * @return true if the workfile will be write protected after gets; false otherwise.
     */
    public boolean getIsProtectWorkfile() {
        return ((attributes & QVCS_PROTECTWORKFILE_BIT) != 0);
    }

    /**
     * Set protection of workfile on or off.
     * @param flag true to enable protection of workfile for gets; false otherwise.
     */
    public void setIsProtectWorkfile(boolean flag) {
        setAttributeBit(flag, QVCS_PROTECTWORKFILE_BIT);
    }

    /**
     * Is this a binary file?
     * @return true if this is a binary file; false otherwise.
     */
    public boolean getIsBinaryfile() {
        return ((attributes & QVCS_BINARYFILE_BIT) != 0);
    }

    /**
     * Set binary file state on or off.
     * @param flag true to indicate this is a binary file; false otherwise.
     */
    public void setIsBinaryfile(boolean flag) {
        setAttributeBit(flag, QVCS_BINARYFILE_BIT);
    }

    /**
     * Do we journal changes to the archive?
     * @return true if journaling is enabled. This just means that we write some info to a human readable 'journal' file.
     * @deprecated I think the server always writes a journal entry, no matter how this is set.
     */
    public boolean getIsJournalfile() {
        return ((attributes & QVCS_JOURNALFILE_BIT) != 0);
    }

    /**
     * Set journal file on or off.
     * @param flag true to enable journaling; false to turn it off.
     * @deprecated I think the server ignores this one.
     */
    public void setIsJournalfile(boolean flag) {
        setAttributeBit(flag, QVCS_JOURNALFILE_BIT);
    }

    /**
     * Do we compress additions to the archive?
     * @return true if we should try to compress revisions as they're added to an archive; false otherwise.
     */
    public boolean getIsCompression() {
        return ((attributes & QVCS_COMPRESSION_BIT) != 0);
    }

    /**
     * Set compression on or off.
     * @param flag true to enable compression attempts; false otherwise.
     */
    public void setIsCompression(boolean flag) {
        setAttributeBit(flag, QVCS_COMPRESSION_BIT);
    }

    /**
     * Do we automatically merge when checking in? (NOT IMPLEMENTED).
     * @return true if the auto-merge bit is set.
     */
    public boolean getIsAutoMerge() {
        return ((attributes & QVCS_AUTOMERGE_BIT) != 0);
    }

    /**
     * Set auto-merge on or off.
     * @param flag true to enable auto-merge.
     */
    public void setIsAutoMerge(boolean flag) {
        setAttributeBit(flag, QVCS_AUTOMERGE_BIT);
    }

    /**
     * Do we compute a delta when checking in? (NOT IMPLEMENTED).
     * @return true if computing deltas are enabled; false otherwise.
     */
    public boolean getIsComputeDelta() {
        return ((attributes & QVCS_COMPUTEDELTA_BIT) == 0);
    }

    /**
     * Set compute delta on or off.
     * @param flag true to enable compute delta.
     */
    public void setIsComputeDelta(boolean flag) {
        setAttributeBit(!flag, QVCS_COMPUTEDELTA_BIT);
    }

    /**
     * Do we save only the latest revision? (NOT IMPLEMENTED).
     * @return true if we save only the latest revision (useful for huge binary files).
     */
    public boolean getIsLatestRevOnly() {
        return ((attributes & QVCS_LATESTREVONLY_BIT) != 0);
    }

    /**
     * Set latest rev only on or off.
     * @param flag true to save only the most recent revision.
     */
    public void setIsLatestRevOnly(boolean flag) {
        setAttributeBit(flag, QVCS_LATESTREVONLY_BIT);
    }

    private void setAttributeBit(boolean flag, short bit) {
        if (flag) {
            attributes |= bit;
        } else {
            attributes &= ~bit;
        }
    }

    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();

        returnString.append("Attributes:\n");

        // Report delete work attribute
        returnString.append(addNoPrefix(getIsDeleteWork())).append(DELETE_WORK_STRING + "\n");

        // Report protect archive attribute
        returnString.append(addNoPrefix(getIsProtectArchive())).append(PROTECT_ARCHIVE_STRING + "\n");

        // Report protect workfile attribute
        returnString.append(addNoPrefix(getIsProtectWorkfile())).append(PROTECT_WORKFILE_STRING + "\n");

        // Report binary file attribute
        returnString.append(addNoPrefix(getIsBinaryfile())).append(BINARY_FILE_STRING + "\n");

        // Report journal file attribute
        returnString.append(addNoPrefix(getIsJournalfile())).append(JOURNAL_FILE_STRING + "\n");

        // Report compression attribute
        returnString.append(addNoPrefix(getIsCompression())).append(COMPRESSION_STRING + "\n");

        // Report auto-merge attribute
        returnString.append(addNoPrefix(getIsAutoMerge())).append(AUTO_MERGE_STRING + "\n");

        // Report compute delta attribute
        returnString.append(addNoPrefix(getIsComputeDelta())).append(COMPUTE_DELTA_STRING + "\n");

        // Report latest rev only attribute
        returnString.append(addNoPrefix(getIsLatestRevOnly())).append(LATEST_REV_ONLY_STRING + "\n");

        return returnString.toString();
    }

    private String addNoPrefix(boolean flag) {
        String returnString;
        if (flag) {
            returnString = "   ";
        } else {
            returnString = QVCSConstants.QVCS_NO + " ";
        }
        return returnString;
    }

    /**
     * Create a property string representation of the attributes.
     * @return a property string representation of the attributes.
     */
    public String toPropertyString() {
        StringBuffer attributeString = new StringBuffer();

        attributeString = addAttribute(QVCS_DELETEWORK_BIT, attributeString);
        attributeString.append(",");

        attributeString = addAttribute(QVCS_PROTECTARCHIVE_BIT, attributeString);
        attributeString.append(",");

        attributeString = addAttribute(QVCS_PROTECTWORKFILE_BIT, attributeString);
        attributeString.append(",");

        attributeString = addAttribute(QVCS_JOURNALFILE_BIT, attributeString);
        attributeString.append(",");

        attributeString = addAttribute(QVCS_COMPRESSION_BIT, attributeString);
        attributeString.append(",");

        attributeString = addAttribute(QVCS_BINARYFILE_BIT, attributeString);
        attributeString.append(",");

        attributeString = addAttribute(QVCS_AUTOMERGE_BIT, attributeString);
        attributeString.append(",");

        attributeString = addAttribute(QVCS_COMPUTEDELTA_BIT, attributeString);
        attributeString.append(",");

        attributeString = addAttribute(QVCS_LATESTREVONLY_BIT, attributeString);

        return attributeString.toString();
    }

    /**
     * Consume a property string and set the attribute bits accordingly.
     * @param propertyString the property string representation of the archive attributes.
     */
    public void fromPropertyString(String propertyString) {
        attributes = 0;

        StringBuffer attributeString = new StringBuffer(propertyString);

        consumeAttributeString(QVCS_DELETEWORK_BIT, attributeString);
        attributeString.delete(0, 1 + attributeString.toString().indexOf(',', 0));

        consumeAttributeString(QVCS_PROTECTARCHIVE_BIT, attributeString);
        attributeString.delete(0, 1 + attributeString.toString().indexOf(',', 0));

        consumeAttributeString(QVCS_PROTECTWORKFILE_BIT, attributeString);
        attributeString.delete(0, 1 + attributeString.toString().indexOf(',', 0));

        consumeAttributeString(QVCS_JOURNALFILE_BIT, attributeString);
        attributeString.delete(0, 1 + attributeString.toString().indexOf(',', 0));

        consumeAttributeString(QVCS_COMPRESSION_BIT, attributeString);
        attributeString.delete(0, 1 + attributeString.toString().indexOf(',', 0));

        consumeAttributeString(QVCS_BINARYFILE_BIT, attributeString);
        attributeString.delete(0, 1 + attributeString.toString().indexOf(',', 0));

        consumeAttributeString(QVCS_AUTOMERGE_BIT, attributeString);
        attributeString.delete(0, 1 + attributeString.toString().indexOf(',', 0));

        consumeAttributeString(QVCS_COMPUTEDELTA_BIT, attributeString);
        attributeString.delete(0, 1 + attributeString.toString().indexOf(',', 0));

        consumeAttributeString(QVCS_LATESTREVONLY_BIT, attributeString);
    }

    private StringBuffer addAttribute(short attrBit, StringBuffer attributeString) {
        if ((attributes & attrBit) != 0) {
            attributeString.append(QVCSConstants.QVCS_YES);
        } else {
            attributeString.append(QVCSConstants.QVCS_NO);
        }
        return attributeString;
    }

    private void consumeAttributeString(short attributeBit, StringBuffer attributeString) {
        String yesNo;
        if (attributeString.toString().indexOf(',') > 0) {
            yesNo = attributeString.substring(0, attributeString.toString().indexOf(',', 0));
        } else {
            yesNo = attributeString.toString();
        }
        if (yesNo.compareToIgnoreCase(QVCSConstants.QVCS_YES) == 0) {
            attributes |= attributeBit;
        }
    }
}
