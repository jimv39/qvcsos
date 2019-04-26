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
package com.qumasoft.guitools.merge;

import java.awt.Color;

/**
 * Color manager for gui merge tool.
 * @author Jim Voris
 */
public final class ColorManager {

    // <editor-fold>
    private static final Color NORMAL_COLOR = new Color(0, 0, 0);
    private static final Color INSERT_FOREGROUND_COLOR = new Color(0, 125, 110);
    private static final Color DELETE_FOREGROUND_COLOR = new Color(255, 10, 0);
    private static final Color REPLACE_FOREGROUND_COLOR = new Color(0, 10, 255);
    private static final Color CHANGE_BACKGROUND_COLOR = new Color(210, 210, 210);
    private static final Color COLLISION_BACKGROUND_COLOR = new Color(255, 200, 68);
    private static final Color DELETED_ROW_LINE_NUMBER_BACKGROUND_COLOR = new Color(200, 100, 100);
    private static final Color FIRST_DECENDENT_CHANGE_BACKGROUND_COLOR = new Color(233, 239, 248);
    private static final Color SECOND_DECENDENT_CHANGE_BACKGROUND_COLOR = new Color(248, 239, 233);
    private static final Color REPLACE_COMPARE_HILITE_BACKGROUND_COLOR = new Color(160, 200, 255);
    // </editor-fold>

    // Hide the default constructor.
    private ColorManager() {
    }

    static Color getNormalColor() {
        return NORMAL_COLOR;
    }

    static Color getReplaceForegroundColor() {
        return REPLACE_FOREGROUND_COLOR;
    }

    static Color getInsertForegroundColor() {
        return INSERT_FOREGROUND_COLOR;
    }

    static Color getDeleteForegroundColor() {
        return DELETE_FOREGROUND_COLOR;
    }

    static Color getChangeBackgroundColor() {
        return CHANGE_BACKGROUND_COLOR;
    }

    static Color getCollisionBackgroundColor() {
        return COLLISION_BACKGROUND_COLOR;
    }

    static Color getDeletedRowLineNumberBackgroundColor() {
        return DELETED_ROW_LINE_NUMBER_BACKGROUND_COLOR;
    }

    static Color getFirstDecendentChangeBackgroundColor() {
        return FIRST_DECENDENT_CHANGE_BACKGROUND_COLOR;
    }

    static Color getSecondDecendentChangeBackgroundColor() {
        return SECOND_DECENDENT_CHANGE_BACKGROUND_COLOR;
    }

    /**
     * Get the color for a replace background color. This is public because it is also used by ContentRow.
     * @return the color for a replace background color.
     */
    public static Color getReplaceCompareHiliteBackgroundColor() {
        return REPLACE_COMPARE_HILITE_BACKGROUND_COLOR;
    }
}
