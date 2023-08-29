/*
 * Copyright 2023 Jim Voris.
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
 * @author Jim Voris.
 */
public class FilterType {
    private Integer id;
    private String filterType;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param typeId the id to set
     */
    public void setId(Integer typeId) {
        this.id = typeId;
    }

    /**
     * @return the filterType
     */
    public String getFilterType() {
        return filterType;
    }

    /**
     * @param typeName the filterType to set
     */
    public void setFilterType(String typeName) {
        this.filterType = typeName;
    }
}
