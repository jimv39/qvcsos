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
package com.qumasoft.guitools.qwin;

import java.util.List;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author Jim Voris
 */
public class TagComboModel extends DefaultComboBoxModel<String> {

    /**
     * Default constructor.Populate the model from the database.
     * @param tagList list of tags.
     */
    public TagComboModel(List<String> tagList) {
        tagList.forEach(tag -> {
            addElement(tag);
        });
    }
}
