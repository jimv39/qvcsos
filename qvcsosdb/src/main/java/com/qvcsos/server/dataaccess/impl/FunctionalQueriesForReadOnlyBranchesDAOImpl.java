/*
 * Copyright 2021-2022 Jim Voris.
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
package com.qvcsos.server.dataaccess.impl;

import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesForReadOnlyBranchesDAO;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.FileName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class FunctionalQueriesForReadOnlyBranchesDAOImpl implements FunctionalQueriesForReadOnlyBranchesDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalQueriesForReadOnlyBranchesDAOImpl.class);

    private final String schemaName;

    // <editor-fold>
    // Result set field indexes for skinny info queries.
    private static final int USER_NAME_SET_INDEX = 1;
    private static final int COMMIT_DATE_RESULT_SET_INDEX = 2;
    private static final int FILE_NAME_RESULT_SET_INDEX = 3;
    private static final int FILE_REVISION_ID_RESULT_SET_INDEX = 4;
    private static final int FILE_ID_RESULT_SET_INDEX = 5;
    private static final int REVISION_DIGEST_RESULT_SET_INDEX = 6;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 7;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 8;
    // </editor-fold>

    // <editor-fold>
    // Result set field indexes for directory location queries.
    private static final int DL_ID_RESULT_SET_INDEX = 1;
    private static final int DL_DIRECTORY_ID_RESULT_SET_INDEX = 2;
    private static final int DL_BRANCH_ID_RESULT_SET_INDEX = 3;
    private static final int DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX = 4;
    private static final int DL_COMMIT_ID_RESULT_SET_INDEX = 5;
    private static final int DL_DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX = 6;
    private static final int DL_DELETED_FLAG_RESULT_SET_INDEX = 7;
    // </editor-fold>

    public FunctionalQueriesForReadOnlyBranchesDAOImpl(String schema) {
        this.schemaName = schema;
    }

    /**
     * Get the skinnyInfo for a read-only branch.This gets tricky (I think). We
     * need to perform 4 separate queries. The first just looks in the regular
     * directory_location and file_name tables; the 2nd query looks in the
     * directory_location_history and file_name tables; the 3rd query looks in
     * the directory_location and file_name_history tables; and the 4th looks in
     * the directory_location_history and the file_name_history tables.
     *
     * @param branch the read-only branch that we're working on.
     * @param boundingCommitId the boundary commit id.
     * @param ids the directory coordinate ids.
     * @return the list of skinnyLogfileInfo's for the requested directory.
     */
    @Override
    public List<SkinnyLogfileInfo> getSkinnyLogfileInfoForReadOnlyBranch(Branch branch, int boundingCommitId, DirectoryCoordinateIds ids) {

        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

        List<Branch> branchAncestryList = functionalQueriesDAO.getBranchAncestryList(branch.getParentBranchId());

        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();
        Map<Integer, Map<Integer, SkinnyLogfileInfo>> candidateMap = new TreeMap<>();

        String branchesToSearchString = functionalQueriesDAO.buildBranchesToSearchString(branchAncestryList);

        // This is a map of maps; outer key is file_id; inner key is commit_id, String is fileName.
        Map<Integer, Map<Integer, FileName>> candidateFileNamesMap = new TreeMap<>();
        run1stFileNameQuery(candidateFileNamesMap, boundingCommitId, ids.getDirectoryId(), branchesToSearchString);
        run2ndFileNameQuery(candidateFileNamesMap, boundingCommitId, ids.getDirectoryId(), branchesToSearchString);
        // Keyed by file_id, inner map is keyed by commit_id
        Map<Integer, Map<Integer, FileName>> pruneMap = new TreeMap<>();
        populatePruneMapFromFileNameTable(pruneMap, ids.getDirectoryId(), boundingCommitId, branchesToSearchString);
        populatePruneMapFromFileNameHistoryTable(pruneMap, ids.getDirectoryId(), boundingCommitId, branchesToSearchString);
        pruneFileNamesLocatedElsewhere(candidateFileNamesMap, pruneMap);
        pruneDeletedFileNames(candidateFileNamesMap);
        List<Integer> fileIdList = new ArrayList<>();
        for (Integer fileId : candidateFileNamesMap.keySet()) {
            fileIdList.add(fileId);
        }
        String fileIdsToSearchString = functionalQueriesDAO.buildIdsToSearchString(fileIdList);

        run1stSkinnyInfoQuery(candidateMap, ids.getDirectoryId(), boundingCommitId, branchesToSearchString, fileIdsToSearchString);
        run2ndSkinnyInfoQuery(candidateMap, ids.getDirectoryId(), boundingCommitId, branchesToSearchString, fileIdsToSearchString);

        // TODO -- if I ever support directory moves and/or renames, then I will need to add queries;
        // one for directory_location_history, and another for file_name-history and directory_location_history.
        // Since I do not yet support directory moves and/or directory renames, we'll skip that for now.
        for (Map<Integer, SkinnyLogfileInfo> candidatesMap : candidateMap.values()) {
            // Use the sorting of the TreeMap so that the last entry is the one we want, since it is the newest revision.
            Object[] skinnyArray = candidatesMap.values().toArray();
            SkinnyLogfileInfo skinnyInfo = (SkinnyLogfileInfo) skinnyArray[skinnyArray.length - 1];
            skinnyInfo.setRevisionCount(candidatesMap.size());

            // Figure out the filename to use...
            Map<Integer, FileName> fileNameMap = candidateFileNamesMap.get(skinnyInfo.getFileID());
            Object[] fileNameArray = fileNameMap.values().toArray();
            FileName fileName = (FileName) fileNameArray[fileNameArray.length - 1];
            skinnyInfo.setShortWorkfileName(fileName.getFileName());

            skinnyList.add(skinnyInfo);
            LOGGER.debug("***===>>> Branch::: BranchId: [{}] directoryId: [{}] filename: [{}] Default revision string: [{}]", branch.getId(), ids.getDirectoryId(),
                    skinnyInfo.getShortWorkfileName(), skinnyInfo.getDefaultRevisionString());
        }

        return skinnyList;
    }

    /**
     * Use file_name and directory_name tables.
     *
     * @param candidateMap
     * @param directoryId
     * @param tagBranchCommitId
     * @param branchesToSearchString
     * @param fileIdsToSearchString
     */
    private void run1stSkinnyInfoQuery(Map<Integer, Map<Integer, SkinnyLogfileInfo>> candidateMap, int directoryId, Integer tagBranchCommitId, String branchesToSearchString,
            String fileIdsToSearchString) {
        // Create the SQL query string
        String queryString = build1stSkinnyInfoQueryStringForReadOnlyBranch(branchesToSearchString, fileIdsToSearchString);
        if (queryString != null) {
            LOGGER.debug("1st query for ReadOnly branch query string: [{}]", queryString);
            ResultSet resultSet = null;
            PreparedStatement preparedStatement = null;
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                // <editor-fold>
                preparedStatement.setInt(1, directoryId);
                preparedStatement.setInt(2, tagBranchCommitId);
                preparedStatement.setInt(3, tagBranchCommitId);
                // </editor-fold>

                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String fetchedUserName = resultSet.getString(USER_NAME_SET_INDEX);
                    Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                    String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                    Integer fetchedFileRevisionId = resultSet.getInt(FILE_REVISION_ID_RESULT_SET_INDEX);
                    Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                    byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                    Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                    Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);

                    Map<Integer, SkinnyLogfileInfo> mapForCurrentFile = candidateMap.get(fetchedFileId);
                    if (mapForCurrentFile == null) {
                        mapForCurrentFile = new TreeMap<>();
                        candidateMap.put(fetchedFileId, mapForCurrentFile);
                    }

                    SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo();
                    skinnyInfo.setLastEditByString(fetchedUserName);
                    skinnyInfo.setLastCheckInDate(fetchedCommitDate);
                    skinnyInfo.setShortWorkfileName(fetchedFilename);
                    skinnyInfo.setDefaultRevisionString(String.format("%d.%d", fetchedBranchId, fetchedFileRevisionId));
                    skinnyInfo.setFileID(fetchedFileId);
                    skinnyInfo.setDefaultRevisionDigest(fetchedDigest);
                    skinnyInfo.setBranchId(fetchedBranchId);
                    skinnyInfo.setCommitId(fetchedCommitId);
                    skinnyInfo.setFileRevisionId(fetchedFileRevisionId);
                    mapForCurrentFile.put(skinnyInfo.getFileRevisionId(), skinnyInfo);
                }
            } catch (SQLException | IllegalStateException e) {
                LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: SQL exception in run1stQuery", e);
                throw new RuntimeException(e);
            } finally {
                DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
            }
        } else {
            LOGGER.debug("Skipping 1st query for ReadOnly branch for directoryId: [{}]", directoryId);
        }
    }

    /**
     * Use file_name_history and directory_name tables.
     *
     * @param candidateMap
     * @param directoryId
     * @param tagBranchCommitId
     * @param branchesToSearchString
     */
    private void run2ndSkinnyInfoQuery(Map<Integer, Map<Integer, SkinnyLogfileInfo>> candidateMap, int directoryId, Integer tagBranchCommitId, String branchesToSearchString,
            String fileIdsToSearchString) {
        String queryString = build2ndSkinnyInfoQueryStringForReadOnlyBranch(branchesToSearchString, fileIdsToSearchString);
        if (queryString != null) {
            LOGGER.debug("2nd query for ReadOnly branch query string: [{}]", queryString);
            ResultSet resultSet = null;
            PreparedStatement preparedStatement = null;
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                // <editor-fold>
                preparedStatement.setInt(1, directoryId);
                preparedStatement.setInt(2, tagBranchCommitId);
                preparedStatement.setInt(3, tagBranchCommitId);
                // </editor-fold>

                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String fetchedUserName = resultSet.getString(USER_NAME_SET_INDEX);
                    Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                    String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                    Integer fetchedFileRevisionId = resultSet.getInt(FILE_REVISION_ID_RESULT_SET_INDEX);
                    Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                    byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                    Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                    Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);

                    Map<Integer, SkinnyLogfileInfo> mapForCurrentFile = candidateMap.get(fetchedFileId);
                    if (mapForCurrentFile == null) {
                        mapForCurrentFile = new TreeMap<>();
                        candidateMap.put(fetchedFileId, mapForCurrentFile);
                    }

                    SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo();
                    skinnyInfo.setLastEditByString(fetchedUserName);
                    skinnyInfo.setLastCheckInDate(fetchedCommitDate);
                    skinnyInfo.setShortWorkfileName(fetchedFilename);
                    skinnyInfo.setDefaultRevisionString(String.format("%d.%d", fetchedBranchId, fetchedFileRevisionId));
                    skinnyInfo.setFileID(fetchedFileId);
                    skinnyInfo.setDefaultRevisionDigest(fetchedDigest);
                    skinnyInfo.setBranchId(fetchedBranchId);
                    skinnyInfo.setCommitId(fetchedCommitId);
                    skinnyInfo.setFileRevisionId(fetchedFileRevisionId);
                    mapForCurrentFile.put(skinnyInfo.getFileRevisionId(), skinnyInfo);
                }
            } catch (SQLException | IllegalStateException e) {
                LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: SQL exception in run2ndSkinnyInfoQuery", e);
                throw new RuntimeException(e);
            } finally {
                DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
            }
        } else {
            LOGGER.debug("Skipping 2nd query for ReadOnly branch for directoryId: [{}]", directoryId);
        }
    }

    @Override
    public List<DirectoryLocation> findChildDirectoryLocationsForReadOnlyBranch(Branch branch, List<Branch> branchArray, int boundingCommitId, int parentDirectoryLocationId) {

        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

        String branchesToSearchString = functionalQueriesDAO.buildBranchesToSearchString(branchArray);

        Map<String, DirectoryLocation> directoryLocationMap = new TreeMap<>();
        List<DirectoryLocation> directoryLocationList = new ArrayList<>();

        run1stDirectoryLocationQuery(parentDirectoryLocationId, branchesToSearchString, boundingCommitId, directoryLocationMap);
        run2ndDirectoryLocationQuery(parentDirectoryLocationId, branchesToSearchString, boundingCommitId, directoryLocationMap);

        for (DirectoryLocation directoryLocation : directoryLocationMap.values()) {
            directoryLocationList.add(directoryLocation);
        }
        return directoryLocationList;
    }

    private void run1stDirectoryLocationQuery(int parentDirectoryLocationId, String branchesToSearchString, Integer tagCommitId, Map<String, DirectoryLocation> directoryLocationMap) {
        // Create the SQL query string
        String selectSegment = "SELECT ID, DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, COMMIT_ID, DIRECTORY_SEGMENT_NAME, DELETED_FLAG FROM ";
        String findChildDirectoryLocationsForTagBranch = selectSegment + this.schemaName
                + ".DIRECTORY_LOCATION WHERE PARENT_DIRECTORY_LOCATION_ID = ? AND BRANCH_ID IN (%s) AND COMMIT_ID <= %d "
                + "ORDER BY DIRECTORY_SEGMENT_NAME, BRANCH_ID DESC";

        String queryString = String.format(findChildDirectoryLocationsForTagBranch, branchesToSearchString, tagCommitId);

        LOGGER.debug("run1stDirectoryLocationQuery query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, parentDirectoryLocationId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedDirectoryLocationId = resultSet.getInt(DL_ID_RESULT_SET_INDEX);
                Integer fetchedDirectoryId = resultSet.getInt(DL_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(DL_BRANCH_ID_RESULT_SET_INDEX);
                Object fetchedParentDirectoryLocationObject = resultSet.getObject(DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                Integer fetchedParentDirectoryLocationId = null;
                if (fetchedParentDirectoryLocationObject != null) {
                    fetchedParentDirectoryLocationId = resultSet.getInt(DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(DL_COMMIT_ID_RESULT_SET_INDEX);
                String fetchedDirectorySegmentName = resultSet.getString(DL_DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(DL_DELETED_FLAG_RESULT_SET_INDEX);

                DirectoryLocation directoryLocation = new DirectoryLocation();
                directoryLocation.setId(fetchedDirectoryLocationId);
                directoryLocation.setDirectoryId(fetchedDirectoryId);
                directoryLocation.setBranchId(fetchedBranchId);
                directoryLocation.setParentDirectoryLocationId(fetchedParentDirectoryLocationId);
                directoryLocation.setCommitId(fetchedCommitId);
                directoryLocation.setDirectorySegmentName(fetchedDirectorySegmentName);
                directoryLocation.setDeletedFlag(fetchedDeletedFlag);
                directoryLocationMap.put(directoryLocation.getDirectorySegmentName(), directoryLocation);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: SQL exception in run1stDirectoryLocationQuery", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: exception in run1stDirectoryLocationQuery", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private void run2ndDirectoryLocationQuery(int parentDirectoryLocationId, String branchesToSearchString, Integer tagCommitId, Map<String, DirectoryLocation> directoryLocationMap) {
        // Create the SQL query string
        String selectSegment = "SELECT DIRECTORY_LOCATION_ID, DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, COMMIT_ID, DIRECTORY_SEGMENT_NAME, DELETED_FLAG FROM ";
        String findChildDirectoryLocationsForTagBranch = selectSegment + this.schemaName
                + ".DIRECTORY_LOCATION_HISTORY WHERE PARENT_DIRECTORY_LOCATION_ID = ? AND BRANCH_ID IN (%s) AND COMMIT_ID <= %d "
                + "ORDER BY DIRECTORY_SEGMENT_NAME, BRANCH_ID DESC";

        String queryString = String.format(findChildDirectoryLocationsForTagBranch, branchesToSearchString, tagCommitId);

        LOGGER.debug("run2ndDirectoryLocationQuery query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, parentDirectoryLocationId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedDirectoryLocationId = resultSet.getInt(DL_ID_RESULT_SET_INDEX);
                Integer fetchedDirectoryId = resultSet.getInt(DL_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(DL_BRANCH_ID_RESULT_SET_INDEX);
                Object fetchedParentDirectoryLocationObject = resultSet.getObject(DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                Integer fetchedParentDirectoryLocationId = null;
                if (fetchedParentDirectoryLocationObject != null) {
                    fetchedParentDirectoryLocationId = resultSet.getInt(DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(DL_COMMIT_ID_RESULT_SET_INDEX);
                String fetchedDirectorySegmentName = resultSet.getString(DL_DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(DL_DELETED_FLAG_RESULT_SET_INDEX);

                DirectoryLocation directoryLocation = new DirectoryLocation();
                directoryLocation.setId(fetchedDirectoryLocationId);
                directoryLocation.setDirectoryId(fetchedDirectoryId);
                directoryLocation.setBranchId(fetchedBranchId);
                directoryLocation.setParentDirectoryLocationId(fetchedParentDirectoryLocationId);
                directoryLocation.setCommitId(fetchedCommitId);
                directoryLocation.setDirectorySegmentName(fetchedDirectorySegmentName);
                directoryLocation.setDeletedFlag(fetchedDeletedFlag);
                directoryLocationMap.put(directoryLocation.getDirectorySegmentName(), directoryLocation);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: SQL exception in run2ndDirectoryLocationQuery", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: exception in run2ndDirectoryLocationQuery", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private String build1stSkinnyInfoQueryStringForReadOnlyBranch(String branchesToSearchString, String fileIdsToSearchString) {
        String queryString = null;
        if (!fileIdsToSearchString.isEmpty()) {

            String selectSegment = "SELECT UR.USER_NAME, CM.COMMIT_DATE, FN.FILE_NAME, FR.ID AS FRID, FR.FILE_ID, FR.REVISION_DIGEST, FR.BRANCH_ID, CM.ID FROM ";
            StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
            queryFormatStringBuilder.append(this.schemaName).append(".FILE_REVISION FR,")
                    .append(this.schemaName).append(".COMIT CM,")
                    .append(this.schemaName).append(".FILE_NAME FN,")
                    .append(this.schemaName).append(".DIRECTORY_LOCATION DL,")
                    .append(this.schemaName).append(".USER UR ")
                    .append("WHERE ");
            if (fileIdsToSearchString.length() > 0) {
                queryFormatStringBuilder.append("FN.FILE_ID IN (%s) AND ");
            }
            queryFormatStringBuilder.append("FR.BRANCH_ID IN (%s) AND ")
                    .append("FR.COMMIT_ID = CM.ID AND ")
                    .append("FR.PROMOTED_FLAG = FALSE AND ")
                    .append("CM.USER_ID = UR.ID AND ")
                    .append("FN.FILE_ID = FR.FILE_ID AND ")
                    .append("DL.DIRECTORY_ID = FN.DIRECTORY_ID AND ")
                    .append("DL.DIRECTORY_ID = ? AND ")
                    .append("FN.COMMIT_ID <= ? AND ")
                    .append("FR.COMMIT_ID <= ? ")
                    .append("ORDER BY BRANCH_ID DESC, FILE_ID, FR.ID DESC");
            String queryFormatString = queryFormatStringBuilder.toString();
            if (fileIdsToSearchString.length() > 0) {
                queryString = String.format(queryFormatString, fileIdsToSearchString, branchesToSearchString);
            } else {
                queryString = String.format(queryFormatString, branchesToSearchString);
            }
        }
        return queryString;
    }

    private String build2ndSkinnyInfoQueryStringForReadOnlyBranch(String branchesToSearchString, String fileIdsToSearchString) {
        String queryString = null;
        if (!fileIdsToSearchString.isEmpty()) {

            String selectSegment = "SELECT UR.USER_NAME, CM.COMMIT_DATE, FN.FILE_NAME, FR.ID AS FRID, FR.FILE_ID, FR.REVISION_DIGEST, FR.BRANCH_ID, CM.ID FROM ";
            StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
            queryFormatStringBuilder.append(this.schemaName).append(".FILE_REVISION FR,")
                    .append(this.schemaName).append(".COMIT CM,")
                    .append(this.schemaName).append(".FILE_NAME_HISTORY FN,")
                    .append(this.schemaName).append(".DIRECTORY_LOCATION DL,")
                    .append(this.schemaName).append(".USER UR ")
                    .append("WHERE ");
            if (fileIdsToSearchString.length() > 0) {
                queryFormatStringBuilder.append("FN.FILE_ID IN (%s) AND ");
            }
            queryFormatStringBuilder.append("FR.BRANCH_ID IN (%s) AND ")
                    .append("FR.COMMIT_ID = CM.ID AND ")
                    .append("FR.PROMOTED_FLAG = FALSE AND ")
                    .append("CM.USER_ID = UR.ID AND ")
                    .append("FN.FILE_ID = FR.FILE_ID AND ")
                    .append("DL.DIRECTORY_ID = FN.DIRECTORY_ID AND ")
                    .append("DL.DIRECTORY_ID = ? AND ")
                    .append("FN.COMMIT_ID <= ? AND ")
                    .append("FR.COMMIT_ID <= ? ")
                    .append("ORDER BY BRANCH_ID DESC, FILE_ID, FR.ID DESC");
            String queryFormatString = queryFormatStringBuilder.toString();
            if (fileIdsToSearchString.length() > 0) {
                queryString = String.format(queryFormatString, fileIdsToSearchString, branchesToSearchString);
            } else {
                queryString = String.format(queryFormatString, branchesToSearchString);
            }
        }
        return queryString;
    }

    /**
     * We query the file_name table to get the file_id's and file_names that we
     * need to look at.
     *
     * @param candidateFileNamesMap where we put the results.
     * @param boundingCommitId the bounding commit_id; all results must have a
     * commit_id less than this bounding commit_id.
     * @param directoryId the directory_id where we're looking for filenames.
     * @param branchesToSearchString the branches to look in.
     */
    private void run1stFileNameQuery(Map<Integer, Map<Integer, FileName>> candidateFileNamesMap, int boundingCommitId, int directoryId, String branchesToSearchString) {
        String selectSegment = "SELECT FN.FILE_ID, FN.DIRECTORY_ID, FN.COMMIT_ID, FN.FILE_NAME, FN.DELETED_FLAG FROM ";
        StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
        String findFileNames = queryFormatStringBuilder.append(this.schemaName).append(".FILE_NAME FN ")
                .append("WHERE ")
                .append("BRANCH_ID IN (%s) AND ")
                .append("DIRECTORY_ID = ? AND ")
                .append("COMMIT_ID <= ? ").toString();
        String queryString = String.format(findFileNames, branchesToSearchString);
        LOGGER.debug("run1stFileNameQuery query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, directoryId);
            preparedStatement.setInt(2, boundingCommitId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedFileId = resultSet.getInt(1);
                Integer fetchedDirectoryId = resultSet.getInt(2);
                Integer fetchedCommitId = resultSet.getInt(3);
                String fetchedFileName = resultSet.getString(4);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(5);
                FileName fileName = new FileName();
                fileName.setFileId(fetchedFileId);
                fileName.setDirectoryId(fetchedDirectoryId);
                fileName.setCommitId(fetchedCommitId);
                fileName.setFileName(fetchedFileName);
                fileName.setDeletedFlag(fetchedDeletedFlag);
                Map<Integer, FileName> commitIdMap = candidateFileNamesMap.get(fetchedFileId);
                if (commitIdMap == null) {
                    commitIdMap = new TreeMap<>();
                }
                commitIdMap.put(fetchedCommitId, fileName);
                candidateFileNamesMap.put(fetchedFileId, commitIdMap);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: SQL exception in run1stFileNameQuery", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: exception in run1stFileNameQuery", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private void run2ndFileNameQuery(Map<Integer, Map<Integer, FileName>> candidateFileNamesMap, int boundingCommitId, int directoryId, String branchesToSearchString) {
        String selectSegment = "SELECT FN.FILE_ID, FN.DIRECTORY_ID, FN.COMMIT_ID, FN.FILE_NAME, FN.DELETED_FLAG FROM ";
        StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
        String findFileNames = queryFormatStringBuilder.append(this.schemaName).append(".FILE_NAME_HISTORY FN ")
                .append("WHERE ")
                .append("BRANCH_ID IN (%s) AND ")
                .append("COMMIT_ID <= ? ")
                .append("ORDER BY FILE_ID, COMMIT_ID DESC").toString();
        String queryString = String.format(findFileNames, branchesToSearchString);
        LOGGER.debug("run2ndFileNameQuery query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, boundingCommitId);

            resultSet = preparedStatement.executeQuery();
            int currentFileId = -1;
            boolean skipFlag = false;
            while (resultSet.next()) {
                int fetchedFileId = resultSet.getInt(1);
                int fetchedDirectoryId = resultSet.getInt(2);
                int fetchedCommitId = resultSet.getInt(3);
                String fetchedFileName = resultSet.getString(4);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(5);
                FileName fileName = new FileName();
                fileName.setFileId(fetchedFileId);
                fileName.setCommitId(fetchedCommitId);
                fileName.setFileName(fetchedFileName);
                fileName.setDeletedFlag(fetchedDeletedFlag);
                if (fetchedFileId != currentFileId) {
                    currentFileId = fetchedFileId;
                    skipFlag = false;
                }
                if (fetchedDirectoryId != directoryId) {
                    skipFlag = true;
                }
                if (!skipFlag) {
                    // Only snag file names for the directory we're interested in...
                    Map<Integer, FileName> commitIdMap = candidateFileNamesMap.get(fetchedFileId);
                    if (commitIdMap == null) {
                        commitIdMap = new TreeMap<>();
                    }
                    commitIdMap.put(fetchedCommitId, fileName);
                    candidateFileNamesMap.put(fetchedFileId, commitIdMap);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: SQL exception in run2ndFileNameQuery", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: exception in run2ndFileNameQuery", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private void populatePruneMapFromFileNameTable(Map<Integer, Map<Integer, FileName>> pruneMap, int directoryId, int boundingCommitId, String branchesToSearchString) {
        String selectSegment = "SELECT FN.FILE_ID, FN.DIRECTORY_ID, FN.COMMIT_ID, FN.FILE_NAME FROM ";
        StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
        String findFileNames = queryFormatStringBuilder.append(this.schemaName).append(".FILE_NAME FN ")
                .append("WHERE ")
                .append("BRANCH_ID IN (%s) AND ")
                .append("COMMIT_ID <= ? AND ")
                .append("DIRECTORY_ID != ? AND ")
                .append("DELETED_FLAG = FALSE ORDER BY FILE_ID, COMMIT_ID DESC").toString();
        String queryString = String.format(findFileNames, branchesToSearchString);
        LOGGER.debug("populatePruneMapFromFileNameTable query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, boundingCommitId);
            preparedStatement.setInt(2, directoryId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int fetchedFileId = resultSet.getInt(1);
                int fetchedDirectoryId = resultSet.getInt(2);
                int fetchedCommitId = resultSet.getInt(3);
                String fetchedFileName = resultSet.getString(4);
                FileName fileName = new FileName();
                fileName.setFileId(fetchedFileId);
                fileName.setDirectoryId(fetchedDirectoryId);
                fileName.setCommitId(fetchedCommitId);
                fileName.setFileName(fetchedFileName);
                Map<Integer, FileName> innerMap = pruneMap.get(fileName.getFileId());
                if (innerMap == null) {
                    innerMap = new TreeMap<>();
                    pruneMap.put(fileName.getFileId(), innerMap);
                }
                innerMap.put(fileName.getCommitId(), fileName);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: SQL exception in populatePruneMapFromFileNameTable", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: exception in populatePruneMapFromFileNameTable", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private void populatePruneMapFromFileNameHistoryTable(Map<Integer, Map<Integer, FileName>> pruneMap, int directoryId, int boundingCommitId, String branchesToSearchString) {
        String selectSegment = "SELECT FN.FILE_ID, FN.DIRECTORY_ID, FN.COMMIT_ID, FN.FILE_NAME FROM ";
        StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
        String findFileNames = queryFormatStringBuilder.append(this.schemaName).append(".FILE_NAME_HISTORY FN ")
                .append("WHERE ")
                .append("BRANCH_ID IN (%s) AND ")
                .append("COMMIT_ID <= ? AND ")
                .append("DIRECTORY_ID != ? AND ")
                .append("DELETED_FLAG = FALSE ORDER BY FILE_ID, COMMIT_ID DESC").toString();
        String queryString = String.format(findFileNames, branchesToSearchString);
        LOGGER.debug("populatePruneMapFromFileNameHistoryTable query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, boundingCommitId);
            preparedStatement.setInt(2, directoryId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int fetchedFileId = resultSet.getInt(1);
                int fetchedDirectoryId = resultSet.getInt(2);
                int fetchedCommitId = resultSet.getInt(3);
                String fetchedFileName = resultSet.getString(4);
                FileName fileName = new FileName();
                fileName.setFileId(fetchedFileId);
                fileName.setDirectoryId(fetchedDirectoryId);
                fileName.setCommitId(fetchedCommitId);
                fileName.setFileName(fetchedFileName);
                Map<Integer, FileName> innerMap = pruneMap.get(fileName.getFileId());
                if (innerMap == null) {
                    innerMap = new TreeMap<>();
                    pruneMap.put(fileName.getFileId(), innerMap);
                }
                innerMap.put(fileName.getCommitId(), fileName);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: SQL exception in populatePruneMapFromFileNameHistoryTable", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReadOnlyBranchesDAOImpl: exception in populatePruneMapFromFileNameHistoryTable", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private void pruneFileNamesLocatedElsewhere(Map<Integer, Map<Integer, FileName>> candidateFileNamesMap, Map<Integer, Map<Integer, FileName>> pruneMap) {
        for (Map<Integer, FileName> commitIdMap : pruneMap.values()) {
            Object[] pruneCandidatefileNameArray = commitIdMap.values().toArray();
            FileName pruneCandidateFileName = (FileName) pruneCandidatefileNameArray[pruneCandidatefileNameArray.length - 1];
            Map<Integer, FileName> candidateFileNameMap = candidateFileNamesMap.get(pruneCandidateFileName.getFileId());
            if (candidateFileNameMap != null) {
                Object[] candidateFileNameArray = candidateFileNameMap.values().toArray();
                FileName candidateFileName = (FileName) candidateFileNameArray[candidateFileNameArray.length - 1];
                if (pruneCandidateFileName.getCommitId() > candidateFileName.getCommitId()) {
                    LOGGER.debug("Pruning file: [{}] as it has a newer commit located in directory: [{}]", candidateFileName.getFileName(), pruneCandidateFileName.getDirectoryId());
                    candidateFileNamesMap.remove(candidateFileName.getFileId());
                }
            }
        }
    }

    private void pruneDeletedFileNames(Map<Integer, Map<Integer, FileName>> candidateFileNamesMap) {
        // We have to use an Iterator to avoid concurrent modification exceptions.
        for (Iterator<Map.Entry<Integer, Map<Integer, FileName>>> it = candidateFileNamesMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Map<Integer, FileName>> entry = it.next();
            Map<Integer, FileName> commitMap = entry.getValue();
            Object[] candidateFileNameArray = commitMap.values().toArray();
            FileName candidateFileName = (FileName) candidateFileNameArray[candidateFileNameArray.length - 1];
            if (candidateFileName.getDeletedFlag()) {
                it.remove();
            }
        }
    }
}
