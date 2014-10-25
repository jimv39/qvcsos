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
 * Interface for QVCS operations.
 * @author Jim Voris
 */
public interface QVCSOperation {

    /**
     * Implement the QVCSOperation interface's execute method to allow your class to execute a QVCS command. execute() returns true if the command succeeds, false if the command
     * failed. Throw a QVCSOperationException if anything goes wrong.
     *
     * @param arguments the command arguments.
     * @throws QVCSOperationException if anything QVCS related goes wrong.
     * @return true if the operation completed successfully; false otherwise.
     */
    boolean execute(String[] arguments) throws QVCSOperationException;

    /**
     * Implement the QVCSOperation interface's execute method to allow your class to execute a QVCS command. execute() returns true if the command succeeds, false if the
     * command failed. Throw a QVCSOperationException if anything goes wrong.
     *
     * @throws QVCSOperationException if anything QVCS related goes wrong.
     * @return true if the operation completed successfully; false otherwise.
     */
    boolean execute() throws QVCSOperationException;
}
