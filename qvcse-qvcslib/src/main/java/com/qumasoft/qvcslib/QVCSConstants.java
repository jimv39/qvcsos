/*   Copyright 2004-2023 Jim Voris
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

/**
 * Constants used throughout the application.
 *
 * @author Jim Voris
 */
public final class QVCSConstants {
    /** Hide the default constructor. */
    private QVCSConstants() { }

    /** The version string for this release. */
    public static final String QVCS_RELEASE_VERSION = "4.1.5-SNAPSHOT";
    /** A constant for YES. */
    public static final String QVCS_YES = "YES";
    /** A constant for NO. */
    public static final String QVCS_NO = "NO";
    /** A constant for NONE. */
    public static final String QVCS_NONE = "NONE";
//    /** A constant for the remote project type. Clients see remote projects hosted on the server. */
//    public static final String QVCS_REMOTE_PROJECT_TYPE = "REMOTE";
//    /** A constant for the served project type. Servers see served projects. */
//    public static final String QVCS_SERVED_PROJECT_TYPE = "SERVED";
    /** A place holder user name for a server user. */
    public static final String QVCS_SERVER_USER = "SERVER USER";
    /** The name of the server. */
    public static final String QVCS_SERVER_SERVER_NAME = "QVCS-Enterprise Server";
    /** The ADMIN user name. */
    public static final String QVCS_ADMIN_USER = "ADMIN";
    /** The behavior properties directory name. */
    public static final String QVCS_BEHAVIOR_PROPERTIES_DIRECTORY = "qvcsBehaviorProperties";
    /** The project properties directory name. */
    public static final String QVCS_PROPERTIES_DIRECTORY = "qvcsProjectProperties";
    /** The server properties directory name. */
    public static final String QVCS_SERVERS_DIRECTORY = "qvcsServerProperties";
    /** The administrative data directory name. */
    public static final String QVCS_ADMIN_DATA_DIRECTORY = "qvcsAdminData";
    /** The meta-data directory name. */
    public static final String QVCS_META_DATA_DIRECTORY = "qvcsMetaData";
    /** A filename prefix used for directory meta-data files. */
    public static final String QVCS_DIRECTORY_METADATA_FILENAME = "qvcsDirectoryMetaData_";
    /** The user data directory name. */
    public static final String QVCS_USER_DATA_DIRECTORY = "qvcsUserData";
    /** The report directory name. */
    public static final String QVCS_REPORTS_DIRECTORY = "qvcsReports";
    /** The activity journal directory name. */
    public static final String QVCS_ACTIVITY_JOURNAL_DIRECTORY = "qvcsActivityJournal";
    /** The root directory for the web site. */
    public static final String QVCS_WEB_SERVER_ROOT_DIRECTORY = "ServerWebSite";
    /** The web server log name. */
    public static final String QVCS_WEB_SERVER_LOGFILE = "WebServer.log";
    /** The filename prefix used for remote project properties files. */
    public static final String QVCS_REMOTE_PROJECTNAME_PREFIX = "qvcs.remote.project.";
    /** The filename prefix used for served project properties files. */
    public static final String QVCS_SERVED_PROJECTNAME_PREFIX = "qvcs.served.project.";
    /** The filename prefix used for user properties files. */
    public static final String QVCS_USERNAME_PROPERTIES_PREFIX = "qvcs.username.";
    /** The filename prefix used for user location properties files. */
    public static final String QVCS_USERLOCATION_PROPERTIES_PREFIX = "qvcs.userlocations.";
    /** The filename prefix used for visual compare utility properties. */
    public static final String QVCS_COMPARE_PROPERTIES_PREFIX = "qvcs.visualCompare.";
    /** The filename prefix for server properties files. */
    public static final String QVCS_SERVERNAME_PROPERTIES_PREFIX = "qvcs.servername.";
    /** The filename prefix used for report files. */
    public static final String QVCS_REPORT_NAME_PREFIX = "qvcs.report.";
    /** Workfile digest store name. */
    public static final String QVCS_WORKFILE_DIGEST_STORE_NAME = "qvcs.workfileDigestStore.";
    /** Filter store name. */
    public static final String QVCS_FILTER_STORE_NAME = "qvcs.filterStore.";
    /** View utility store name. */
    public static final String QVCS_VIEW_UTILITY_STORE_NAME = "qvcs.viewUtilityStore.";
    /** File group store name. */
    public static final String QVCS_FILEGROUP_STORE_NAME = "qvcs.fileGroupStore.";
    /** Activity journal name. */
    public static final String QVCS_ACTIVITY_JOURNAL_NAME = "qvcs.server.journal";
    /** Default project name. */
    public static final String QWIN_DEFAULT_PROJECT_NAME = "All Projects";
    /** Default project properties name. */
    public static final String QWIN_DEFAULT_PROJECT_PROPERTIES_NAME = "Default_QVCS_Project";
    /** Default server name. */
    public static final String QVCS_DEFAULT_SERVER_NAME = "localhost";
    /** Name of servers. */
    public static final String QVCS_SERVERS_NAME = "Servers";
    /** The name of the Trunk branch. */
    public static final String QVCS_TRUNK_BRANCH = "Trunk";
    /** The fake appended path for the cemetery. */
    public static final String QVCSOS_CEMETERY_FAKE_APPENDED_PATH = "~~~~ Fake Cemetery Appended Path ~~~~";
    /** Define the TRUNK branch type. */
    public static final int QVCS_TRUNK_BRANCH_TYPE = 1;
    /** Define the FEATURE branch type. */
    public static final int QVCS_FEATURE_BRANCH_TYPE = 2;
    /** Define the TAG-BASED branch type. */
    public static final int QVCS_TAG_BASED_BRANCH_TYPE = 3;
    /** Define the RELEASE branch type. */
    public static final int QVCS_RELEASE_BRANCH_TYPE = 4;
    /** The digest algorithm used to capture the signature of a file revision, and as password hash. */
    public static final String QVCSOS_DIGEST_ALGORITHM = "SHA-512/256";

