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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Label info.
 * @author Jim Voris
 */
public class LabelInfo implements Serializable {
    private static final long serialVersionUID = 3108618625061087277L;

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
    private CommonShort revisionCount = new CommonShort();
    private CommonShort creatorIndex = new CommonShort();
    private CommonShort labelStringSize = new CommonShort();
    private LabelRevisionInfo labelRevInfo;
    private String labelString;

    /**
     * Default constructor.
     */
    public LabelInfo() {
    }

    /**
     * Build label info using label string, revision string, etc.
     * @param label the label string.
     * @param revisionString the revision string.
     * @param floatingFlag floating label flag; true if this is to be a floating label; false otherwise.
     * @param creatorIdx the creator index.
     */
    public LabelInfo(String label, String revisionString, boolean floatingFlag, int creatorIdx) {
        // We write it as a null terminated string
        labelStringSize.setValue(label.getBytes().length + 1);
        labelString = label;

        creatorIndex.setValue(creatorIdx);

        // Create the revision major.minor pair array based on the string we
        // got.
        labelRevInfo = new LabelInfo.LabelRevisionInfo(revisionString, floatingFlag);
        revisionCount.setValue(labelRevInfo.getRevisionCount());
    }

    /**
     * Read from a file. Assume the stream is correctly positioned.
     * @param inStream the stream to read from.
     * @throws IOException for read problems.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        revisionCount.read(inStream);
        creatorIndex.read(inStream);
        labelStringSize.read(inStream);

        // Make and read as many major/minor objects as we need.
        labelRevInfo = new LabelRevisionInfo(revisionCount.getValue());
        labelRevInfo.read(inStream);

        // Read the label string.
        if (labelStringSize.getValue() > 0) {
            byte[] lblString = new byte[labelStringSize.getValue()];
            int bytesRead = inStream.read(lblString);
            labelString = new String(lblString, 0, labelStringSize.getValue() - 1);
            LOGGER.log(Level.FINEST, "Read label: " + labelString);
        }
    }

    /**
     * Write to a file.
     * @param outStream the stream to write to.
     * @throws IOException for write problems.
     */
    public void write(RandomAccessFile outStream) throws IOException {
        if (revisionCount.getValue() > 0) {
            revisionCount.write(outStream);
            creatorIndex.write(outStream);
            labelStringSize.write(outStream);

            // Write as many major/minor objects as we need.
            labelRevInfo.write(outStream);

            // Write the label string.
            if (labelStringSize.getValue() > 0) {
                outStream.write(labelString.getBytes());
                outStream.writeByte(0);
            }
        }
    }

    /**
     * Is this a floating label.
     * @return true if this describes a floating label; false otherwise.
     */
    public boolean isFloatingLabel() {
        return labelRevInfo.isFloatingLabel();
    }

    /**
     * Get the depth of the revision that this label is associated with.
     * @return the depth of the revision that this label is associated with.
     */
    public int getDepth() {
        return revisionCount.getValue() - 1;
    }

    /**
     * Is this the special 'obsolete' label.
     * @return true if this is the 'obsolete' label.
     */
    public boolean getIsObsolete() {
        return labelRevInfo.isObsolete();
    }

    /**
     * Set the obsolete-ness of this label info.
     * @param flag true to indicate this is an obsolete label; false to clear the obsolete-ness of this label.
     * @param creatorIdx the creator index of the user performing this operation.
     * @deprecated we don't use this anymore -- at least we shouldn't be using it any more.
     */
    public void setIsObsolete(boolean flag, int creatorIdx) {
        if (flag) {
            revisionCount.setValue(1);
            creatorIndex.setValue(creatorIdx);
            labelStringSize.setValue(1);
            labelString = "";
            labelRevInfo = new LabelRevisionInfo(1);
            labelRevInfo.majorMinorArray[0] = new LabelMajorMinor();
            labelRevInfo.majorMinorArray[0].majorNumber.setValue(-1);
            labelRevInfo.majorMinorArray[0].minorNumber.setValue(-1);
        } else {
            // The caller should throw instance away.
            revisionCount.setValue(0);
        }
    }

