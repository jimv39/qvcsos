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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.requestdata.ClientRequestAddUserPropertyData;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Jim Voris.
 */
public class RemotePropertiesBaseClass {
    private final String userName;
    private final TransportProxyInterface transportProxy;
    private final Map<String, UserPropertyData> userPropertyMap;
    private final String propertyKey;

    private static final String IS_READ_ONLY_BRANCH_FLAG_TAG = "QVCS_ISREADONLY_BRANCH_FLAG";
    private static final String IS_TAG_BASED_BRANCH_FLAG_TAG = "QVCS_ISTAG_BASED_BRANCH_FLAG";
    private static final String IS_FEATURE_BRANCH_FLAG_TAG = "QVCS_ISFEATURE_BRANCH_FLAG";
    private static final String IS_RELEASE_BRANCH_FLAG_TAG = "QVCS_ISRELEASE_BRANCH_FLAG";
    private static final String TAG_BASED_BRANCH_TAG = "QVCS_TAG_BASED_BRANCH_TAG";
    private static final String BRANCH_PARENT_TAG = "QVCS_BRANCH_PARENT";
    private static final String BRANCH_ANCHOR_DATE_TAG = "QVCS_BRANCH_ANCHOR_DATE_TAG";
    private static final String MOVEABLE_TAG_FLAG_TAG = "QVCS_MOVEABLE_TAG_TAG";
    private static final String WORKFILE_LOCATION_TAG = "_QVCS_WORKFILELOCATION";

    private static final String IGNORE_LEADING_WHITE_SPACE_TAG = "VisualCompare-IgnoreLeadingWhitespace";
    private static final String IGNORE_ALL_WHITE_SPACE_TAG = "VisualCompare-IgnoreAllWhitespace";
    private static final String IGNORE_CASE_TAG = "VisualCompare-IgnoreCase";
    private static final String IGNORE_EOL_CHANGES_TAG = "VisualCompare-IgnoreEOLChanges";
    private static final String MRU_FILE1_NAME_TAG = "VisualCompare-MRUFile1Name";
    private static final String MRU_FILE2_NAME_TAG = "VisualCompare-MRUFile2Name";

    private static final String IGNORE_HIDDEN_DIRECTORIES_TAG = "QVCS_IgnoreHiddenDirectories";
    private static final String MOST_RECENT_APPENDED_PATH_TAG = "QVCS_MostRecentAppendedPath";
    private static final String MOST_RECENT_PROJECT_NAME_TAG = "QVCS_MostRecentProjectName";
    private static final String MOST_RECENT_BRANCH_NAME_TAG = "QVCS_MostRecentBranchName";
    private static final String MOST_RECENT_PROJECT_TYPE_TAG = "QVCS_MostRecentProjectType";
    private static final String ACTIVITY_PANE_LOG_LEVEL_TAG = "QVCS_ActivityPaneLogLevel";
    private static final String USE_EXTERNAL_VISUAL_COMPARE_TOOL_FLAG_TAG = "QVCS_UseExternalVisualCompareToolFlagTag";
    private static final String EXTERNAL_VISUAL_COMPARE_COMMAND_LINE_TAG = "QVCS_ExternalVisualCompareCommandLineTag";

    private static final String AUTO_UPDATE_FLAG_TAG = "QVCS_AUTOUPDATEFLAG";
    private static final String AUTO_UPDATE_INTERVAL_TAG = "QVCS_AUTOUPDATEINTERVAL";
    private static final int DEFAULT_UPDATE_INTERVAL = 10;
    private static final String ACTIVE_FILE_FILTER_NAME_TAG = "QVCS_ActiveFileFilterNameTag";
    private static final String COLUMN_WIDTH_TAG = "QVCS_ColumnWidth";
    private static final int DEFAULT_COLUMN_WIDTH = 150;
    private static final String CURRENT_SORT_COLUMN_TAG = "QVCS_CurrentSortColumn";
    private static final String FILE_LIST_HEIGHT_TAG = "QVCS_FileListHeight";
    private static final int DEFAULT_FILE_LIST_HEIGHT = 100;
    private static final String FONT_SIZE_TAG = "QVCS_FontSize";
    private static final int DEFAULT_FONT_SIZE = 12;
    private static final String FRAME_HEIGHT_TAG = "QVCS_FrameHeight";
    private static final int DEFAULT_FRAME_HEIGHT = 500;
    private static final String FRAME_WIDTH_TAG = "QVCS_FrameWidth";
    private static final int DEFAULT_FRAME_WIDTH = 800;
    private static final String FRAME_X_LOCATION_TAG = "QVCS_FrameXLocation";
    private static final int DEFAULT_FRAME_X_LOCATION = 200;
    private static final String FRAME_Y_LOCATION_TAG = "QVCS_FrameYLocation";
    private static final int DEFAULT_FRAME_Y_LOCATION = 400;
    private static final String FRAME_MAXIMIZE_FLAG_TAG = "QVCS_MaximizeFlag";
    private static final String LARGE_TOOLBAR_BUTTON_TAG = "QVCS_UseLargeToolbarButtons";
    private static final String LOOK_AND_FEEL_TAG = "QVCS_LookAndFeel";
    private static final String TREE_WIDTH_TAG = "QVCS_TreeWidth";
    private static final int DEFAULT_TREE_WIDTH = 200;
    private static final String USE_COLORED_FILE_ICONS_TAG = "QVCS_UseColoredFileIcons";


