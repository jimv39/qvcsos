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
package com.qumasoft.qvcslib.requestdata;

/**
 * Client request operation data interface. Define those methods that must be implemented in a operation data request.
 * @author Jim Voris
 */
public interface ClientRequestOperationDataInterface extends ClientRequestDataInterface {

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    String getProjectName();

    /**
     * Get the view name.
     *
     * @return the view name.
     */
    String getBranchName();
}
