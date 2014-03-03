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
 * Move a file action.
 * @author Jim Voris
 */
public class LogfileActionMoveFile extends LogfileActionType {

    private final String originAppendedPath;
    private final String destinationAppendedPath;

    /**
     * Creates a new instance of LogfileActionMoveFile.
     * @param originPath the origin appended path.
     * @param destinationPath the destination appended path.
     */
    public LogfileActionMoveFile(String originPath, String destinationPath) {
        super("MoveFile", LogfileActionType.MOVE_FILE);
        originAppendedPath = originPath;
        destinationAppendedPath = destinationPath;
    }

    /**
     * Get the origin appended path.
     * @return the origin appended path.
     */
    public String getOriginAppendedPath() {
        return originAppendedPath;
    }

    /**
     * Get the destination appended path.
     * @return the destination appended path.
     */
    public String getDestinationAppendedPath() {
        return destinationAppendedPath;
    }
}