    public RemotePropertiesBaseClass(String uName, TransportProxyInterface proxy, String key) {
        this.userName = uName;
        this.transportProxy = proxy;
        this.userPropertyMap = new TreeMap<>();
        this.propertyKey = key;
    }
    /**
     * Get the is read-only branch flag tag.
     * @return the is read-only branch flag tag.
     */
    public static String getStaticIsReadOnlyBranchFlagTag() {
        return IS_READ_ONLY_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is read-only branch flag tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the is read-only branch flag tag.
     */
    public static String getIsReadOnlyBranchFlagTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + getStaticIsReadOnlyBranchFlagTag();
    }

    /**
     * Get the is read-only branch flag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the is read-only branch flag.
     */
    public Boolean getIsReadOnlyBranchFlag(String projectName, String branchName) {
        return getBooleanValue(getIsReadOnlyBranchFlagTag(projectName, branchName));
    }

    /**
     * Set the is read-only branch flag.
     * @param projectName project name.
     * @param branchName branch name.
     * @param flag the is read-only branch flag.
     */
    public void setIsReadOnlyBranchFlag(String projectName, String branchName, boolean flag) {
        setBooleanValue(getIsReadOnlyBranchFlagTag(projectName, branchName), flag);
    }

    /**
     * Get the is date-based branch flag tag.
     * @return the is date-based branch flag tag.
     */
    public static String getStaticIsTagBasedBranchFlagTag() {
        return IS_TAG_BASED_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is date-based branch flag tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the is date-based branch flag tag.
     */
    public static String getIsTagBasedBranchFlagTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticIsTagBasedBranchFlagTag();
    }

    /**
     * Get the is tag based branch flag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the is tag based branch flag.
     */
    public boolean getIsTagBasedBranchFlag(String projectName, String branchName) {
        return getBooleanValue(getIsTagBasedBranchFlagTag(projectName, branchName));
    }

    /**
     * Get the is feature branch flag tag.
     * @return the is feature branch flag tag.
     */
    public static String getStaticIsFeatureBranchFlagTag() {
        return IS_FEATURE_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is feature branch flag tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the is feature branch flag tag.
     */
    public static String getIsFeatureBranchFlagTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticIsFeatureBranchFlagTag();
    }

    /**
     * Get the is feature branch flag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the is feature branch flag.
     */
    public boolean getIsFeatureBranchFlag(String projectName, String branchName) {
        return getBooleanValue(getIsFeatureBranchFlagTag(projectName, branchName));
    }

    /**
     * Get the is release branch flag tag.
     *
     * @return the is release branch flag tag.
     */
    public static String getStaticIsReleaseBranchFlagTag() {
        return IS_RELEASE_BRANCH_FLAG_TAG;
    }

