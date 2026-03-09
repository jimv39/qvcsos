/*
 * Copyright 2026 Jim Voris.
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
package com.qvcsos.server.datamodel;

/**
 *
 * @author Jim Voris
 */
public class Label {
    private Integer labelId;
    private String labelText;

    /**
     * @return the labelId
     */
    public Integer getLabelId() {
        return labelId;
    }

    /**
     * @param id the labelId to set
     */
    public void setLabelId(Integer id) {
        this.labelId = id;
    }

    /**
     * @return the labelText
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * @param label the labelText to set
     */
    public void setLabelText(String label) {
        this.labelText = label;
    }
}
