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
import java.io.OutputStream;

/**
 * Keyword expansion context. A convenience data object for holding the context data used during a keyword expansion operation. We need it for two reasons:
 * <ol>
 * <li>So we don't get pesky checkstyle warnings for passing too many arguments to methods.</li>
 * <li>So our keyword expansion classes can be stateless. All the state surrounding a given expansion operation is contained here, instead of as a class variable in the
 * keyword expansion implementation class.</li>
 * </ol>
 * @author Jim Voris
 */
public class KeywordExpansionContext {

    private final OutputStream outStream;
    private final File outputFile;
    private final LogfileInfo logfileInfo;
    private final int revisionIndex;
    private final String labelString;
    private final String appendedPath;
    private final AbstractProjectProperties projectProperties;
    private boolean binaryFileFlag;

    /**
     * Create an expansion context instance.
     * @param oStream the output stream.
     * @param outFile the output file (used for expansion of the Filename keyword).
     * @param info the logfile info.
     * @param revIndex the revision index.
     * @param label the label string.
     * @param path the appended path.
     * @param projProperties the project properties.
     */
    public KeywordExpansionContext(OutputStream oStream, File outFile, LogfileInfo info, int revIndex, String label, String path, AbstractProjectProperties projProperties) {
        this.outStream = oStream;
        this.outputFile = outFile;
        this.logfileInfo = info;
        this.revisionIndex = revIndex;
        this.labelString = label;
        this.appendedPath = path;
        this.projectProperties = projProperties;
    }

    /**
     * Get the out stream.
     * @return the out stream;
     */
    public OutputStream getOutStream() {
        return outStream;
    }

    /**
     * Get the output file.
     * @return the output file.
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Get the logfile info.
     * @return the logfile info.
     */
    public LogfileInfo getLogfileInfo() {
        return logfileInfo;
    }

    /**
     * Get the revision index.
     * @return the revision index.
     */
    public int getRevisionIndex() {
        return revisionIndex;
    }

    /**
     * Get the label string.
     * @return the label string.
     */
    public String getLabelString() {
        return labelString;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * Get the project properties.
     * @return the project properties.
     */
    public AbstractProjectProperties getProjectProperties() {
        return projectProperties;
    }

    /**
     * Get the binary file flag.
     * @return the binary file flag.
     */
    public boolean getBinaryFileFlag() {
        return binaryFileFlag;
    }

    /**
     * Set the binary file flag.
     * @param flag the binary file flag.
     */
    public void setBinaryFileFlag(boolean flag) {
        this.binaryFileFlag = flag;
    }
}
