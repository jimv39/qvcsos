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

import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The KeywordManagerInterface is meant to abstract away the action of expanding and contracting keywords. <br><br> One implementation of
 * this interface would be used to expand/contract QVCS flavor keywords; a separate implementation of this interface could be used to
 * expand/contract keywords for PVCS, or CVS, or SourceSafe, or StarTeam, etc.
 *
 * @author Jim Voris
 */
public interface KeywordManagerInterface {

    /**
     * Expand any keywords present in the inStream and write the result to the outStream.
     *
     * @param inStream [input] the stream containing the unexpanded keywords. The caller must close the stream.
     * @param expansionContext the expansion context for holding parameters and state.
     * @throws java.io.IOException on any read or write errors.
     * @throws com.qumasoft.qvcslib.QVCSException for any QVCS specific problems.
     */
    void expandKeywords(FileInputStream inStream, KeywordExpansionContext expansionContext) throws java.io.IOException, QVCSException;

    /**
     * Expand any keywords present in the inStream and write the result to the outStream.
     *
     * @param inBuffer [input] the buffer containing the unexpanded keywords
     * @param expansionContext the expansion context for holding parameters and state.
     * @throws java.io.IOException on any read or write errors.
     * @throws com.qumasoft.qvcslib.QVCSException for any QVCS specific problems.
     */
    void expandKeywords(byte[] inBuffer, KeywordExpansionContext expansionContext) throws java.io.IOException, QVCSException;

    /**
     * Contract any keywords present in the inStream and write the result to the outStream. If the Comment keyword is present in the
     * inStream, contract that, and store the captured comment string in the checkInComment AtomicReference<String>.
     *
     * @param inStream [input] the stream containing the expanded keywords
     * @param outStream [output] the stream containing the contracted keywords
     * @param checkInComment [output] the AtomicReference<String> containing and check-in comments extracted from the inStream by
     * contracting the Comment keyword.
     * @param projectProperties The project properties for the current project.
     * @param binaryFileFlag true if working on a binary file.
     * @throws java.io.IOException on any read or write errors.
     * @throws com.qumasoft.qvcslib.QVCSException for any QVCS specific problems.
     */
    void contractKeywords(FileInputStream inStream,
            OutputStream outStream,
            AtomicReference<String> checkInComment,
            AbstractProjectProperties projectProperties,
            boolean binaryFileFlag) throws java.io.IOException, QVCSException;
}
