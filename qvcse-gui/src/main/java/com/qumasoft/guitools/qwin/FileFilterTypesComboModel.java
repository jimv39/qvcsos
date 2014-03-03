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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.QVCSConstants;
import javax.swing.DefaultComboBoxModel;

/**
 * File filter types combo model.
 * @author Jim Voris
 */
public class FileFilterTypesComboModel extends DefaultComboBoxModel<String> {
    private static final long serialVersionUID = -2395632387879704164L;

    FileFilterTypesComboModel() {
        addElement(QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER);
        addElement(QVCSConstants.EXTENSION_FILTER);
        addElement(QVCSConstants.EXCLUDE_EXTENSION_FILTER);
        addElement(QVCSConstants.REG_EXP_FILENAME_FILTER);
        addElement(QVCSConstants.EXCLUDE_REG_EXP_FILENAME_FILTER);
        addElement(QVCSConstants.REG_EXP_REV_DESC_FILTER);
        addElement(QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER);
        addElement(QVCSConstants.STATUS_FILTER);
        addElement(QVCSConstants.EXCLUDE_STATUS_FILTER);
        addElement(QVCSConstants.LOCKED_BY_FILTER);
        addElement(QVCSConstants.EXCLUDE_LOCKED_BY_FILTER);
        addElement(QVCSConstants.CHECKED_IN_AFTER_FILTER);
        addElement(QVCSConstants.CHECKED_IN_BEFORE_FILTER);
        addElement(QVCSConstants.FILESIZE_GREATER_THAN_FILTER);
        addElement(QVCSConstants.FILESIZE_LESS_THAN_FILTER);
        addElement(QVCSConstants.LAST_EDIT_BY_FILTER);
        addElement(QVCSConstants.EXCLUDE_LAST_EDIT_BY_FILTER);
        addElement(QVCSConstants.WITH_LABEL_FILTER);
        addElement(QVCSConstants.WITH_LABEL_FILTER_WITH_ALL_REVISIONS);
        addElement(QVCSConstants.WITHOUT_LABEL_FILTER);
        addElement(QVCSConstants.AFTER_LABEL_FILTER);
        addElement(QVCSConstants.AFTER_LABEL_FILTER_INCLUDE_MISSING);
        addElement(QVCSConstants.UPTO_LABEL_FILTER);
    }
}
