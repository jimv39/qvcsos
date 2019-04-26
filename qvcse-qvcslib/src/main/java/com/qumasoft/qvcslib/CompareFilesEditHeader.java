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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Compare files edit header. This is the information that goes at the very beginning of an edit script... Looks like it's not really used any more, but still acts as a place
 * holder so we don't have to change the structure of what gets written to disk.
 * @author Jim Voris
 */
public class CompareFilesEditHeader {

    private CommonTime timeOfTarget;
    private Common32Long baseFileSize;

    private static final int INSTANCE_BYTE_COUNT = 8;

    /**
     * Get the number of bytes this uses when written to disk.
     * @return the number of bytes this uses when written to disk.
     */
    public static int getEditHeaderSize() {
        return INSTANCE_BYTE_COUNT;
    }

    /**
     * Write the header to a data output stream.
     * @param outStream the stream to write to.
     * @throws IOException on a write problem.
     */
    public void write(DataOutputStream outStream) throws IOException {
        baseFileSize.write(outStream);
        timeOfTarget.write(outStream);
    }

    /**
     * Get the time of target.
     * @return the timeOfTarget the time of target
     */
    public CommonTime getTimeOfTarget() {
        return timeOfTarget;
    }

    /**
     * Set the time of target. Basically the time that the edit script was created.
     * @param timeOfTarg the time of target.
     */
    public void setTimeOfTarget(CommonTime timeOfTarg) {
        this.timeOfTarget = timeOfTarg;
    }

    /**
     * Get the base file size.
     * @return the baseFileSize the base file size.
     */
    public Common32Long getBaseFileSize() {
        return baseFileSize;
    }

    /**
     * Set the base file size.
     * @param baseFileSz the base file size.
     */
    public void setBaseFileSize(Common32Long baseFileSz) {
        this.baseFileSize = baseFileSz;
    }
}