    /**
     * Get the revision string for the revision associated with this label.
     * @return the revision string for the revision associated with this label.
     */
    public String getLabelRevisionString() {
        return labelRevInfo.toString();
    }

    /**
     * Get the label string.
     * @return the label string.
     */
    public String getLabelString() {
        return labelString;
    }

    /**
     * Convenience toString that reports the label string.
     * @return convenience toString that reports the label string.
     */
    @Override
    public String toString() {
        return labelString;
    }

    /**
     * Get the creator index of the user who applied this label.
     * @return the creator index of the user who applied this label.
     */
    public int getCreatorIndex() {
        return creatorIndex.getValue();
    }

    /**
     * Get a sortable revision string for the revision associated with this label.
     * @return a sortable revision string for the revision associated with this label.
     */
    public String getSortableRevisionString() {
        return labelRevInfo.getSortableRevisionString();
    }

    /**
     * Get the major/minor pairs that describe the revision associated with this label.
     * @return the major/minor pairs that describe the revision associated with this label.
     */
    public LabelMajorMinor[] getMajorMinorPairs() {
        return labelRevInfo.getMajorMinorPairs();
    }

    /**
     * Create an inner class to handle the entire label revision information.
     */
    public class LabelRevisionInfo implements Serializable {
        private static final long serialVersionUID = 6948932352797324730L;

        private LabelMajorMinor[] majorMinorArray;

        /**
         * Create instance using revision string and floating label flag.
         * @param revisionString the revision string.
         * @param floatingLabelFlag floating label flag.
         */
        public LabelRevisionInfo(String revisionString, boolean floatingLabelFlag) {
            try {
                byte[] revisionArray = revisionString.getBytes();
                int periodCount = 0;
                for (int i = 0; i < revisionArray.length; i++) {
                    if (revisionArray[i] == '.') {
                        periodCount++;
                    }
                }
                int majorMinorArraySize = 1 + (periodCount / 2);
                LOGGER.log(Level.FINE, "revision string: " + revisionString);
                majorMinorArray = new LabelMajorMinor[majorMinorArraySize];

                int j = 0;
                String[] majorMinorNumberStringArray = revisionString.split("\\.");
                for (int i = 0; i < majorMinorArraySize; i++) {
                    int majorNumber = Integer.parseInt(majorMinorNumberStringArray[j++]);
                    int minorNumber = Integer.parseInt(majorMinorNumberStringArray[j++]);

                    majorMinorArray[i] = new LabelMajorMinor(majorNumber, minorNumber);
                }

                // If this is a floating label, then the last minor number is
                // set to -1.
                if (floatingLabelFlag) {
                    majorMinorArray[majorMinorArray.length - 1].minorNumber.setValue(-1);
                }
            } catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, "failed to construct LabelRevisionInfo!! due to exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
                throw new QVCSRuntimeException(e.getLocalizedMessage());
            }
        }

        /**
         * A useful string representation of the label revision info.
         * @return a useful string representation of the label revision info.
         */
        @Override
        public String toString() {
            StringBuilder returnString = new StringBuilder();
            for (int i = 0; i < majorMinorArray.length; i++) {
                if (i != 0) {
                    returnString.append(".");
                }
                returnString.append(majorMinorArray[i].toString());
            }
            return returnString.toString();
        }

        /**
         * Get the major/minor pairs for this label.
         * @return the major/minor pairs for this label.
         */
        public LabelMajorMinor[] getMajorMinorPairs() {
            return majorMinorArray;
        }

        /**
         * Get a sortable revision string.
         * @return a sortable revision string.
         */
        public String getSortableRevisionString() {
            DecimalFormat formatter = new DecimalFormat("0000");
            StringBuilder resultBuffer = new StringBuilder();
            for (LabelMajorMinor labelMajorMinor : majorMinorArray) {
                int major = labelMajorMinor.majorNumber.getValue();
                int minor = labelMajorMinor.minorNumber.getValue();
                resultBuffer.append(formatter.format(major));
                resultBuffer.append(formatter.format(minor));
            }
            return resultBuffer.toString();
        }