    /**
     * Get the is release branch flag tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the is release branch flag tag.
     */
    public static String getIsReleaseBranchFlagTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticIsReleaseBranchFlagTag();
    }

    /**
     * Get the is release branch flag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the is release branch flag.
     */
    public boolean getIsReleaseBranchFlag(String projectName, String branchName) {
        return getBooleanValue(getIsReleaseBranchFlagTag(projectName, branchName));
    }

    /**
     * Get the date based branch date tag.
     * @return the date based branch date tag.
     */
    public static String getStaticTagBasedBranchTag() {
        return TAG_BASED_BRANCH_TAG;
    }

    /**
     * Get the date based branch date tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the date based branch date tag.
     */
    public static String getTagBasedBranchTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticTagBasedBranchTag();
    }

    /**
     * Get the tag based tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the tag based tag.
     */
    public String getTagBasedTag(String projectName, String branchName) {
        return getStringValue(getTagBasedBranchTag(projectName, branchName));
    }

    /**
     * Get the branch parent tag.
     * @return the branch parent tag.
     */
    public static String getStaticBranchParentTag() {
        return BRANCH_PARENT_TAG;
    }

    /**
     * Get the branch parent tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the branch parent tag.
     */
    public static String getBranchParentTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticBranchParentTag();
    }

    /**
     * Get the branch parent name.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the branch parent name.
     */
    public String getBranchParent(String projectName, String branchName) {
        return getStringValue(getBranchParentTag(projectName, branchName));
    }

    public static String getStaticBranchAnchorDateTag() {
        return BRANCH_ANCHOR_DATE_TAG;
    }

    /**
     * Get the branch anchor date tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the branch anchor date tag.
     */
    public static String getBranchAnchorDateTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticBranchAnchorDateTag();
    }

    /**
     * Get the branch anchor date.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the branch anchor date.
     */
    public Date getBranchAnchorDate(String projectName, String branchName) {
        String anchor = getStringValue(getBranchAnchorDateTag(projectName, branchName));
        Long anchorTime = Long.valueOf(anchor);
        return new Date(anchorTime);
    }


    public static String getStaticMoveableTagTag() {
        return MOVEABLE_TAG_FLAG_TAG;
    }

    /**
     * Get the branch moveable tag tag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the branch moveable tag tag.
     */
    public static String getMoveableTagTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticMoveableTagTag();
    }

    public boolean getIsMoveableTagBranchFlag(String projectName, String branchName) {
        return getBooleanValue(getMoveableTagTag(projectName, branchName));
    }


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the ignore hidden directories tag.
     * @return the String constant we use in the property file to identify the ignore hidden directories setting.
     */
    public static String getStaticIgnoreHiddenDirectoriesTag() {
        return IGNORE_HIDDEN_DIRECTORIES_TAG;
    }

    public static String getIgnoreHiddenDirectoriesTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticIgnoreHiddenDirectoriesTag();
    }

    /**
     * Get the most recent appended path tag.
     * @return the String constant we use in the property file to identify the most recent appendedPath property.
     */
    public static String getStaticMostRecentAppendedPathTag() {
        return MOST_RECENT_APPENDED_PATH_TAG;
    }

    public static String getMostRecentAppendedPathTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticMostRecentAppendedPathTag();
    }

    /**
     * Get the most recent project name tag.
     * @return the String constant we use in the property file to identify the most recent project name property.
     */
    public static String getStaticMostRecentProjectNameTag() {
        return MOST_RECENT_PROJECT_NAME_TAG;
    }

    public static String getMostRecentProjectNameTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticMostRecentProjectNameTag();
    }

    /**
     * Get the most recent branch name tag.
     * @return the String constant we use in the property file to identify the most recent branch name property.
     */
    public static String getStaticMostRecentBranchNameTag() {
        return MOST_RECENT_BRANCH_NAME_TAG;
    }

    public static String getMostRecentBranchNameTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticMostRecentBranchNameTag();
    }

    /**
     * Get the most recent project type tag.
     * @return the String constant we use in the property file to identify the most recent project type property.
     */
    public static String getStaticMostRecentProjectTypeTag() {
        return MOST_RECENT_PROJECT_TYPE_TAG;
    }

    public static String getMostRecentProjectTypeTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticMostRecentProjectTypeTag();
    }

    /**
     * Get the activity pane log level tag.
     * @return the String constant we use in the property file to identify the log level used in the activity pane property.
     */
    public static String getStaticActivityPaneLogLevelTag() {
        return ACTIVITY_PANE_LOG_LEVEL_TAG;
    }

    public static String getActivityPaneLogLevelTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticActivityPaneLogLevelTag();
    }

    /**
     * Get the use external visual compare tool flag tag.
     * @return the String constant we use in the property file to identify whether we use an external visual compare tool property.
     */
    public static String getStaticUseExternalVisualCompareToolFlagTag() {
        return USE_EXTERNAL_VISUAL_COMPARE_TOOL_FLAG_TAG;
    }

    public static String getUseExternalVisualCompareToolFlagTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticUseExternalVisualCompareToolFlagTag();
    }

    /**
     * Get the external visual compare command line tag.
     * @return the String constant we use in the property file to identify command line used for the external visual compare tool property.
     */
    public static String getStaticExternalVisualCommandLineTag() {
        return EXTERNAL_VISUAL_COMPARE_COMMAND_LINE_TAG;
    }

    public static String getExternalVisualCommandLineTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticExternalVisualCommandLineTag();
    }

    /**
     * Get the static active file filter name tag.
     * @return the String constant we use in the property file to identify the active file filter name property.
     */
    public static String getStaticActiveFileFilterNameTag() {
        return ACTIVE_FILE_FILTER_NAME_TAG;
    }

    public static String getActiveFileFilterNameTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticActiveFileFilterNameTag();
    }

    /**
     * Get the font size tag.
     * @return the String constant we use in the property file to identify font size property.
     */
    public static String getStaticFontSizeTag() {
        return FONT_SIZE_TAG;
    }

    public static String getFontSizeTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticFontSizeTag();
    }

    /**
     * Get the auto update flag tag.
     * @return the String constant we use in the property file to identify the auto update flag property.
     */
    public static String getStaticAutoUpdateFlagTag() {
        return AUTO_UPDATE_FLAG_TAG;
    }

    public static String getAutoUpdateFlagTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticAutoUpdateFlagTag();
    }

    /**
     * Get the auto update interval tag.
     * @return the String constant we use in the property file to identify the auto update interval property.
     */
    public static String getStaticAutoUpdateIntervalTag() {
        return AUTO_UPDATE_INTERVAL_TAG;
    }

    public static String getAutoUpdateIntervalTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticAutoUpdateIntervalTag();
    }

    /**
     * Get the column width tag for the given column.
     * @param columnNumber the column for which we want the column width'd tag.
     * @return the String constant we use in the property file to identify the column width property for the given column.
     */
    public static String getStaticColumnWidthTag(int columnNumber) {
        return COLUMN_WIDTH_TAG + Integer.toString(columnNumber);
    }

    public static String getColumnWidthTag(String projectName, String branchName, int columnNumber) {
        return projectName + ":" + branchName + ":" + ":" + getStaticColumnWidthTag(columnNumber);
    }

    /**
     * Get the current sort column tag.
     * @return the String constant we use in the property file to identify the most recent sort column property.
     */
    public static String getStaticCurrentSortColumnTag() {
        return CURRENT_SORT_COLUMN_TAG;
    }

    public static String getCurrentSortColumnTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticCurrentSortColumnTag();
    }

    /**
     * Get the look-and-feel tag.
     * @return the String constant we use in the property file to identify the look-and-feel property.
     */
    public static String getStaticLookAndFeelTag() {
        return LOOK_AND_FEEL_TAG;
    }

    public static String getLookAndFeelTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticLookAndFeelTag();
    }

    /**
     * Get the use colored file icons tag.
     * @return the String constant we use in the property file to identify the use colored file icons property.
     */
    public static String getStaticUseColoredFileIconsTag() {
        return USE_COLORED_FILE_ICONS_TAG;
    }

    public static String getUseColoredFileIconsTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticUseColoredFileIconsTag();
    }

    /**
     * Get the use large toolbar buttons tag.
     * @return the String constant we use in the property file to identify the use large toolbar property.
     */
    public static String getStaticUseLargeToolbarButtonsTag() {
        return LARGE_TOOLBAR_BUTTON_TAG;
    }

    public static String getUseLargeToolbarButtonsTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticUseLargeToolbarButtonsTag();
    }

    /**
     * Get the file list height tag.
     * @return the String constant we use in the property file to identify the file list height property.
     */
    public static String getStaticFileListHeightTag() {
        return FILE_LIST_HEIGHT_TAG;
    }

    public static String getFileListHeightTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticFileListHeightTag();
    }

    /**
     * Get the frame height tag.
     * @return the String constant we use in the property file to identify the frame height property.
     */
    public static String getStaticFrameHeightTag() {
        return FRAME_HEIGHT_TAG;
    }

    public static String getFrameHeightTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticFrameHeightTag();
    }

    /**
     * Get the frame width tag.
     * @return the String constant we use in the property file to identify the frame width property.
     */
    public static String getStaticFrameWidthTag() {
        return FRAME_WIDTH_TAG;
    }

    public static String getFrameWidthTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticFrameWidthTag();
    }

    /**
     * Get the frame X location tag.
     * @return the String constant we use in the property file to identify the frame's X location property.
     */
    public static String getStaticFrameXLocationTag() {
        return FRAME_X_LOCATION_TAG;
    }

    public static String getFrameXLocationTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticFrameXLocationTag();
    }

    /**
     * Get the frame Y location tag.
     * @return the String constant we use in the property file to identify the frame's Y location property.
     */
    public static String getStaticFrameYLocationTag() {
        return FRAME_Y_LOCATION_TAG;
    }

    public static String getFrameYLocationTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticFrameYLocationTag();
    }

    /**
     * Get the frame maximize flag tag.
     * @return the String constant we use in the property file to identify the frame's maximize property.
     */
    public static String getStaticFrameMaximizeFlagTag() {
        return FRAME_MAXIMIZE_FLAG_TAG;
    }

    public static String getFrameMaximizeFlagTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticFrameMaximizeFlagTag();
    }

    /**
     * Get the tree width tag.
     * @return the String constant we use in the property file to identify the tree width property.
     */
    public static String getStaticTreeWidthTag() {
        return TREE_WIDTH_TAG;
    }

    public static String getTreeWidthTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticTreeWidthTag();
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    /**
     * Set the ignore hidden directories flag.When true, QVCS-Enterprise will ignore directories starting with the '.' character.
     * @param projectName project name.
     * @param branchName branch name.
     * @param flag value for the hidden directories flag.
     */
    public void setIgnoreHiddenDirectoriesFlag(String projectName, String branchName, boolean flag) {
        setBooleanValue(getIgnoreHiddenDirectoriesTag(projectName, branchName), flag);
    }

    /**
     * Get the ignore hidden directories flag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the hidden directories flag.
     */
    public boolean getIgnoreHiddenDirectoriesFlag(String projectName, String branchName) {
        return getBooleanValue(getIgnoreHiddenDirectoriesTag(projectName, branchName));
    }

    /**
     * Set the most recent appendedPath.
     * @param projectName project name.
     * @param branchName branch name.
     * @param appendedPath the most recent appended path.
     */
    public void setMostRecentAppendedPath(String projectName, String branchName, String appendedPath) {
        setStringValue(getMostRecentAppendedPathTag(projectName, branchName), appendedPath);
    }

    /**
     * Get the most recent appendedPath.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the most recent appendedPath.
     */
    public String getMostRecentAppendedPath(String projectName, String branchName) {
        return getStringValue(getMostRecentAppendedPathTag(projectName, branchName));
    }

    /**
     * Get the most recent project name.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the most recent project name.
     */
    public String getMostRecentProjectName(String projectName, String branchName) {
        return getStringValue(getMostRecentProjectNameTag(projectName, branchName));
    }

    /**
     * Set the most recent project name.
     * @param projectName project name.
     * @param branchName branch name.
     * @param mostRecentProjectName the most recent project name.
     */
    public void setMostRecentProjectName(String projectName, String branchName, String mostRecentProjectName) {
        setStringValue(getMostRecentProjectNameTag(projectName, branchName), mostRecentProjectName);
    }

    /**
     * Get the most recent branch name.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the most recent branch name.
     */
    public String getMostRecentBranchName(String projectName, String branchName) {
        String mostRecentBranchName = getStringValue(getMostRecentBranchNameTag(projectName, branchName));
        if (mostRecentBranchName == null || mostRecentBranchName.length() == 0) {
            mostRecentBranchName = QVCSConstants.QVCS_TRUNK_BRANCH;
        }
        return mostRecentBranchName;
    }

    /**
     * Set the most recent branch name.
     * @param projectName project name.
     * @param branchName branch name.
     * @param mostRecentBranchName the most recent branch name.
     */
    public void setMostRecentBranchName(String projectName, String branchName, String mostRecentBranchName) {
        setStringValue(getMostRecentBranchNameTag(projectName, branchName), mostRecentBranchName);
    }

    /**
     * Get the most recent project type.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the most recent project type.
     */
    public String getMostRecentProjectType(String projectName, String branchName) {
        return getStringValue(getMostRecentProjectTypeTag(projectName, branchName));
    }

    /**
     * Set the most recent project type.
     * @param projectName project name.
     * @param branchName branch name.
     * @param projectType the most recent project type.
     */
    public void setMostRecentProjectType(String projectName, String branchName, String projectType) {
        setStringValue(getMostRecentProjectTypeTag(projectName, branchName), projectType);
    }

    /**
     * Set the log level property for the activity pane.
     * @param projectName project name.
     * @param branchName branch name.
     * @param logLevel the log level property for the activity pane.
     */
    public void setActivityPaneLogLevel(String projectName, String branchName, String logLevel) {
        setStringValue(getActivityPaneLogLevelTag(projectName, branchName), logLevel);
    }

    /**
     * Get the use external visual compare tool flag.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the use external visual compare tool flag.
     */
    public boolean getUseExternalVisualCompareTool(String projectName, String branchName) {
        return getBooleanValue(getUseExternalVisualCompareToolFlagTag(projectName, branchName));
    }

    /**
     * Set the use external visual compare tool flag.
     * @param projectName project name.
     * @param branchName branch name.
     * @param flag the use external visual compare tool flag.
     */
    public void setUseExternalVisualCompareTool(String projectName, String branchName, boolean flag) {
        setBooleanValue(getUseExternalVisualCompareToolFlagTag(projectName, branchName), flag);
    }

    /**
     * Get the external visual compare command line.
     * @param projectName project name.
     * @param branchName branch name.
     * @return the external visual compare command line.
     */
    public String getExternalVisualCommandLine(String projectName, String branchName) {
        return getStringValue(getExternalVisualCommandLineTag(projectName, branchName));
    }

    /**
     * Set the external visual compare command line.
     * @param projectName project name.
     * @param branchName branch name.
     * @param commandLine the external visual compare command line.
     */
    public void setExternalVisualCommandLine(String projectName, String branchName, String commandLine) {
        setStringValue(getExternalVisualCommandLineTag(projectName, branchName), commandLine);
    }


