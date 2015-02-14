//   Copyright 2004-2015 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServedProjectProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.dataaccess.impl.FileDAOImpl;
import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO -- this is not finished, or useful at all at the moment.
 * The class holds the code we use to verify that the trunk archives match the database. (Recall that trunk archives are the only
 * archive files there are).
 *
 * <p>For projects, the verification is absolutely strict: if the project exists in the served projects list, then it must exist on
 * disk, and it must exist in the database.</p>
 *
 * <p>For directories, the verification is fairly strict: the given directory id must match what we find in the archive directory.
 * If the directory has moved (i.e. it has a different appended path), then we'll update the database to capture the new appended
 * path. We do not allow additions of directories, or deletions of directories: If any are found, we'll throw a QVCSException.</p>
 *
 * <p>For files within a given directory, we allow renames, moves, additions, and deletions of files.</p>
 *
 * @author Jim Voris
 */
public final class DatabaseVerificationManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseVerificationManager.class);
    private static final DatabaseVerificationManager DATABASE_VERIFICATION_MANAGER = new DatabaseVerificationManager();

    /**
     * Creates a new instance of DatabaseVerificationManager.
     */
    private DatabaseVerificationManager() {
    }

    /**
     * Get the singleton instance of the Database verification manager.
     *
     * @return the singleton instance of the Database verification manager.
     */
    public static DatabaseVerificationManager getInstance() {
        return DATABASE_VERIFICATION_MANAGER;
    }

    /**
     * Iterate over the Trunk to verify that it matches what's in the database. If there are differences, the file systems wins, and
     * we update the database to match the file system.
     */
    void verifyTrunkToDatabase(ServedProjectProperties[] projectPropertiesList) throws QVCSException {
        LOGGER.info("QVCSEnterpriseServer: Verifying directory structure against database...");

//        try
//        {
//            // Verify that the projects exist in the database.
//            verifyProjectsExistInDatabase(projectPropertiesList);
//
//            // Iterate over the list of projects...
//            for (int i = 0; i < projectPropertiesList.length; i++)
//            {
//                // Wrap this work in a server transaction so the DirectoryContents
//                // stuff will behave in a useful way...
//                ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
//
//                // Keep track that we're in a transaction.
//                ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
//
//                String archiveLocation = projectPropertiesList[i].getArchiveLocation();
//                File projectBaseDirectory = new File(archiveLocation);
//
//                // Verify the directory structure against the database.
//                verifyDirectoryStructureToDatabase(projectBaseDirectory, projectPropertiesList[i], bogusResponseObject);
//
//                // And verify the directory contents objects for this project tree.
//                verifyFilesToDatabase(projectBaseDirectory, projectPropertiesList[i], bogusResponseObject);
//
//                // Keep track that we ended this transaction.
//                ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
//            }
//        }
//        catch (QVCSException e)
//        {
//            m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
//            throw e;
//        }
    }

    /**
     * Verify the directory structure for given project.
     *
     * @param directory the root directory of the project.
     * @param servedProjectProperties the project properties.
     * @param bogusResponseObject a bogus response object.
     * @throws QVCSException if something goes wrong.
     */
    private void verifyDirectoryStructureToDatabase(File directory, ServedProjectProperties servedProjectProperties, ServerResponseFactoryInterface bogusResponseObject)
            throws QVCSException {
        // TODO
    }

    /**
     * Verify a given directory against the database.
     */
    private void verifyFilesToDatabase(File directory, ServedProjectProperties servedProjectProperties, ServerResponseFactoryInterface bogusResponseObject) throws QVCSException {
        LOGGER.info("Verifying database for directory: [{}]", directory.getAbsolutePath());
        String projectName = servedProjectProperties.getProjectName();
        String viewName = QVCSConstants.QVCS_TRUNK_VIEW;
        String appendedPath = ServerUtility.deduceAppendedPath(directory, servedProjectProperties);

        File[] fileList = directory.listFiles();

        if (fileList != null) {
            try {
                // Create the archiveDirManager for this directory...
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, appendedPath);
                ArchiveDirManager archiveDirManager = (ArchiveDirManager) ArchiveDirManagerFactoryForServer.getInstance()
                        .getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, directoryCoordinate,
                        QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject, false);
                verifyDatabaseFilesForArchiveDirManager(archiveDirManager);
            } catch (QVCSException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            } catch (SQLException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                throw new QVCSException("Could not verify database against archive directory tree.");
            }
            for (File fileList1 : fileList) {
                if (fileList1.isDirectory()) {
                    // Recurse through the directory tree...
                    verifyFilesToDatabase(fileList1, servedProjectProperties, bogusResponseObject);
                }
            }
        }
    }

    /**
     * Verify that the records in the database match what's on disk for a given archive directory. This method will update the
     * database as needed so that it matches what's on disk.
     *
     * @param archiveDirManager the directory manager for the directory that we're verifying.
     * @throws SQLException if there is a SQL problem.
     */
    private void verifyDatabaseFilesForArchiveDirManager(ArchiveDirManager archiveDirManager) throws SQLException {
        // Wrap this in a transaction.
        DatabaseManager.getInstance().getConnection().setAutoCommit(false);
        FileDAO fileDAO = new FileDAOImpl();

        try {
            // TODO.
            // Verify all the files in this directory.
            Collection<ArchiveInfoInterface> archiveInfoCollection = archiveDirManager.getArchiveInfoCollection().values();
//            for (ArchiveInfoInterface archiveInfo : archiveInfoCollection)
//            {
//                com.qumasoft.server.datamodel.File file = new com.qumasoft.server.datamodel.File();
//                file.setFileId(archiveInfo.getFileID());
//                file.setBranchId(1);    // We're guaranteed to be on the trunk here.
//                file.setDeletedFlag(false);
//                file.setDirectoryId(archiveDirManager.getDirectoryID());
//                file.setFileName(archiveInfo.getShortWorkfileName());
//                fileDAO.insert(file);
//            }
            DatabaseManager.getInstance().getConnection().commit();
        } finally {
            DatabaseManager.getInstance().getConnection().setAutoCommit(true);
        }
    }

    /**
     * Verify that any project listed in the project properties list exists in the database.
     *
     * @param projectPropertiesList the list of served projects.
     * @throws QVCSException if we find a project that doesn't exist in the database.
     */
    private void verifyProjectsExistInDatabase(ServedProjectProperties[] projectPropertiesList) throws QVCSException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