        /**
         * Set the size of the major/minor pair array.
         * @param size the size of the major/minor pair array.
         */
        public LabelRevisionInfo(int size) {
            majorMinorArray = new LabelMajorMinor[size];
        }

        /**
         * Read from a file. Assume the stream is positioned correctly.
         * @param inStream the stream to read from.
         * @throws IOException on a read error.
         */
        public void read(RandomAccessFile inStream) throws IOException {
            for (int i = 0; i < majorMinorArray.length; i++) {
                majorMinorArray[i] = new LabelMajorMinor();
                majorMinorArray[i].read(inStream);
            }
        }

        /**
         * Write to a file. Assume the stream is positioned correctly.
         * @param outStream the stream to write to.
         * @throws IOException on a write error.
         */
        public void write(RandomAccessFile outStream) throws IOException {
            for (LabelMajorMinor labelMajorMinor : majorMinorArray) {
                labelMajorMinor.write(outStream);
            }
        }

        /**
         * Does this describe a floating label.
         * @return true if this is a floating label; false otherwise.
         */
        public boolean isFloatingLabel() {
            return majorMinorArray[majorMinorArray.length - 1].minorNumber.getValue() == (short) -1;
        }

        /**
         * Does this describe the special 'obsolete' label.
         * @return true if this is the special 'obsolete' label, false otherwise.
         * @deprecated we don't use these kinds of labels anymore.
         */
        public boolean isObsolete() {
            return (majorMinorArray[0].majorNumber.getValue() == -1)
                    && (majorMinorArray[0].minorNumber.getValue() == -1);
        }

        /**
         * Get the number of major/minor pairs that are needed to describe the revision associated with this label.
         * TODO -- this is a lousy name for this method.
         * @return the number of major/minor pairs that are needed to describe the revision associated with this label.
         */
        public int getRevisionCount() {
            return majorMinorArray.length;
        }
    }

    /**
     * Create an inner class to handle the reading of the major/minor pairs.
     */
    public class LabelMajorMinor implements Serializable {
        private static final long serialVersionUID = 1553056809254188868L;

        private CommonShort majorNumber = new CommonShort();
        private CommonShort minorNumber = new CommonShort();

        /**
         * Default constructor.
         */
        public LabelMajorMinor() {
        }

        /**
         * Create an instance using the given major/minor values.
         * @param major the major number of this revision segment.
         * @param minor the minor number of this revision segment.
         */
        public LabelMajorMinor(int major, int minor) {
            majorNumber.setValue(major);
            minorNumber.setValue(minor);
        }

        /**
         * A useful string representation of the label's major/minor revision info.
         * @return a useful string representation of the label's major/minor revision info.
         */
        @Override
        public String toString() {
            StringBuilder returnString = new StringBuilder();
            returnString.append(majorNumber.getValue());
            short sMinor = (short) minorNumber.getValue();

            // Only report the minor number if it's != -1
            if (sMinor != (short) -1) {
                returnString.append(".").append(Integer.toString(minorNumber.getValue()));
            }
            return returnString.toString();
        }

        /**
         * Get the major number.
         * @return the major number.
         */
        public int getMajorNumber() {
            return majorNumber.getValue();
        }

        /**
         * Get the minor number.
         * @return the minor number.
         */
        public int getMinorNumber() {
            return minorNumber.getValue();
        }

        /**
         * Read from a file. Assume the stream is positioned correctly.
         * @param inStream the stream to read from.
         * @throws IOException on a read error.
         */
        public void read(RandomAccessFile inStream) throws IOException {
            majorNumber.read(inStream);
            minorNumber.read(inStream);
        }

        /**
         * Write to a file. Assume the stream is positioned correctly.
         * @param outStream the stream to write to.
         * @throws IOException on a write error.
         */
        public void write(RandomAccessFile outStream) throws IOException {
            majorNumber.write(outStream);
            minorNumber.write(outStream);
        }
    }
}
