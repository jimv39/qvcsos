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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User Properties. These values are stored in the user properties file.
 *
 * @author Jim Voris
 */
public final class UserProperties extends QumaProperties {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");
    private static final int DEFAULT_FONT_SIZE = 11;
    private static final String AUTO_UPDATE_INTERVAL_TAG = "QVCS_AUTOUPDATEINTERVAL";
    private static final String AUTO_UPDATE_FLAG_TAG = "QVCS_AUTOUPDATEFLAG";
    private static final String BYPASS_LOGIN_DIALOG_FLAG_TAG = "QVCS_BypassLoginFlag";
    private static final String BYPASS_PASSWORD_TAG = "QVCS_BypassPassword";
    private static final String BYPASS_SERVERNAME_TAG = "QVCS_BypassServerName";
    private static final String BYPASS_USERNAME_TAG = "QVCS_BypassUserName";
    private static final String IGNORE_HIDDEN_DIRECTORIES_TAG = "QVCS_IgnoreHiddenDirectories";
    private static final String MOST_RECENT_APPENDED_PATH_TAG = "QVCS_MostRecentAppendedPath";
    private static final String MOST_RECENT_PROJECT_NAME_TAG = "QVCS_MostRecentProjectName";
    private static final String MOST_RECENT_VIEW_NAME_TAG = "QVCS_MostRecentViewName";
    private static final String MOST_RECENT_PROJECT_TYPE_TAG = "QVCS_MostRecentProjectType";
    private static final String FRAME_WIDTH_TAG = "QVCS_FrameWidth";
    private static final String FRAME_HEIGHT_TAG = "QVCS_FrameHeight";
    private static final String FRAME_X_LOCATION_TAG = "QVCS_FrameXLocation";
    private static final String FRAME_Y_LOCATION_TAG = "QVCS_FrameYLocation";
    private static final String COLUMN_WIDTH_TAG = "QVCS_ColumnWidth";
    private static final String LARGE_TOOLBAR_BUTTON_TAG = "QVCS_UseLargeToolbarButtons";
    private static final String USE_COLORED_FILE_ICONS_TAG = "QVCS_UseColoredFileIcons";
    private static final String TREE_WIDTH_TAG = "QVCS_TreeWidth";
    private static final String FILE_LIST_HEIGHT_TAG = "QVCS_FileListHeight";
    private static final String FRAME_MAXIMIZE_FLAG_TAG = "QVCS_MaximizeFlag";
    private static final String CURRENT_SORT_COLUMN_TAG = "QVCS_CurrentSortColumn";
    private static final String LOOK_AND_FEEL_TAG = "QVCS_LookAndFeel";
    private static final String ACTIVITY_PANE_LOG_LEVEL_TAG = "QVCS_ActivityPaneLogLevel";
    private static final String ACTIVE_FILE_FILTER_NAME_TAG = "QVCS_ActiveFileFilterNameTag";
    private static final String FONT_SIZE_TAG = "QVCS_FontSize";
    private static final String USE_EXTERNAL_VISUAL_COMPARE_TOOL_FLAG_TAG = "QVCS_UseExternalVisualCompareToolFlagTag";
    private static final String EXTERNAL_VISUAL_COMPARE_COMMAND_LINE_TAG = "QVCS_ExternalVisualCompareCommandLineTag";

    /**
     * Creates new UserProperties.
     *
     * @param homeDirectory the base directory that defines where to find the property file.
     */
    public UserProperties(String homeDirectory) {
        setPropertyFileName(homeDirectory
                + File.separator
                + QVCSConstants.QVCS_USER_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_USERNAME_PROPERTIES_PREFIX
                + System.getProperty("user.name")
                + ".properties");
        loadProperties(getPropertyFileName());
    }

