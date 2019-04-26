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
import java.util.Objects;

/**
 * Role types. There can be any number of different role types. By default there are several created automatically when a server is started for the very first time. The default
 * types are:
 * <ul>
 * <li>ADMIN</li>
 * <li>PROJECT_ADMIN</li>
 * <li>READER</li>
 * <li>WRITER</li>
 * <li>DEVELOPER</li>
 * <li>CEMETERY_ADMIN</li>
 * </ul>
 * @author Jim Voris
 */
public class RoleType implements Serializable {
    private static final long serialVersionUID = -3689812653200699967L;

    private final String roleType;
    private static final int HASH_START_VALUE = 7;
    private static final int HASH_MULTIPLIER = 79;

    /**
     * Creates a new instance of RoleType.
     * @param type the name for the role type.
     */
    public RoleType(String type) {
        roleType = type;
    }

    /**
     * Get the role type.
     * @return the role type.
     */
    public String getRoleType() {
        return roleType;
    }

    /**
     * Convenience toString to report the role type.
     * @return report the role type string.
     */
    @Override
    public String toString() {
        return getRoleType();
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof RoleType) {
            RoleType r = (RoleType) o;
            return roleType.equals(r.roleType);
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = HASH_START_VALUE;
        hash = HASH_MULTIPLIER * hash + Objects.hashCode(this.roleType);
        return hash;
    }
}