////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    public static String getStaticIgnoreLeadingWhitespaceTag() {
        return IGNORE_LEADING_WHITE_SPACE_TAG;
    }

    public static String getIgnoreLeadingWhitespaceTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticIgnoreLeadingWhitespaceTag();
    }

    public static String getStaticIgnoreAllWhitespaceTag() {
        return IGNORE_ALL_WHITE_SPACE_TAG;
    }

    public static String getIgnoreAllWhitespaceTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticIgnoreAllWhitespaceTag();
    }

    public static String getStaticIgnoreCaseTag() {
        return IGNORE_CASE_TAG;
    }

    public static String getIgnoreCaseTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticIgnoreCaseTag();
    }

    public static String getStaticIgnoreEOLChangesTag() {
        return IGNORE_EOL_CHANGES_TAG;
    }

    public static String getIgnoreEOLChangesTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticIgnoreEOLChangesTag();
    }

    public static String getStaticMRUFile1NameTag() {
        return MRU_FILE1_NAME_TAG;
    }

    public static String getMRUFile1NameTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticMRUFile1NameTag();
    }

    public static String getStaticMRUFile2NameTag() {
        return MRU_FILE2_NAME_TAG;
    }

    public static String getMRUFile2NameTag(String projectName, String branchName) {
        return projectName + ":" + branchName + ":" + ":" + getStaticMRUFile2NameTag();
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    public void setIgnoreLeadingWhitespace(String projectName, String branchName, boolean flag) {
        setBooleanValue(getIgnoreLeadingWhitespaceTag(projectName, branchName), flag);
    }

    public boolean getIgnoreLeadingWhitespace(String projectName, String branchName) {
        return getBooleanValue(getIgnoreLeadingWhitespaceTag(projectName, branchName));
    }

    public void setIgnoreAllWhitespace(String projectName, String branchName, boolean flag) {
        setBooleanValue(getIgnoreAllWhitespaceTag(projectName, branchName), flag);
    }

    public boolean getIgnoreAllWhitespace(String projectName, String branchName) {
        return getBooleanValue(getIgnoreAllWhitespaceTag(projectName, branchName));
    }

    public void setIgnoreCase(String projectName, String branchName, boolean flag) {
        setBooleanValue(getIgnoreCaseTag(projectName, branchName), flag);
    }

    public boolean getIgnoreCase(String projectName, String branchName) {
        return getBooleanValue(getIgnoreCaseTag(projectName, branchName));
    }

    public void setIgnoreEOLChanges(String projectName, String branchName, boolean flag) {
        setBooleanValue(getIgnoreEOLChangesTag(projectName, branchName), flag);
    }

    public boolean getIgnoreEOLChanges(String projectName, String branchName) {
        return getBooleanValue(getIgnoreEOLChangesTag(projectName, branchName));
    }

    public void setMRUFile1Name(String projectName, String branchName, String mruFilename) {
        setStringValue(getMRUFile1NameTag(projectName, branchName), mruFilename);
    }

    public String getMRUFile1Name(String projectName, String branchName) {
        return getStringValue(getMRUFile1NameTag(projectName, branchName));
    }

    public void setMRUFile2Name(String projectName, String branchName, String mruFilename) {
        setStringValue(getMRUFile2NameTag(projectName, branchName), mruFilename);
    }

    public String getMRUFile2Name(String projectName, String branchName) {
        return getStringValue(getMRUFile2NameTag(projectName, branchName));
    }

    public void setActiveFileFilterName(String projectName, String branchName, String fileFilterName) {
        setStringValue(getActiveFileFilterNameTag(projectName, branchName), fileFilterName);
    }

    public String getActiveFileFilterName(String projectName, String branchName) {
        return getStringValue(getActiveFileFilterNameTag(projectName, branchName));
    }

    public void setFontSize(String projectName, String branchName, int fontSize) {
        setIntegerValue(getFontSizeTag(projectName, branchName), fontSize);
    }

    public int getFontSize(String projectName, String branchName) {
        return getIntegerValue(getFontSizeTag(projectName, branchName), DEFAULT_FONT_SIZE);
    }

    public boolean getAutoUpdateFlag(String projectName, String branchName) {
        return getBooleanValue(getAutoUpdateFlagTag(projectName, branchName));
    }

    public void setAutoUpdateFlag(String projectName, String branchName, boolean flag) {
        setBooleanValue(getAutoUpdateFlagTag(projectName, branchName), flag);
    }

    public int getAutoUpdateInterval(String projectName, String branchName) {
        return getIntegerValue(getAutoUpdateIntervalTag(projectName, branchName), DEFAULT_UPDATE_INTERVAL);
    }

    public void setAutoUpdateInterval(String projectName, String branchName, int autoUpdateInterval) {
        setIntegerValue(getAutoUpdateIntervalTag(projectName, branchName), autoUpdateInterval);
    }

    public int getColumnWidth(String projectName, String branchName, int columnNumber) {
        return getIntegerValue(getColumnWidthTag(projectName, branchName, columnNumber), DEFAULT_COLUMN_WIDTH);
    }

    public void setColumnWidth(String projectName, String branchName, int columnNumber, int columnWidth) {
        setIntegerValue(getColumnWidthTag(projectName, branchName, columnNumber), columnWidth);
    }

    public String getCurrentSortColumn(String projectName, String branchName) {
        return getStringValue(getCurrentSortColumnTag(projectName, branchName));
    }

    public void setCurrentSortColumn(String projectName, String branchName, String sortColumnName) {
        setStringValue(getCurrentSortColumnTag(projectName, branchName), sortColumnName);
    }

    public String getLookAndFeel(String projectName, String branchName) {
        return getStringValue(getLookAndFeelTag(projectName, branchName));
    }

    public void setLookAndFeel(String projectName, String branchName, String lookAndFeel) {
        setStringValue(getLookAndFeelTag(projectName, branchName), lookAndFeel);
    }

    public boolean getUseColoredFileIconsFlag(String projectName, String branchName) {
        return getBooleanValue(getUseColoredFileIconsTag(projectName, branchName));
    }

    public void setUseColoredFileIconsFlag(String projectName, String branchName, boolean flag) {
        setBooleanValue(getUseColoredFileIconsTag(projectName, branchName), flag);
    }

    public boolean getUseLargeToolbarButtons(String projectName, String branchName) {
        return getBooleanValue(getUseLargeToolbarButtonsTag(projectName, branchName));
    }

    public void setUseLargeToolbarButtons(String projectName, String branchName, boolean flag) {
        setBooleanValue(getUseLargeToolbarButtonsTag(projectName, branchName), flag);
    }

    public int getFileListHeight(String projectName, String branchName) {
        return getIntegerValue(getFileListHeightTag(projectName, branchName), DEFAULT_FILE_LIST_HEIGHT);
    }

    public void setFileListHeight(String projectName, String branchName, int fileListHeight) {
        setIntegerValue(getFileListHeightTag(projectName, branchName), fileListHeight);
    }

    public int getFrameHeight(String projectName, String branchName) {
        return getIntegerValue(getFrameHeightTag(projectName, branchName), DEFAULT_FRAME_HEIGHT);
    }

    public void setFrameHeight(String projectName, String branchName, int frameHeight) {
        setIntegerValue(getFrameHeightTag(projectName, branchName), frameHeight);
    }

    public int getFrameWidth(String projectName, String branchName) {
        return getIntegerValue(getFrameWidthTag(projectName, branchName), DEFAULT_FRAME_WIDTH);
    }

    public void setFrameWidth(String projectName, String branchName, int frameWidth) {
        setIntegerValue(getFrameWidthTag(projectName, branchName), frameWidth);
    }

    public int getFrameXLocation(String projectName, String branchName) {
        return getIntegerValue(getFrameXLocationTag(projectName, branchName), DEFAULT_FRAME_X_LOCATION);
    }

    public void setFrameXLocation(String projectName, String branchName, int frameXLocation) {
        setIntegerValue(getFrameXLocationTag(projectName, branchName), frameXLocation);
    }

    public int getFrameYLocation(String projectName, String branchName) {
        return getIntegerValue(getFrameYLocationTag(projectName, branchName), DEFAULT_FRAME_Y_LOCATION);
    }

    public void setFrameYLocation(String projectName, String branchName, int frameYLocation) {
        setIntegerValue(getFrameYLocationTag(projectName, branchName), frameYLocation);
    }

    public boolean getFrameMaximizeFlag(String projectName, String branchName) {
        return getBooleanValue(getFrameMaximizeFlagTag(projectName, branchName));
    }

    public void setFrameMaximizeFlag(String projectName, String branchName, boolean flag) {
        setBooleanValue(getFrameMaximizeFlagTag(projectName, branchName), flag);
    }

    public int getTreeWidth(String projectName, String branchName) {
        return getIntegerValue(getTreeWidthTag(projectName, branchName), DEFAULT_TREE_WIDTH);
    }

    public void setTreeWidth(String projectName, String branchName, int treeWidth) {
        setIntegerValue(getTreeWidthTag(projectName, branchName), treeWidth);
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    /**
     * @return the propertyKey
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    public Map<String, UserPropertyData> getUserPropertyMap() {
        return userPropertyMap;
    }

    /**
     * Set the workfile location for a given server, project, and branch.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param location the workfile location for the above.
     */
    public void setWorkfileLocation(String serverName, String projectName, String branchName, String location) {
        String workfileLocationTag;

        if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            workfileLocationTag = serverName + "_" + projectName + "_" + WORKFILE_LOCATION_TAG;
        } else {
            workfileLocationTag = serverName + "_" + projectName + "_" + branchName + "_" + WORKFILE_LOCATION_TAG;
        }
        setStringValue(workfileLocationTag, location);
    }

    /**
     * Get the workfile location for a given server, project, and branch.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @return the workfile location for the above.
     */
    public String getWorkfileLocation(String serverName, String projectName, String branchName) {
        String workfileLocationTag;

        if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            workfileLocationTag = serverName + "_" + projectName + "_" + WORKFILE_LOCATION_TAG;
        } else {
            workfileLocationTag = serverName + "_" + projectName + "_" + branchName + "_" + WORKFILE_LOCATION_TAG;
        }

        return getStringValue(workfileLocationTag);
    }

    public void setStringValue(String tag, String value) {
        UserPropertyData userPropertyData = userPropertyMap.get(tag);
        if (userPropertyData != null) {
            userPropertyData.setPropertyValue(value);
        } else {
            userPropertyData = new UserPropertyData();
            userPropertyData.setPropertyName(tag);
            userPropertyData.setPropertyValue(value);
            userPropertyMap.put(tag, userPropertyData);
        }
        setUserProperty(userPropertyData);
    }

    public String getStringValue(String tag) {
        String returnedString = null;
        UserPropertyData userPropertyData = userPropertyMap.get(tag);
        if (userPropertyData != null) {
            returnedString = userPropertyData.getPropertyValue();
        }
        return returnedString;
    }

    public void setBooleanValue(String tag, boolean flag) {
        UserPropertyData userPropertyData = userPropertyMap.get(tag);
        String value = Boolean.toString(flag);
        if (userPropertyData != null) {
            userPropertyData.setPropertyValue(value);
        } else {
            userPropertyData = new UserPropertyData();
            userPropertyData.setPropertyName(tag);
            userPropertyData.setPropertyValue(value);
            userPropertyMap.put(tag, userPropertyData);
        }
        setUserProperty(userPropertyData);
    }

    public boolean getBooleanValue(String tag) {
        boolean returnFlag = false;
        UserPropertyData userPropertyData = userPropertyMap.get(tag);
        if (userPropertyData != null) {
            returnFlag = Boolean.parseBoolean(userPropertyData.getPropertyValue());
        }
        return returnFlag;
    }

    public void setIntegerValue(String tag, int valueAsInt) {
        UserPropertyData userPropertyData = userPropertyMap.get(tag);
        String valueAsString = Integer.toString(valueAsInt);
        if (userPropertyData != null) {
            userPropertyData.setPropertyValue(valueAsString);
        } else {
            userPropertyData = new UserPropertyData();
            userPropertyData.setPropertyName(tag);
            userPropertyData.setPropertyValue(valueAsString);
            userPropertyMap.put(tag, userPropertyData);
        }
        setUserProperty(userPropertyData);
    }

    public int getIntegerValue(String tag) {
        int returnInt = 0;
        String propertyValue = userPropertyMap.get(tag).getPropertyValue();
        if (propertyValue != null) {
            returnInt = Integer.decode(propertyValue);
        }
        return returnInt;
    }

    public int getIntegerValue(String tag, int defaultValue) {
        int returnInt = defaultValue;
        UserPropertyData data = userPropertyMap.get(tag);
        if (data != null) {
            String propertyValue = data.getPropertyValue();
            if (propertyValue != null) {
                returnInt = Integer.decode(propertyValue);
            }
        }
        return returnInt;
    }

    private void setUserProperty(UserPropertyData upData) {
        ClientRequestAddUserPropertyData data = new ClientRequestAddUserPropertyData();
        data.setUserPropertyData(upData);
        data.setUserName(userName);
        data.setPropertiesKey(getPropertyKey());
        String machineName = Utility.getComputerName();
        data.setClientComputerName(machineName);
        upData.setUserAndComputer(Utility.createUserAndComputerKey(userName, machineName));
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
        SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, data);
        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
    }
}