    protected void loadProperties(String propertyFileName) {
        FileInputStream inStream = null;
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        defaultProperties.put(getMostRecentProjectTypeTag(), QVCSConstants.QVCS_REMOTE_PROJECT_TYPE);
        defaultProperties.put(getFrameXLocationTag(), QVCSConstants.QVCS_DEFAULT_X_LOCATION);
        defaultProperties.put(getFrameYLocationTag(), QVCSConstants.QVCS_DEFAULT_Y_LOCATION);
        defaultProperties.put(getFrameWidthTag(), QVCSConstants.QVCS_DEFAULT_X_SIZE);
        defaultProperties.put(getFrameHeightTag(), QVCSConstants.QVCS_DEFAULT_Y_SIZE);

        // Create the actual properties
        setActualProperties(new java.util.Properties(defaultProperties));
        try {
            inStream = new FileInputStream(new File(getPropertyFileName()));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            // Catch any exception.  If the property file is missing, we'll just go
            // with the defaults.
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Exception in closing user properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString() + ": "
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Save the properties to the property file.
     */
    public void saveProperties() {
        FileOutputStream outStream = null;
        if (getActualProperties() != null) {
            try {
                File propertyFile = new File(getPropertyFileName());
                propertyFile.getParentFile().mkdirs();
                outStream = new FileOutputStream(propertyFile);
                getActualProperties().store(outStream, "QVCS User Properties for user: " + System.getProperty("user.name"));
            } catch (IOException e) {
                // Catch any exception.  If the property file is missing, we'll just go
                // with the defaults.
                LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Exception in closing user properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString() + ": "
                                + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
    /**
     * Get the bypass login flag tag.
     * @return the String constant we use in the property file to identify the bypass login dialog setting.
     */
    public static String getBypassLoginDialogFlagTag() {
        return BYPASS_LOGIN_DIALOG_FLAG_TAG;
    }

    /**
     * Get the bypass server name tag.
     * @return the String constant we use in the property file to identify the bypass server name setting.
     */
    public static String getBypassServerNameTag() {
        return BYPASS_SERVERNAME_TAG;
    }

    /**
     * Get the bypass password tag.
     * @return the String constant we use in the property file to identify the bypass password string.
     */
    public static String getBypassPasswordTag() {
        return BYPASS_PASSWORD_TAG;
    }

    /**
     * Get the bypass user name tag.
     * @return the String constant we use in the property file to identify the bypass username string.
     */
    public static String getBypassUserNameTag() {
        return BYPASS_USERNAME_TAG;
    }

    /**
     * Get the ignore hidden directories tag.
     * @return the String constant we use in the property file to identify the ignore hidden directories setting.
     */
    public static String getIgnoreHiddenDirectoriesTag() {
        return IGNORE_HIDDEN_DIRECTORIES_TAG;
    }

    /**
     * Get the most recent appended path tag.
     * @return the String constant we use in the property file to identify the most recent appendedPath property.
     */
    public static String getMostRecentAppendedPathTag() {
        return MOST_RECENT_APPENDED_PATH_TAG;
    }

    /**
     * Get the most recent project name tag.
     * @return the String constant we use in the property file to identify the most recent project name property.
     */
    public static String getMostRecentProjectNameTag() {
        return MOST_RECENT_PROJECT_NAME_TAG;
    }

    /**
     * Get the most recent view name tag.
     * @return the String constant we use in the property file to identify the most recent view name property.
     */
    public static String getMostRecentViewNameTag() {
        return MOST_RECENT_VIEW_NAME_TAG;
    }

    /**
     * Get the most recent project type tag.
     * @return the String constant we use in the property file to identify the most recent project type property.
     */
    public static String getMostRecentProjectTypeTag() {
        return MOST_RECENT_PROJECT_TYPE_TAG;
    }

    /**
     * Get the current sort column tag.
     * @return the String constant we use in the property file to identify the most recent sort column property.
     */
    public static String getCurrentSortColumnTag() {
        return CURRENT_SORT_COLUMN_TAG;
    }

    /**
     * Get the frame width tag.
     * @return the String constant we use in the property file to identify the frame width property.
     */
    public static String getFrameWidthTag() {
        return FRAME_WIDTH_TAG;
    }

    /**
     * Get the frame height tag.
     * @return the String constant we use in the property file to identify the frame height property.
     */
    public static String getFrameHeightTag() {
        return FRAME_HEIGHT_TAG;
    }

    /**
     * Get the frame X location tag.
     * @return the String constant we use in the property file to identify the frame's X location property.
     */
    public static String getFrameXLocationTag() {
        return FRAME_X_LOCATION_TAG;
    }

    /**
     * Get the frame Y location tag.
     * @return the String constant we use in the property file to identify the frame's Y location property.
     */
    public static String getFrameYLocationTag() {
        return FRAME_Y_LOCATION_TAG;
    }

    /**
     * Get the column width tag for the given column.
     * @param columnNumber the column for which we want the column width'd tag.
     * @return the String constant we use in the property file to identify the column width property for the given column.
     */
    public static String getColumnWidthTag(int columnNumber) {
        return COLUMN_WIDTH_TAG + Integer.toString(columnNumber);
    }

    /**
     * Get the frame maximize flag tag.
     * @return the String constant we use in the property file to identify the frame's maximize property.
     */
    public static String getFrameMaximizeFlagTag() {
        return FRAME_MAXIMIZE_FLAG_TAG;
    }

    /**
     * Get the look-and-feel tag.
     * @return the String constant we use in the property file to identify the look-and-feel property.
     */
    public static String getLookAndFeelTag() {
        return LOOK_AND_FEEL_TAG;
    }

    /**
     * Get the activity pane log level tag.
     * @return the String constant we use in the property file to identify the log level used in the activity pane property.
     */
    public static String getActivityPaneLogLevelTag() {
        return ACTIVITY_PANE_LOG_LEVEL_TAG;
    }

    /**
     * Get the use external visual compare tool flag tag.
     * @return the String constant we use in the property file to identify whether we use an external visual compare tool property.
     */
    public static String getUseExternalVisualCompareToolFlagTag() {
        return USE_EXTERNAL_VISUAL_COMPARE_TOOL_FLAG_TAG;
    }

    /**
     * Get the external visual compare command line tag.
     * @return the String constant we use in the property file to identify command line used for the external visual compare tool property.
     */
    public static String getExternalVisualCommandLineTag() {
        return EXTERNAL_VISUAL_COMPARE_COMMAND_LINE_TAG;
    }

    /**
     * Get the active file filter name tag.
     * @return the String constant we use in the property file to identify the active file filter name property.
     */
    public static String getActiveFileFilterNameTag() {
        return ACTIVE_FILE_FILTER_NAME_TAG;
    }

    /**
     * Get the use large toolbar buttons tag.
     * @return the String constant we use in the property file to identify the use large toolbar property.
     */
    public static String getUseLargeToolbarButtonsTag() {
        return LARGE_TOOLBAR_BUTTON_TAG;
    }

    /**
     * Get the tree width tag.
     * @return the String constant we use in the property file to identify the tree width property.
     */
    public static String getTreeWidthTag() {
        return TREE_WIDTH_TAG;
    }

    /**
     * Get the file list height tag.
     * @return the String constant we use in the property file to identify the file list height property.
     */
    public static String getFileListHeightTag() {
        return FILE_LIST_HEIGHT_TAG;
    }

    /**
     * Get the auto update interval tag.
     * @return the String constant we use in the property file to identify the auto update interval property.
     */
    public static String getAutoUpdateIntervalTag() {
        return AUTO_UPDATE_INTERVAL_TAG;
    }

    /**
     * Get the auto update flag tag.
     * @return the String constant we use in the property file to identify the auto update flag property.
     */
    public static String getAutoUpdateFlagTag() {
        return AUTO_UPDATE_FLAG_TAG;
    }

    /**
     * Get the use colored file icons tag.
     * @return the String constant we use in the property file to identify the use colored file icons property.
     */
    public static String getUseColoredFileIconsTag() {
        return USE_COLORED_FILE_ICONS_TAG;
    }

    /**
     * Get the font size tag.
     * @return the String constant we use in the property file to identify font size property.
     */
    public static String getFontSizeTag() {
        return FONT_SIZE_TAG;
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
    /**
     * Set the bypass login dialog flag.
     * @param flag true if we want to bypass the login dialog; false otherwise.
     */
    public void setBypassLoginDialogFlag(boolean flag) {
        setBooleanValue(getBypassLoginDialogFlagTag(), flag);
    }

    /**
     * Get the bypass login dialog flag.
     * @return true if for bypassing the login dialog; false otherwise.
     */
    public boolean getBypassLoginDialogFlag() {
        return getBooleanValue(getBypassLoginDialogFlagTag());
    }

    /**
     * Set the ignore hidden directories flag. When true, QVCS-Enterprise will ignore directories starting with the '.' character.
     * @param flag value for the hidden directories flag.
     */
    public void setIgnoreHiddenDirectoriesFlag(boolean flag) {
        setBooleanValue(getIgnoreHiddenDirectoriesTag(), flag);
    }

    /**
     * Get the ignore hidden directories flag.
     * @return the hiddend directories flag.
     */
    public boolean getIgnoreHiddenDirectoriesFlag() {
        return getBooleanValue(getIgnoreHiddenDirectoriesTag());
    }

    /**
     * Set the bypass server name value.
     * @param serverName the server name.
     */
    public void setBypassServerName(String serverName) {
        setStringValue(getBypassServerNameTag(), serverName);
    }

    /**
     * Get the bypass server name.
     * @return the bypass server name.
     */
    public String getBypassServerName() {
        return getStringValue(getBypassServerNameTag());
    }

    /**
     * Set the password. Note that this is stored in the clear!!
     * @param password the password value.
     */
    public void setBypassPassword(String password) {
        setStringValue(getBypassPasswordTag(), password);
    }

    /**
     * Get the password. Note that this is in the clear!!
     * @return the password.
     */
    public String getBypassPassword() {
        return getStringValue(getBypassPasswordTag());
    }

    /**
     * Set the bypass user name. Used if the bypass login flag is set to define the user name used for the automated login attempt.
     * @param username the bypass user name.
     */
    public void setBypassUserName(String username) {
        setStringValue(getBypassUserNameTag(), username);
    }

    /**
     * Get the bypass user name.
     * @return the bypass user name.
     */
    public String getBypassUserName() {
        return getStringValue(getBypassUserNameTag());
    }

    /**
     * Set the most recent appendedPath.
     * @param appendedPath the most recent appended path.
     */
    public void setMostRecentAppendedPath(String appendedPath) {
        setStringValue(getMostRecentAppendedPathTag(), appendedPath);
    }

    /**
     * Get the most recent appendedPath.
     * @return the most recent appendedPath.
     */
    public String getMostRecentAppendedPath() {
        return getStringValue(getMostRecentAppendedPathTag());
    }

    /**
     * Get the most recent project name.
     * @return the most recent project name.
     */
    public String getMostRecentProjectName() {
        return getStringValue(getMostRecentProjectNameTag());
    }

    /**
     * Set the most recent project name.
     * @param projectName the most recent project name.
     */
    public void setMostRecentProjectName(String projectName) {
        setStringValue(getMostRecentProjectNameTag(), projectName);
    }

    /**
     * Get the most recent view name.
     * @return the most recent view name.
     */
    public String getMostRecentViewName() {
        String mostRecentViewName = getStringValue(getMostRecentViewNameTag());
        if (mostRecentViewName == null || mostRecentViewName.length() == 0) {
            mostRecentViewName = QVCSConstants.QVCS_TRUNK_VIEW;
        }
        return mostRecentViewName;
    }

    /**
     * Set the most recent view name.
     * @param viewName the most recent view name.
     */
    public void setMostRecentViewName(final String viewName) {
        setStringValue(getMostRecentViewNameTag(), viewName);
    }

    /**
     * Get the most recent project type.
     * @return the most recent project type.
     */
    public String getMostRecentProjectType() {
        return getStringValue(getMostRecentProjectTypeTag());
    }

    /**
     * Set the most recent project type.
     * @param projectType the most recent project type.
     */
    public void setMostRecentProjectType(String projectType) {
        setStringValue(getMostRecentProjectTypeTag(), projectType);
    }

    /**
     * Get the current sort column.
     * @return the current sort column.
     */
    public String getCurrentSortColumn() {
        return getStringValue(getCurrentSortColumnTag());
    }

    /**
     * Set the current sort column.
     * @param sortColumnName the current sort column.
     */
    public void setCurrentSortColumn(String sortColumnName) {
        setStringValue(getCurrentSortColumnTag(), sortColumnName);
    }

    /**
     * Get the frame width.
     * @return the frame width.
     */
    public int getFrameWidth() {
        return getIntegerValue(getFrameWidthTag());
    }

    /**
     * Set the frame width.
     * @param frameWidth the frame width.
     */
    public void setFrameWidth(int frameWidth) {
        setIntegerValue(getFrameWidthTag(), frameWidth);
    }

    /**
     * Get the frame height.
     * @return the frame height.
     */
    public int getFrameHeight() {
        return getIntegerValue(getFrameHeightTag());
    }

    /**
     * Set the frame height property.
     * @param frameHeight the frame height property.
     */
    public void setFrameHeight(int frameHeight) {
        setIntegerValue(getFrameHeightTag(), frameHeight);
    }

    /**
     * Get the frame X location property.
     * @return get the frame X location property.
     */
    public int getFrameXLocation() {
        return getIntegerValue(getFrameXLocationTag());
    }

    /**
     * Set the frame X location property.
     * @param frameXLocation the frame X location property.
     */
    public void setFrameXLocation(int frameXLocation) {
        setIntegerValue(getFrameXLocationTag(), frameXLocation);
    }

    /**
     * Get the frame Y location property.
     * @return the frame Y location property.
     */
    public int getFrameYLocation() {
        return getIntegerValue(getFrameYLocationTag());
    }

    /**
     * Set the frame Y location property.
     * @param frameYLocation the frame Y location property.
     */
    public void setFrameYLocation(int frameYLocation) {
        setIntegerValue(getFrameYLocationTag(), frameYLocation);
    }

    /**
     * Get the column width for the given column.
     * @param columnNumber the column whose width we want.
     * @return the width of the given column.
     */
    public int getColumnWidth(int columnNumber) {
        return getIntegerValue(getColumnWidthTag(columnNumber));
    }

    /**
     * Set the column width property for the given column.
     * @param columnNumber the column whose width property we are saving.
     * @param columnWidth the width of the given column.
     */
    public void setColumnWidth(int columnNumber, int columnWidth) {
        setIntegerValue(getColumnWidthTag(columnNumber), columnWidth);
    }

    /**
     * Set the frame maximize flag.
     * @param flag the frame maximize flag.
     */
    public void setFrameMaximizeFlag(boolean flag) {
        setBooleanValue(getFrameMaximizeFlagTag(), flag);
    }

    /**
     * Get the frame maximize flag.
     * @return the frame maximize flag.
     */
    public boolean getFrameMaximizeFlag() {
        return getBooleanValue(getFrameMaximizeFlagTag());
    }

    /**
     * Get the look-and-feel property value.
     * @return the look-and-feel property value.
     */
    public String getLookAndFeel() {
        return getStringValue(getLookAndFeelTag());
    }

    /**
     * Set the look-and-feel property value.
     * @param lookAndFeel the look-and-feel property value.
     */
    public void setLookAndFeel(String lookAndFeel) {
        setStringValue(getLookAndFeelTag(), lookAndFeel);
    }

    /**
     * Get the log level used in the activity pane.
     * @return the log level used in the activity pane.
     */
    public String getActivityPaneLogLevel() {
        String level = getStringValue(getActivityPaneLogLevelTag());
        if (level.length() == 0) {
            setActivityPaneLogLevel(Level.INFO.toString());
            level = Level.INFO.toString();
        }
        return level;
    }

    /**
     * Set the log level property for the activity pane.
     * @param logLevel the log level property for the activity pane.
     */
    public void setActivityPaneLogLevel(String logLevel) {
        setStringValue(getActivityPaneLogLevelTag(), logLevel);
    }

    /**
     * Get the use external visual compare tool flag.
     * @return the use external visual compare tool flag.
     */
    public boolean getUseExternalVisualCompareTool() {
        return getBooleanValue(getUseExternalVisualCompareToolFlagTag());
    }

    /**
     * Set the use external visual compare tool flag.
     * @param flag the use external visual compare tool flag.
     */
    public void setUseExternalVisualCompareTool(boolean flag) {
        setBooleanValue(getUseExternalVisualCompareToolFlagTag(), flag);
    }

    /**
     * Get the external visual compare command line.
     * @return the external visual compare command line.
     */
    public String getExternalVisualCommandLine() {
        return getStringValue(getExternalVisualCommandLineTag());
    }

    /**
     * Set the external visual compare command line.
     * @param commandLine the external visual compare command line.
     */
    public void setExternalVisualCommandLine(String commandLine) {
        setStringValue(getExternalVisualCommandLineTag(), commandLine);
    }

    /**
     * Get the active file filter name.
     * @return the active file filter name.
     */
    public String getActiveFileFilterName() {
        return getStringValue(getActiveFileFilterNameTag());
    }

    /**
     * Set the active file filter name.
     * @param fileFilterName the active file filter name.
     */
    public void setActiveFileFilterName(String fileFilterName) {
        setStringValue(getActiveFileFilterNameTag(), fileFilterName);
    }

    /**
     * Get the use large toolbar button flag.
     * @return the use large toolbar button flag.
     */
    public boolean getUseLargeToolbarButtons() {
        return getBooleanValue(getUseLargeToolbarButtonsTag());
    }

    /**
     * Set the use large toolbar button flag.
     * @param flag the use large toolbar button flag.
     */
    public void setUseLargeToolbarButtons(boolean flag) {
        setBooleanValue(getUseLargeToolbarButtonsTag(), flag);
    }

    /**
     * Get the use colored file icons flag.
     * @return the use colored file icons flag.
     */
    public boolean getUseColoredFileIconsFlag() {
        return getBooleanValue(getUseColoredFileIconsTag());
    }

    /**
     * Set the use colored file icons flag.
     * @param flag the use colored file icons flag.
     */
    public void setUseColoredFileIconsFlag(boolean flag) {
        setBooleanValue(getUseColoredFileIconsTag(), flag);
    }

    /**
     * Set the tree width.
     * @param treeWidth the tree width.
     */
    public void setTreeWidth(int treeWidth) {
        setIntegerValue(getTreeWidthTag(), treeWidth);
    }

    /**
     * Get the tree width.
     * @return the tree width.
     */
    public int getTreeWidth() {
        return getIntegerValue(getTreeWidthTag());
    }

    /**
     * Set the file list height.
     * @param fileListHeight the file list height.
     */
    public void setFileListHeight(int fileListHeight) {
        setIntegerValue(getFileListHeightTag(), fileListHeight);
    }

    /**
     * Get the file list height.
     * @return the file list height.
     */
    public int getFileListHeight() {
        return getIntegerValue(getFileListHeightTag());
    }

    /**
     * Set the font size.
     * @param fontSize the font size.
     */
    public void setFontSize(int fontSize) {
        setIntegerValue(getFontSizeTag(), fontSize);
    }

    /**
     * Get the font size.
     * @return the font size.
     */
    public int getFontSize() {
        return getIntegerValue(getFontSizeTag(), DEFAULT_FONT_SIZE);
    }

    /**
     * Get the auto update interval.
     * @return the auto update interval.
     */
    public int getAutoUpdateInterval() {
        return getIntegerValue(getAutoUpdateIntervalTag());
    }

    /**
     * Set the auto update interval.
     * @param autoUpdateInterval the auto update interval.
     */
    public void setAutoUpdateInterval(int autoUpdateInterval) {
        setIntegerValue(getAutoUpdateIntervalTag(), autoUpdateInterval);
    }

    /**
     * Get the auto update flag.
     * @return the auto update flag.
     */
    public boolean getAutoUpdateFlag() {
        return getBooleanValue(getAutoUpdateFlagTag());
    }

    /**
     * Set the auto update flag.
     * @param flag the flag value.
     */
    public void setAutoUpdateFlag(boolean flag) {
        setBooleanValue(getAutoUpdateFlagTag(), flag);
    }
}
