/*   Copyright 2004-2022 Jim Voris
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

    /** The version of the structure of FileHistory files. */
    public static final Integer QVCS_FILE_HISTORY_VERSION = 10;
    /** The version string for this release. */
    public static final String QVCS_RELEASE_VERSION = "4.1.2-RELEASE-RC5";
    /** The maximum branch depth that we support. */
    public static final int QVCS_MAXIMUM_BRANCH_DEPTH = 5;
    /** The version of QVCS archive files. */
    public static final int QVCS_ARCHIVE_VERSION = 10;
    /** A constant for YES. */
    public static final String QVCS_YES = "YES";
    /** A constant for NO. */
    public static final String QVCS_NO = "NO";
    /** A constant for NONE. */
    public static final String QVCS_NONE = "NONE";
    /** A constant for the remote project type. Clients see remote projects hosted on the server. */
    public static final String QVCS_REMOTE_PROJECT_TYPE = "REMOTE";
    /** A constant for the served project type. Servers see served projects. */
    public static final String QVCS_SERVED_PROJECT_TYPE = "SERVED";
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
    /** The project archives directory name. This is the directory beneath which we store the archive files. */
    public static final String QVCS_PROJECTS_DIRECTORY = "qvcsProjectsArchiveData";
    /** The directory meta-data directory name. */
    public static final String QVCS_DIRECTORY_METADATA_DIRECTORY = "qvcsDirectoryMetaDataDirectory";
    /** The cemetery directory name. */
    public static final String QVCS_CEMETERY_DIRECTORY = "qvcsCemeteryDirectory";
    /** The branch archives directory name. */
    public static final String QVCS_BRANCH_ARCHIVES_DIRECTORY = "qvcsBranchArchivesDirectory";
    /** A filename prefix used for directory meta-data files. */
    public static final String QVCS_DIRECTORY_METADATA_FILENAME = "qvcsDirectoryMetaData_";
    /** The reference copies directory name. */
    public static final String QVCS_REFERENCECOPY_DIRECTORY = "qvcsProjectsReferenceCopies";
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
    /** The filename prefix used for checkin comments. */
    public static final String QVCS_CHECKIN_COMMENTS_PREFIX = "qvcs.checkincomments.";
    /** The filename prefix used for visual compare utility properties. */
    public static final String QVCS_COMPARE_PROPERTIES_PREFIX = "qvcs.visualCompare.";
    /** The filename prefix for server properties files. */
    public static final String QVCS_SERVERNAME_PROPERTIES_PREFIX = "qvcs.servername.";
    /** The filename prefix used for report files. */
    public static final String QVCS_REPORT_NAME_PREFIX = "qvcs.report.";
    /** The filename prefix used for cemetery archive files. */
    public static final String QVCS_CEMETERY_FILENAME_PREFIX = "QVCS_CEMETERY_ARCHIVE";
    /** The filename suffix used for cemetery archive files. */
    public static final String QVCS_CEMETERY_FILENAME_SUFFIX = ".QVCS_ARCHIVE";
    /** The filename prefix used for branch archive filenames. */
    public static final String QVCS_BRANCH_FILENAME_PREFIX = "QVCS_BRANCH_ARCHIVE";
    /** The filename suffix used for branch archive filenames. */
    public static final String QVCS_BRANCH_FILENAME_SUFFIX = ".QVCS_ARCHIVE";
    /** The root name of the role store file. */
    public static final String QVCS_ROLE_STORE_NAME = "qvcs.roleStore.";
    /** The root name of the role project branch store file. */
    public static final String QVCS_ROLE_PROJECT_BRANCH_STORE_NAME = "qvcs.roleProjectBranchStore.";
    /** The root name of the role privileges store file. */
    public static final String QVCS_ROLE_PRIVILEGES_STORE_NAME = "qvcs.rolePrivilegesStore.";
    /** The root name of the authentication store file. */
    public static final String QVCS_AUTHENTICATION_STORE_NAME = "qvcs.authenticationStore.";
    /** Directory dictionary id store name. */
    public static final String QVCS_DIRECTORYID_DICT_STORE_NAME = "qvcs.directoryIdDictionaryStore.";
    /** File id dictionary store name. */
    public static final String QVCS_FILEID_DICT_STORE_NAME = "qvcs.fileIdDictionaryStore.";
    /** Workfile digest store name. */
    public static final String QVCS_WORKFILE_DIGEST_STORE_NAME = "qvcs.workfileDigestStore.";
    /** Archive digest store name. */
    public static final String QVCS_ARCHIVE_DIGEST_STORE_NAME = "qvcs.archiveDigestStore.";
    /** Label store name. */
    public static final String QVCS_LABEL_STORE_NAME = "qvcs.labelStore.";
    /** Filter store name. */
    public static final String QVCS_FILTER_STORE_NAME = "qvcs.filterStore.";
    /** View utility store name. */
    public static final String QVCS_VIEW_UTILITY_STORE_NAME = "qvcs.viewUtilityStore.";
    /** Checkout comment store name. */
    public static final String QVCS_CHECKOUT_COMMENT_STORE_NAME = "qvcs.checkOutCommentStore.";
    /** File group store name. */
    public static final String QVCS_FILEGROUP_STORE_NAME = "qvcs.fileGroupStore.";
    /** Directory id store name. */
    public static final String QVCS_DIRECTORYID_STORE_NAME = "qvcs.directoryIDStore";
    /** File id store name. */
    public static final String QVCS_FILEID_STORE_NAME = "qvcs.fileIDStore";
    /** Branch store name. */
    public static final String QVCS_BRANCH_STORE_NAME = "qvcs.branchStore.";
    /** Archive temp file suffix. */
    public static final String QVCS_ARCHIVE_TEMPFILE_SUFFIX = ".temp";
    /** Archive old file suffix. */
    public static final String QVCS_ARCHIVE_OLDFILE_SUFFIX = ".old";
    /** QVCS/QVCS-Pro cache name. */
    public static final String QVCS_CACHE_NAME = "Q$QCache";
    /** QVCS/QVCS-Pro journal name. */
    public static final String QVCS_JOURNAL_NAME = "qvcs.jou";
    /** Directory id filename. */
    public static final String QVCS_DIRECTORYID_FILENAME = "qvcs.DirectoryID.dat";
    /** Mac OS .DS_Store name. */
    public static final String QVCS_MAC_DS_STORE_FILENAME = ".DS_Store";
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
    /** Define the TRUNK branch type. */
    public static final int QVCS_TRUNK_BRANCH_TYPE = 1;
    /** Define the FEATURE branch type. */
    public static final int QVCS_FEATURE_BRANCH_TYPE = 2;
    /** Define the TAG-BASED branch type. */
    public static final int QVCS_TAG_BASED_BRANCH_TYPE = 3;
    /** Define the RELEASE branch type. */
    public static final int QVCS_RELEASE_BRANCH_TYPE = 4;
    /** The comment prefix used for QVCS automatically created revisions. */
    public static final String QVCS_INTERNAL_REV_COMMENT_PREFIX = "QVCS internal revision comment. DO NOT EDIT. ";
    /** The comment segment used for QVCS file moves. */
    public static final String QVCS_INTERNAL_FILE_MOVED_FROM = "File moved from: [";
    /** The comment segment used for QVCS file renames. */
    public static final String QVCS_INTERNAL_FILE_RENAMED_FROM = "File renamed from: [";
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
    /** Locked by column name. */
    public static final String QVCS_LOCKEDBY_COLUMN = "LockedBy";
    /** Last check in column name. */
    public static final String QVCS_LAST_CHECKIN_COLUMN = "LastCheckIn";
    /** Workfile in column name. */
    public static final String QVCS_WORKFILE_IN_COLUMN = "WorkfileIn";
    /** Workfile size column name. */
    public static final String QVCS_WORKFILE_SIZE_COLUMN = "WorkfileSize";
    /** Last edit by column name. */
    public static final String QVCS_LAST_EDIT_BY_COLUMN = "LastEditBy";
    /** Appended path column name. */
    public static final String QVCS_APPENDED_PATH_COLUMN = "AppendedPath";

    /*
     * Define the default screen size and location.
     */
    /** Default X location. */
    public static final String QVCS_DEFAULT_X_LOCATION = "100";
    /** Default Y location. */
    public static final String QVCS_DEFAULT_Y_LOCATION = "100";
    /** Default X size. */
    public static final String QVCS_DEFAULT_X_SIZE = "400";
    /** Default Y size. */
    public static final String QVCS_DEFAULT_Y_SIZE = "400";

    /** Default revision string. */
    public static final String QVCS_DEFAULT_REVISION = "Default Revision";
    /** The string we use on the root node of the Admin user tree. */
    public static final String QVCS_DEFAULT_USER_TREE_NAME = " Users";
    private static final int MAX_PATH_BASE = 260;
    private static final int MAX_PATH_SUPPLEMENT = 256;
    /** The size of the supplemental information in the QVCS Header. */
    public static final int QVCS_SUPPLEMENTAL_SIZE = MAX_PATH_BASE + MAX_PATH_SUPPLEMENT;
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
