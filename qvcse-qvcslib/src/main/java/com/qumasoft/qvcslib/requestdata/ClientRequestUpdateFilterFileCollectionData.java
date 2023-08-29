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
package com.qumasoft.qvcslib.requestdata;

import com.qumasoft.qvcslib.CommonFilterFileCollection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jim Voris.
 */
public class ClientRequestUpdateFilterFileCollectionData extends ClientRequestClientData {
    /** Add a file filter collection request. */
    public static final int ADD_FILE_FILTER_COLLECTION_REQUEST = 1;
    /** Reset filter collection. */
    public static final int RESET_FILE_FILTER_COLLECTION_REQUEST = 2;

    private List<CommonFilterFileCollection> commonFilterFileCollectionList = new ArrayList<>();
    private Integer updateType;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.SYNC_TOKEN
    };


    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.UPDATE_FILTER_FILE_COLLECTION;
    }

    /**
     * @return the commonFilterFileCollectionList
     */
    public List<CommonFilterFileCollection> getCommonFilterFileCollectionList() {
        return commonFilterFileCollectionList;
    }

    /**
     * @param filterCollectionList the commonFilterFileCollection to set
     */
    public void setCommonFilterFileCollectionList(List<CommonFilterFileCollection> filterCollectionList) {
        this.commonFilterFileCollectionList = filterCollectionList;
    }

    /**
     * @return the updateType
     */
    public Integer getUpdateType() {
        return updateType;
    }

    /**
     * @param ut the updateType to set
     */
    public void setUpdateType(Integer ut) {
        this.updateType = ut;
    }

    public void addCommonFilterFileCollection(CommonFilterFileCollection cffc) {
        commonFilterFileCollectionList.add(cffc);
    }

}
