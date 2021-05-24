/*
 * Copyright 2021 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.qvcslib;

import java.util.Set;

/**
 * Simple class to hold the directory set, and the file set of a .qvcsosignore file.
 * @author Jim Voris
 */
public class IgnoreFileData {
    private Set<String> directorySet;
    private Set<String> fileSet;

    public IgnoreFileData() {
        directorySet = null;
        fileSet = null;
    }

    Set<String> getDirectorySet() {
        return directorySet;
    }

    Set<String> getFileSet() {
        return fileSet;
    }

    void setDirectorySet(Set<String> dirSet) {
        this.directorySet = dirSet;
    }

    void setFileSet(Set<String> fSet) {
        this.fileSet = fSet;
    }

}