    /*
     * Constants for file filters.
     */
    /** Include extension filter. */
    public static final String EXTENSION_FILTER = "Include Extension";
    /** Exclude extension filter. */
    public static final String EXCLUDE_EXTENSION_FILTER = "Exclude Extension";
    /** Include regular expression filter. */
    public static final String REG_EXP_FILENAME_FILTER = "Include Regular Expression Filename";
    /** Exclude regular expression filter. */
    public static final String EXCLUDE_REG_EXP_FILENAME_FILTER = "Exclude Regular Expression Filename";
    /** Revision description regular expression filter. */
    public static final String REG_EXP_REV_DESC_FILTER = "Revision description regular expression";
    /** Exclude revision description regular expression filter. */
    public static final String EXCLUDE_REG_EXP_REV_DESC_FILTER = "Exclude revision description regular expression";
    /** Include file status filter. */
    public static final String STATUS_FILTER = "Include File Status";
    /** Exclude file status filter. */
    public static final String EXCLUDE_STATUS_FILTER = "Exclude File Status";
    /** Checked in after commit id filter. */
    public static final String CHECKED_IN_AFTER_COMMIT_ID_FILTER = "Checked in after commit id";
    /** Checked in before commit id filter. */
    public static final String CHECKED_IN_BEFORE_COMMIT_ID_FILTER = "Checked in before commit id";
    /** File size greater than filter. */
    public static final String FILESIZE_GREATER_THAN_FILTER = "Filesize greater than";
    /** File size less than filter. */
    public static final String FILESIZE_LESS_THAN_FILTER = "Filesize less than";
    /** Last edit by filter. */
    public static final String LAST_EDIT_BY_FILTER = "Include last edit by";
    /** Exclude last edit by filter. */
    public static final String EXCLUDE_LAST_EDIT_BY_FILTER = "Exclude last edit by";
    /** Exclude uncontrolled file filter. */
    public static final String EXCLUDE_UNCONTROLLED_FILE_FILTER = "Exclude uncontrolled files";
    /** Exclude obsolete filter. */
    public static final String EXCLUDE_OBSOLETE_FILTER = "Exclude obsolete files";
    /** Obsolete file filter. */
    public static final String OBSOLETE_FILTER = "Obsolete files";
    /** Search Commit messages filter. */
    public static final String SEARCH_COMMIT_MESSAGES_FILTER = "Search Commit Messages";
    /** By Commit id filter. */
    public static final String BY_COMMIT_ID_FILTER = "By Commit id";

    /*
     * Constants for revision filters
     */
    /** Edit by filter. */
    public static final String EDIT_BY_FILTER = "Edit by";
    /** Exclude edit by filter. */
    public static final String EXCLUDE_EDIT_BY_FILTER = "Exclude edit by";

    /*
     * Define the column names for the GUI
     */
    /** Filename column name. */
    public static final String QVCS_FILENAME_COLUMN = "Filename";
    /** Status column name. */
    public static final String QVCS_STATUS_COLUMN = "Status";
    /** Last check in column name. */
    public static final String QVCS_LAST_CHECKIN_COLUMN = "LastCheckIn";
    /** Workfile size column name. */
    public static final String QVCS_WORKFILE_SIZE_COLUMN = "WorkfileSize";
    /** Last edit by column name. */
    public static final String QVCS_LAST_EDIT_BY_COLUMN = "LastEditBy";
    /** Appended path column name. */
    public static final String QVCS_APPENDED_PATH_COLUMN = "AppendedPath";

    /** Default revision string. */
    public static final String QVCS_DEFAULT_REVISION = "Default Revision";
    /** The string we use on the root node of the Admin user tree. */
    public static final String QVCS_DEFAULT_USER_TREE_NAME = " Users";
    /** The standard QVCS path separator byte. */
    public static final byte QVCS_STANDARD_PATH_SEPARATOR = '/';
    /** The standard QVCS path separator String. */
    public static final String QVCS_STANDARD_PATH_SEPARATOR_STRING = "/";
    /** The heart beat sleep time. */
    public static final long HEART_BEAT_SLEEP_TIME = 1000L * 120L;   // 120 Seconds
    /** Number of bytes to read or write to prevent out-of-memory problems. */
    public static final int BYTES_TO_XFER = 2 * 1048576;

    /**
     * Define the type of change on a branch when creating a new file record.
     */
    /** A file move. */
    public static final int FILE_NAME_RECORD_CREATED_FOR_MOVE = 1;
    /** A file rename. */
    public static final int FILE_NAME_RECORD_CREATED_FOR_RENAME = 2;
    /** A file delete. */
    public static final int FILE_NAME_RECORD_CREATED_FOR_DELETE = 3;
    /**
     * A file move and rename.
     */
    public static final int FILE_NAME_RECORD_CREATED_FOR_MOVE_AND_RENAME = 4;
    /**
     * Define the type of change on a branch when creating a new directory location record.
     */
    /** A file move. */
    public static final int DIRECTORY_LOCATION_RECORD_CREATED_FOR_MOVE = 1;
    /** A file rename. */
    public static final int DIRECTORY_LOCATION_RECORD_CREATED_FOR_RENAME = 2;
    /** A file delete. */
    public static final int DIRECTORY_LOCATION_RECORD_CREATED_FOR_DELETE = 3;
}
