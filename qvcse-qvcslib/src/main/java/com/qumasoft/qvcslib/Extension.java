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

import java.util.Objects;

/**
 * Helper class to ease handling of file extensions.
 * @author Jim Voris
 */
public class Extension implements java.io.Serializable {
    private static final long serialVersionUID = -858422138088567696L;

    private final String extension;

    /**
     * Creates a new instance of Extension.
     * @param ext the file extension.
     */
    public Extension(String ext) {
        // Look for the final '.' character.  If it does not exist, then
        // we'll have to prefix the passed in extension, so we always have
        // an extension string that starts with the '.' character.
        byte[] bytes = ext.getBytes();
        if (bytes[0] == '.') {
            extension = ext;
        } else {
            extension = "." + ext;
        }
    }

    /**
     * Get the extension. The first character of an extension is <i>always</i> the '.' character.
     * @return the extension string captured/represented by this instance.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Test if a given file ends with this extension.
     * @param filename the filename to evaluate.
     * @return true if the filename ends with this extension; false otherwise.
     */
    public boolean endsWithExtension(String filename) {
        boolean retVal = false;

        if (filename.endsWith(extension)) {
            retVal = true;
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(extension);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Extension other = (Extension) obj;
        return Objects.equals(this.extension, other.extension);
    }
}
