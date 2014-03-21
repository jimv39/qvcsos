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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.qvcslib.UserLocationProperties;

/**
 * Add server operation.
 * @author Jim Voris
 */
public final class OperationAddServer extends OperationMaintainServerBaseClass {

    /**
     * Create an add server operation.
     * @param userLocationProperties user location properties.
     */
    public OperationAddServer(UserLocationProperties userLocationProperties) {
        super(null, userLocationProperties);
    }
}
