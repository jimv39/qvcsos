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
// $Filename: C:\qRoot\testQVCSWorkfiles\Remote Secure Java Project\QumaProjects\com\qumasoft\client\TestCheckOutArchive.java $
//     $Date: Wednesday, February 12, 2003 10:02:22 PM $
// $Revision: 1.57 $
//
// $Log5: C:\qRoot\testQVCSArchives\Remote Secure Java Project\QumaProjects\com\qumasoft\client\TestCheckOutArchive.kbwb $
// 
//   LogfileImpl class. This is the main class for operating on a QVCS
//   archive.
// 
// Revision 1.57  by: JimVoris  Rev date: 2/12/2003 10:02:39 PM
//   Remove unneeded import statements.
// 
// Revision 1.56  by: JimVoris  Rev date: 2/2/2003 6:09:14 PM
//   Add functionality for unlock toolbar button.
// 
// Revision 1.55  by: JimVoris  Rev date: 2/1/2003 11:30:00 AM
//   Make the output file read/write so we can overwrite it.
// 
// Revision 1.54  by: JimVoris  Rev date: 1/8/2003 7:42:14 PM
//   Add getIsObsolete() method.
// 
// Revision 1.53  by: JimVoris  Rev date: 1/5/2003 5:03:59 PM
//   Generalize the static conversion methods so they should work with either forward or backslash characters a the pathSeparator.
// 
// $Endlog$
//
// $Copyright    Insert your copyright information here. $

package TestFiles;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.File;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.qumasoft.operations.CompareFilesEditInformation;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.CompressionFactory;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogFileOperationCheckInCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationCheckOutCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationCreateArchiveCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationGetRevisionCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationLockRevisionCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationUnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.MutableInteger;
import com.qumasoft.qvcslib.MutableString;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QumaAssert;
import com.qumasoft.qvcslib.RevisionDescriptor;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.WorkFile;

class LogFileImpl
{
    // Create our logger object
    private static Logger m_logger = Logger.getLogger("com.qumasoft.qvcslib");

    /** define the size of the buffer we use for file copies */
    private static final int FILE_COPY_BUFFER_SIZE = 32000;

    private RandomAccessFile m_inStream;
    /**
     * Flag to indicate if file is already open
     */
    private boolean m_isOpen;
    
    /** Flag to indicate whether the header information has been read */
    private boolean m_isHeaderInfoRead;
    private boolean m_isRevisionInfoRead;

    private RevisionInformation m_RevisionInfo;
    private String              m_FileName;
    private String              m_ShortArchiveName;
    private java.io.File        m_File;
    private String              m_TempFileName;
    private java.io.File        m_TempFile;
    private String              m_OldFileName;
    private java.io.File        m_OldFile;
    private String              m_ShortWorkfileName;
    private LogFileHeaderInfo   m_HeaderInfo;
    private MutableByteArray    m_ProcessedBuffer      = new MutableByteArray();
    private CompressionFactory  m_CompressionFactory   = new CompressionFactory();
    private AccessList          m_AccessList = null;
    private AccessList          m_ModifierList = null;
    private boolean             m_MustReadArchiveFile = true;
    private LogfileInfo         m_LogfileInfo = null;

    RevisionHeader getRevisionHeader(int index)
    {
        if (index < getRevisionCount())
        {
            return m_RevisionInfo.getRevisionHeader(index);
        }
        else
        {
            return null;
        }
    }
    
    RevisionInformation getRevisionInformation()
    {
        if (!isArchiveInformationRead())
        {
            readInformation();
        }
        return m_RevisionInfo;
    }

    LogfileInfo getLogfileInfo()
    {
        if (m_LogfileInfo == null)
        {
            m_LogfileInfo = new LogfileInfo(getLogFileHeaderInfo(), getRevisionInformation(), getFileName());
        }
        return m_LogfileInfo;
    }
    
    boolean isArchiveInformationRead()
    {
        return m_isHeaderInfoRead && m_isRevisionInfoRead;
    }
    
    boolean isValidArchive()
    {
        if (!isArchiveInformationRead())
        {
            readInformation();
        }
        return isArchiveInformationRead();
    }
    
    boolean getIsObsolete()
    {
        if (!isArchiveInformationRead())
        {
            readInformation();
        }
        return m_HeaderInfo.getIsObsolete();
    }

    int getRevisionCount()
    {
        if (!isArchiveInformationRead())
        {
            readInformation();
        }
        return m_HeaderInfo.getRevisionCount();
    }
    
    String getShortWorkfileName()
    {
        return m_ShortWorkfileName;
    }
    
    String getShortArchiveName()
    {
        return m_ShortArchiveName;
    }
    
    int getLockCount()
    {
        if (!isArchiveInformationRead())
        {
            readInformation();
        }

        return m_HeaderInfo.getLogFileHeader().lockCount();
    }
    
    File getFile()
    {
        return m_File;
    }
    
    String getFileName()
    {
        return m_FileName;
    }
    
    File getOldFile()
    {
        return m_OldFile;
    }
    
    String getOldFileName()
    {
        return m_OldFileName;
    }
    
    File getTempFile()
    {
        return m_TempFile;
    }
    
    String getTempFileName()
    {
        return m_TempFileName;
    }

    String getLockedByString()
    {
        return "Unknown";
    }
    
    /**
     * Return the user name(s) that hold any locks on this archive.  The format of the 
     * returned string is for use within the GUI.  If there are multiple lockers, they are
     * all returned.
     */
    String getLockedByUser()
    {
        String returnString;
        
        if (!isArchiveInformationRead())
        {
            readInformation();
        }
        
        if (isArchiveInformationRead())
        {
            if (m_HeaderInfo.getLogFileHeader().lockCount() > 0)
            {
                StringBuffer lockerString = new StringBuffer();
                int revisionCount = m_HeaderInfo.getRevisionCount();
                int lockCount = m_HeaderInfo.getLogFileHeader().lockCount();
                for (int i = 0, j = 0; (i < revisionCount) && (j < lockCount); i++)
                {
                    RevisionHeader revHeader = m_RevisionInfo.getRevisionHeader(i);
                    if (revHeader.isLocked())
                    {
                        j++;
                        lockerString.append(revHeader.getRevisionString() + "-" + indexToUsername(revHeader.getLockerIndex()));
                    }
                }
                returnString = lockerString.toString();
            }
            else
            {
                returnString = "";
            }
        }
        else
        {
            returnString = "";
        }
        return returnString;
    }
    
    String getWorkfileInLocation()
    {
        if (!isArchiveInformationRead())
        {
            readInformation();
        }

        String returnString = "";
        if (m_HeaderInfo.getLogFileHeader().lockCount() > 0)
        {
            returnString = m_HeaderInfo.getWorkfileName();
        }
        return returnString;
    }
    
    java.util.Date getLastCheckInDate()
    {
        RevisionHeader defaultRevision = getDefaultRevisionHeader();
        return defaultRevision.getCheckInDate();
    }
    
    String getLastEditBy()
    {
        RevisionHeader defaultRevision = getDefaultRevisionHeader();
        return indexToUsername(defaultRevision.getCreatorIndex());
    }
    
    RevisionHeader getDefaultRevisionHeader()
    {
        RevisionHeader returnHeader = null;

        if (!isArchiveInformationRead())
        {
            readInformation();
        }
        
        if (isArchiveInformationRead())
        {
            // If the default branch isn't the trunk, then we have some work to do...
            if (m_HeaderInfo.getLogFileHeader().defaultDepth() > 0)
            {
                int revisionCount = m_HeaderInfo.getRevisionCount();
                RevisionDescriptor defaultDescriptor = m_HeaderInfo.getDefaultRevisionDescriptor();
                String defaultBranchString = defaultDescriptor.toString();
                for (int i = 0; i < revisionCount; i++)
                {
                    RevisionHeader revHeader = m_RevisionInfo.getRevisionHeader(i);
                    if (revHeader.getDepth() == defaultDescriptor.getElementCount() - 1)
                    {
                        if (revHeader.isTip())
                        {
                            String revisionString = revHeader.getRevisionString();
                            int lastDot = revisionString.lastIndexOf('.');
                            String truncatedRevisionString = revisionString.substring(0, lastDot);
                            if (truncatedRevisionString.compareToIgnoreCase(defaultBranchString) == 0)
                            {
                                returnHeader = revHeader;
                                break;
                            }
                        }
                    }
                }
            }
            else
            {
                // The default branch is the trunk.  Things are simple here.
                returnHeader = m_RevisionInfo.getRevisionHeader(0);
            }
        }
        else
        {
            returnHeader = null;
        }
        
        return returnHeader;
    }
    
    String indexToUsername(int index)
    {
        if (!isArchiveInformationRead())
        {
            readInformation();
        }
        
        return m_ModifierList.indexToUser(index);
    }
    
    
    /**
     * This recursive routine will fetch the requested revision into the output file.
     * This routine does handle compressed revisions, but it does not perform keyword
     * expansion.
     */
    boolean fetchRevision(RevisionHeader revisionHeader, String outputFilename, boolean recurseFlag, MutableByteArray processedBuffer)
    {
        boolean bRetVal = false;
        boolean streamIsOpen = false;
        BufferedOutputStream outStream = null;

        try
        {
            if ((revisionHeader.getDepth() == 0) && revisionHeader.isTip())
            {
                // Read the archive file to retrieve the revision.
                byte[] unCompressedRevisionData;
                byte[] revisionData = new byte[(int)revisionHeader.getRevisionSize()];
                m_inStream.seek(revisionHeader.getRevisionDataStartPosition());
                m_inStream.read(revisionData);
                    
                // Decompress the buffer if we need to.
                if (revisionHeader.isCompressed())
                {
                    unCompressedRevisionData = deCompressRevisionData(revisionHeader, revisionData);
                }
                else
                {
                    unCompressedRevisionData = revisionData;
                }
                processedBuffer.setValue(unCompressedRevisionData);
                revisionData = null;
                bRetVal = true;
            }
            else
            {
                // They are requesting an older revision.
                if (revisionHeader.getParentRevisionHeader() == null)
                {
                    // We've reached the tip revision for the requested revision.
                    // but we're recursing and this must be a non-tip revision.
                    // All non-tip revisions MUST have a parent, so to get here
                    // is a boo-boo.
                    QumaAssert.isTrue(false);
                }
                else
                {
                    // We're still working our way to the tip revision.
                    bRetVal = fetchRevision(revisionHeader.getParentRevisionHeader(), outputFilename, true, processedBuffer);
                        
                    // We got our parent.  Now apply our edits to our parent to get the result.
                    if (bRetVal)
                    {
                        byte[] editBuffer = new byte[(int)revisionHeader.getRevisionSize()];
                        byte[] uncompressedEditBuffer;
                        m_inStream.seek(revisionHeader.getRevisionDataStartPosition());
                        m_inStream.read(editBuffer);
                        if (revisionHeader.isCompressed())
                        {
                            uncompressedEditBuffer = deCompressRevisionData(revisionHeader, editBuffer);
                        }
                        else
                        {
                            uncompressedEditBuffer = editBuffer;
                        }
                        byte[] afterEdits = applyEdits(uncompressedEditBuffer, processedBuffer.getValue());
                        processedBuffer.setValue(afterEdits);
                        editBuffer = null;
                        uncompressedEditBuffer = null;
                        bRetVal = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            m_logger.log(Level.WARNING, "Failed to fetch revision: " + revisionHeader.getRevisionString());
            m_logger.log(Level.WARNING, e.getLocalizedMessage());
            e.printStackTrace();
            bRetVal = false;
        }
            
        // Write the result.
        if (!recurseFlag && bRetVal)
        {
            try
            {
                WorkFile outputFile = new WorkFile(outputFilename);
                
                // Overwrite if it is write protected.
                if (!outputFile.canWrite())
                {
                    outputFile.setReadWrite();
                }
                outStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                streamIsOpen = true;
                outStream.write(processedBuffer.getValue());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                bRetVal = false;
            }
            finally
            {
                if (streamIsOpen)
                {
                    try
                    {
                        outStream.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bRetVal;
    }
    
    private byte[] applyEdits(byte[] edits, byte[] originalData) throws QVCSException
    {
        DataInputStream editStream = new DataInputStream(new ByteArrayInputStream(edits));
        CompareFilesEditInformation editInfo = new CompareFilesEditInformation();
        byte[] editedBuffer = new byte[edits.length + originalData.length]; // It can't be any bigger than this.
        byte[] returnedBuffer = null;
        int inIndex = 0;
        int outIndex = 0;
        int deletedBytesCount = 0;
        int insertedBytesCount = 0;
        int bytesTillChange = 0;

        try
        {
            while(editStream.available() > 0)
            {
                editInfo.read(editStream);
                bytesTillChange = (int)editInfo.getSeekPosition() - inIndex;
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, bytesTillChange);
                
                inIndex += bytesTillChange;
                outIndex += bytesTillChange;
            
                deletedBytesCount   = (int)editInfo.getDeletedBytesCount();
                insertedBytesCount  = (int)editInfo.getInsertedBytesCount();

                switch (editInfo.getEditType())
                {
                    case CompareFilesEditInformation.qvcsEDIT_DELETE:   /* Delete input */
                    {
                        // Just skip over deleted bytes
                        inIndex += deletedBytesCount;
                        break;
                    }

                    case CompareFilesEditInformation.qvcsEDIT_INSERT:   /* Insert edit lines */
                    {
                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;
                    }

                    case CompareFilesEditInformation.qvcsEDIT_REPLACE:  /* Replace input line with edit line */
                    {
                        /*
                        * First skip over the bytes to be replaced, then copy
                        * the replacing bytes from the edit file to the output file.
                        */
                        inIndex  += deletedBytesCount;

                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;
                    }

                    default:
                    {
                        continue;
                    }
                }
            }

            // Copy the rest of the input "file" to the output "file".
            int remainingBytes = originalData.length - inIndex;
            if (remainingBytes > 0)
            {
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, remainingBytes);
                outIndex += remainingBytes;
            }
            returnedBuffer = new byte[outIndex];
            System.arraycopy(editedBuffer, 0, returnedBuffer, 0, outIndex);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            m_logger.log(Level.WARNING, " editInfo.seekPosition: " + editInfo.getSeekPosition() + " originalData.length: " + originalData.length + " inIndex: " + inIndex + " editedBuffer.length: " + editedBuffer.length + " outIndex: " + outIndex + " bytesTillChange: " + bytesTillChange);
            m_logger.log(Level.WARNING, e.getLocalizedMessage());
            throw new QVCSException("Internal error!! for " + getShortWorkfileName());
        }
        finally
        {
            editedBuffer = null;
            editInfo = null;
            try
            {
                editStream.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return returnedBuffer;
    }
    
    /**
     * Find the requested revision within the archive.  Return true if we find
     * that revision; also return the index of that revision.  If the revision
     * is not found, return false.
     */
    boolean findRevision(String revision, MutableInteger revisionIndex)
    {
        QumaAssert.isTrue(m_isHeaderInfoRead);
        QumaAssert.isTrue(m_isRevisionInfoRead);

        boolean bRetVal = false;
        int revisionCount = m_HeaderInfo.getRevisionCount();
        for (int i = 0; i < revisionCount; i++)
        {
            RevisionHeader revHeader = m_RevisionInfo.getRevisionHeader(i);
            String revisionString = revHeader.getRevisionString();
            if (revision.compareTo(revisionString) == 0)
            {
                bRetVal = true;
                revisionIndex.setValue(i);
                break;
            }
        }
        
        return bRetVal;
    }
    
    private byte[] deCompressRevisionData(RevisionHeader revisionHeader, byte[] revisionData)
    {
        Compressor compressor = m_CompressionFactory.getCompressor(revisionHeader.getCompressionHeader());
        return compressor.expand(revisionHeader.getCompressionHeader(), revisionData);
    }

    @Override
    public String toString()
    {
        String returnString = new String();
        returnString = "QVCS archive name:\t" + m_FileName;
        returnString += m_HeaderInfo.toString();
        return returnString;
    }

    public LogFileImpl(String fullArchiveFilename)
    {
        m_isOpen             = false;
        m_isHeaderInfoRead   = false;
        m_isRevisionInfoRead = false;

        // Set the filename
        m_FileName = fullArchiveFilename;
        m_File = new File(m_FileName);

        // Set the name of the temp file for the archive
        m_TempFileName = new String (m_FileName + ".temp");
        m_TempFile = new java.io.File(m_TempFileName);
        
        // Set the name of the old file for the archive
        m_OldFileName = new String (m_FileName + ".old");
        m_OldFile = new java.io.File(m_OldFileName);
        
        // Figure out the short workfile name
        m_ShortWorkfileName = convertArchiveNameToShortWorkfileName(fullArchiveFilename, File.separator);
        
        // Figure out the short archive name
        m_ShortArchiveName = convertWorkfileNameToShortArchiveName(m_ShortWorkfileName);
    }
    
    private static String deducePathSeparator(String filename)
    {
        String pathSeparator = "/";

        // Search for a path separator.... it might be '\', or it might be '/'
        int pathSeparatorCandidateForwardSlashIndex = filename.indexOf('/');
        int pathSeparatorCandidateBackSlashIndex = filename.indexOf('\\');
        
        if (pathSeparatorCandidateForwardSlashIndex > -1)
        {
            pathSeparator = "/";
        }
        else if (pathSeparatorCandidateBackSlashIndex > -1)
        {
            pathSeparator = "\\";
        }
        else
        {
            pathSeparator = "/";
        }
        return pathSeparator;
    }
    
    static String convertArchiveNameToShortWorkfileName(String archiveName, String pathSeparator)
    {
        String nameSeparator = new String(".");
        pathSeparator = deducePathSeparator(archiveName);
        
        // Search the archiveName backwards, looking for the path separator.
        int pathSeparatorIndex = archiveName.lastIndexOf(pathSeparator);
        
        // Make a StringBuffer we'll use for changing the name
        StringBuffer tempWorkfileName = new StringBuffer(archiveName);
        
        // Search the archiveName backwards, looking for the name separator.
        int nameSeparatorIndex = archiveName.lastIndexOf(nameSeparator);
        if (nameSeparatorIndex != -1)
        {
            String extension = archiveName.substring(nameSeparatorIndex);
            if (0 == extension.compareTo(".____"))
            {
                // Strip off the fake extension to create the workfile name.
                tempWorkfileName.setLength(archiveName.length() - 4);
            }
            else
            {
                int lastIndex = archiveName.length() - 1;
                int extensionIndex = nameSeparatorIndex + 1;
                for (;extensionIndex <= lastIndex; extensionIndex++)
                {
                    switch (archiveName.charAt(extensionIndex))
                    {
                        case '?':
                        case '*':
                        case '_':
                        case '^':
                        case '~':
                        case '!':
                        case '-':
                        case '{':
                        case '\'':
                        {
                            break;
                        }

                        case 'a':
                        {
                            tempWorkfileName.setCharAt(extensionIndex,'z');
                            break;
                        }

                        case 'A':
                        {
                            tempWorkfileName.setCharAt(extensionIndex,'Z');
                            break;
                        }

                        case '0':
                        {
                            tempWorkfileName.setCharAt(extensionIndex,'9');
                            break;
                        }

                        default:
                        {
                            tempWorkfileName.setCharAt(extensionIndex, (char)(archiveName.charAt(extensionIndex) - 1));
                            break;
                        }
                    }
                }
            }
        }
        String returnString = new String(tempWorkfileName);
        if (pathSeparatorIndex != -1)
        {
            returnString = returnString.substring(pathSeparatorIndex + 1);
        }

        return returnString;
    }
    
    static String convertWorkfileNameToShortArchiveName(String workfileName)
    {
        String pathSeparator = deducePathSeparator(workfileName);
        String nameSeparator = new String(".");
        
        // Search the workfileName backwards, looking for the path separator.
        int pathSeparatorIndex = workfileName.lastIndexOf(pathSeparator);
        
        // Make a StringBuffer we'll use for changing the name
        StringBuffer tempArchiveName = new StringBuffer(workfileName);
        
        // Search the archiveName backwards, looking for the name separator.
        int nameSeparatorIndex = workfileName.lastIndexOf(nameSeparator);
        if (nameSeparatorIndex != -1)
        {
            String extension = workfileName.substring(nameSeparatorIndex);
            if (extension.length() == 0)
            {
                // Append the "___" to the workfile name to create the archive name.
                tempArchiveName.append("___");
            }
            else
            {
                int lastIndex = workfileName.length() - 1;
                int extensionIndex = nameSeparatorIndex + 1;
                for (;extensionIndex <= lastIndex; extensionIndex++)
                {
                    switch (workfileName.charAt(extensionIndex))
                    {
                        case '?':
                        case '*':
                        case '_':
                        case '^':
                        case '~':
                        case '!':
                        case '-':
                        case '{':
                        case '\'':
                        {
                            break;
                        }

                        case 'z':
                        {
                            tempArchiveName.setCharAt(extensionIndex,'a');
                            break;
                        }

                        case 'Z':
                        {
                            tempArchiveName.setCharAt(extensionIndex,'A');
                            break;
                        }

                        case '9':
                        {
                            tempArchiveName.setCharAt(extensionIndex,'0');
                            break;
                        }

                        default:
                        {
                            tempArchiveName.setCharAt(extensionIndex, (char)(workfileName.charAt(extensionIndex) + 1));
                            break;
                        }
                    }
                }
            }
        }
        else
        {
            // Append the ".___" to the workfile name to create the archive name.
            tempArchiveName.append(".___");
        }
        
        String returnString = new String(tempArchiveName);
        if (pathSeparatorIndex != -1)
        {
            returnString = returnString.substring(pathSeparatorIndex + 1);
        }

        return returnString;
    }

    static String convertWorkfileNameToShortWorkfileName(String workfileName)
    {
        String retVal = workfileName;
        byte[] bytes = workfileName.getBytes();
        int index = -1;
        
        // We cannot use File.separator since the client may not use the
        // same separator as the server.
        for (int i = bytes.length - 1; i >= 0; i--)
        {
            if (bytes[i] == '/' || bytes[i] == '\\')
            {
                index = i;
                break;
            }
        }
        if (index != -1)
        {
            retVal = workfileName.substring(index + 1);
        }
        return retVal;
    }
    
    /**
     * open the archive file.
     */
    synchronized boolean open()
    {
        if (!m_isOpen)
        {
            // Make sure the file exists
            if (m_File.exists())
            {
                try
                {
            	    m_inStream = new RandomAccessFile(m_File, "r");
                    m_isOpen = true;
                }
                catch (FileNotFoundException e)
                {

                    m_isOpen = false;
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    m_isOpen = false;
                    e.printStackTrace();
                }
            }
        }
        return m_isOpen;
    }
    
    protected synchronized void close()
    {
        if (m_isOpen)
        {
            try
            {
                m_inStream.close();
                m_isOpen = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    boolean reReadInformation()
    {
        m_isHeaderInfoRead = false;
        m_isRevisionInfoRead = false;
        m_LogfileInfo = null;
        return readInformation();
    }
    
    boolean readInformation()
    {
        boolean bRetValue = true;
        try
        {
            if (open())
            {
                bRetValue = readHeaderInformation();
                if (bRetValue)
                {
                    bRetValue = readRevisionInformation();
                }
            }
        }
        catch (Exception e)
        {
            bRetValue = false;
        }
        finally
        {
            close();
        }
        return bRetValue;
    }

    /**
     * read revision information for this archive
     */
    private boolean readRevisionInformation()
    {
        try
        {
            if (open() && !m_isHeaderInfoRead)
            {
                m_HeaderInfo = new LogFileHeaderInfo();
                m_HeaderInfo.read(m_inStream);
            }
            if (m_isHeaderInfoRead && !m_isRevisionInfoRead)
            {
                m_RevisionInfo = new RevisionInformation(m_HeaderInfo.getRevisionCount(), 
                                                         new AccessList(m_HeaderInfo.getAccessList()),
                                                         new AccessList(m_HeaderInfo.getModifierList()));
                m_RevisionInfo.read(m_inStream);
                m_isRevisionInfoRead = true;
            }
        }
        catch (Exception e)
        {
            m_isRevisionInfoRead = false;
        }
        return m_isRevisionInfoRead;
    }

    /**
     * read header information for this archive
     */
    private boolean readHeaderInformation()
    {
        try
        {
            if (open() && !m_isHeaderInfoRead)
            {
                m_HeaderInfo = new LogFileHeaderInfo();
                m_isHeaderInfoRead = m_HeaderInfo.read(m_inStream);
                if (m_isHeaderInfoRead)
                {
                    m_AccessList = new AccessList(m_HeaderInfo.getAccessList());
                    m_ModifierList = new AccessList(m_HeaderInfo.getModifierList());
                }
            }
        }
        catch (Exception e)
        {
            m_isHeaderInfoRead = false;
        }
        return m_isHeaderInfoRead;
    }
    
    boolean getTipRevision(String fetchToFileName) throws QVCSException
    {
        LogFileOperationGetRevisionCommandArgs commandArgs = new LogFileOperationGetRevisionCommandArgs();
        commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
        commandArgs.setShortWorkfileName(getShortWorkfileName());
        commandArgs.setOutputFileName(fetchToFileName);

        boolean retVal = false;
        Object[] args = new Object[3];
        args[0] = this;
        args[1] = fetchToFileName;
        args[2] = commandArgs;

        return retVal;
    }
    
    LogFileHeaderInfo getLogFileHeaderInfo() 
    {
         if (!isArchiveInformationRead())
        {
            readInformation();
        }
        return m_HeaderInfo;
    }
    
    void setHeaderInfo(LogFileHeaderInfo headerInfo) throws QVCSException
    {
        if (isArchiveInformationRead())
        {
            throw new QVCSException("Invalid use of setHeaderInfo method!!");
        }
        else
        {
            m_HeaderInfo = headerInfo;
            m_MustReadArchiveFile = false;
        }
    }
    
    boolean createArchive(LogFileOperationCreateArchiveCommandArgs commandLineArgs, AbstractProjectProperties projectProperties, String filename) throws QVCSException
    {
        boolean retVal = false;
        Object[] args = new Object[4];
        args[0] = this;
        args[1] = filename;
        args[2] = projectProperties;
        args[3] = commandLineArgs;

        return retVal;
   }
    
    /**
     * Get a revision from the archive file and write it into the
     * filename provided.  Return true if successful, false otherwise.
     * Keywords are NOT expanded by this method.
     *
     */
    boolean getRevision(LogFileOperationGetRevisionCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException
    {
        boolean retVal = false;
        Object[] args = new Object[3];
        args[0] = this;
        args[1] = fetchToFileName;
        args[2] = commandLineArgs;

        return retVal;
    }

    boolean checkOutRevision(LogFileOperationCheckOutCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException
    {
        boolean retVal = false;
        Object[] args = new Object[3];
        args[0] = this;
        args[1] = fetchToFileName;
        args[2] = commandLineArgs;
        
        return retVal;
    }
    
    boolean lockRevision(LogFileOperationLockRevisionCommandArgs commandLineArgs) throws QVCSException
    {
        boolean retVal = false;
        Object[] args = new Object[2];
        args[0] = this;
        args[1] = commandLineArgs;
        
        return retVal;
    }
    
    boolean checkInRevision(LogFileOperationCheckInCommandArgs commandLineArgs, String filename) throws QVCSException
    {
        boolean retVal = false;
        Object[] args = new Object[3];
        args[0] = this;
        args[1] = filename;
        args[2] = commandLineArgs;

        return retVal;
    }
    
    boolean unlockRevision(LogFileOperationUnlockRevisionCommandArgs commandLineArgs) throws QVCSException
    {
        boolean retVal = false;
        Object[] args = new Object[2];
        args[0] = this;
        args[1] = commandLineArgs;
        
        return retVal;
    }

    boolean unlockRevision(String userName, MutableString revisionString) throws QVCSException
    {
        boolean retVal = false;
        
        // Make sure user is on the access list.
        if (!isOnAccessList(userName))
        {
            throw new QVCSException(userName + " is not on the access list for " + getShortWorkfileName());
        }
        
        // Figure out the default revision string if we need to.
        if (0 == revisionString.getValue().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION))
        {
            revisionString.setValue(getDefaultRevisionHeader().getRevisionString());
        }

        // Make sure the revision is already locked.
        MutableInteger revisionIndex = new MutableInteger();
        if (!isRevisionLocked(revisionString, revisionIndex))
        {
        	throw new QVCSException("Revision " + revisionString + " is not locked for " + getShortWorkfileName());
        }

        // Make sure the current user holds the lock on the given revision.
        if (m_ModifierList.userToIndex(userName) != getRevisionHeader(revisionIndex.getValue()).getLockerIndex())
        {
        	throw new QVCSException("Revision " + revisionString + " for " + getShortWorkfileName() + " is not locked by " + userName);
        }
        
        // Make a copy of the archive.  We'll operate on this copy.
        if (false == createCopyOfArchive())
        {
        	throw new QVCSException("Unable to create temporary copy of archive for " + getShortWorkfileName());
        }
        
        java.io.RandomAccessFile ioStream = null;
        try
        {
            ioStream = new java.io.RandomAccessFile(m_TempFile, "rw");
            
            // Seek to the location of this revision in the file.
    		RevisionHeader revInfo = getRevisionHeader(revisionIndex.getValue());
            ioStream.seek(revInfo.getRevisionStartPosition());
            
            // Update the header with info about this locker.
            getLogFileHeaderInfo().getLogFileHeader().decrementLockCount();
            
            // Update the header information in the stream.
            getLogFileHeaderInfo().updateInPlace(ioStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            retVal = false;
        }
        finally
        {
            try
            {
                if (ioStream != null)
                {
                    ioStream.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                retVal = false;
            }
        }
        
        // Remove any old archives.
        m_OldFile.delete();
        if (m_File.renameTo(m_OldFile))
        {
            if (m_TempFile.renameTo(m_File))
            {
                retVal = true;
                m_OldFile.delete();
            }
            else
            {
                m_OldFile.renameTo(m_File);
            	throw new QVCSException("Unable to rename temporary copy of archive for " + getShortWorkfileName());
            }
        }
        else
        {
            throw new QVCSException("Unable to rename archive file for " + getShortWorkfileName());
        }
        return retVal;
    }

    AccessList getAccessList()
    {
         if (!isArchiveInformationRead())
        {
            readInformation();
        }
        return m_AccessList;
    }
    
    AccessList getModifierList()
    {
         if (!isArchiveInformationRead())
        {
            readInformation();
        }
        return m_ModifierList;
    }
    
    boolean isOnAccessList(String userName)
    {
        return false;
    }
    
    boolean isRevisionLocked (MutableString revisionString, MutableInteger revisionIndex) throws QVCSException
    {
    	boolean retVal = true;

        // Figure out the default revision string if we need to.
        if (0 == revisionString.getValue().compareTo(QVCSConstants.QVCS_DEFAULT_REVISION))
        {
            revisionString.setValue(getDefaultRevisionHeader().getRevisionString());
        }

        if (findRevision(revisionString.getValue(), revisionIndex))
    	{
    		RevisionHeader revInfo = getRevisionHeader(revisionIndex.getValue());
    		retVal = revInfo.isLocked();
    	}
    	else
    	{
    		throw new QVCSException("Revision " + revisionString.getValue() + " not found in " + getShortWorkfileName());
    	}
    	return retVal;
    }
    
    String getLockedRevisionString(String userName)
    {
        String retVal = null;
        RevisionInformation revisionInformation = getRevisionInformation();
        AccessList modifierList = new AccessList("Ralph,Joe");
        int revisionCount = getLogFileHeaderInfo().getRevisionCount();
        for (int i = 0; i < revisionCount; i++)
        {
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
            if (revHeader.isLocked())
            {
                String lockerName = modifierList.indexToUser(revHeader.getLockerIndex());
                if (0 == lockerName.compareTo(userName))
                {
                    retVal = revHeader.getRevisionString();
                    break;
                }
            }
        }
        return retVal;
    }
    
	/** 
     * Determine whether the given user holds any locks for this archive.
	 */
    boolean isLockedByUser (String userName)
    {
    	boolean retVal = false;

        if (!isArchiveInformationRead())
        {
            readInformation();
        }
        
        if (isArchiveInformationRead())
        {
            if (m_HeaderInfo.getLogFileHeader().lockCount() > 0)
            {
                int revisionCount = m_HeaderInfo.getRevisionCount();
                int lockCount = m_HeaderInfo.getLogFileHeader().lockCount();
                for (int i = 0, j = 0; (i < revisionCount) && (j < lockCount); i++)
                {
                    RevisionHeader revHeader = m_RevisionInfo.getRevisionHeader(i);
                    if (revHeader.isLocked())
                    {
                    	j++;
                    	if (0 == userName.compareTo(indexToUsername(revHeader.getLockerIndex())))
                    	{
                    		retVal = true;
                    		break;
                    	}
                    }
                }
            }
        }
        return retVal;
   }
    
    boolean createCopyOfArchive()
    {
        boolean retVal = false;
        try
        {
            // Make sure the temp file is gone.
            m_TempFile.delete();
            
            // Copy the archive to the tempfile.
            retVal = copyFile(m_File, m_TempFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return retVal;
    }
    
    private boolean copyFile(java.io.File fromFile, java.io.File toFile)
    {
        boolean retVal = false;
        java.io.BufferedInputStream     inStream  = null;
        java.io.BufferedOutputStream    outStream = null;
        
        // We can only copy a file if it exists.
        if (fromFile.exists())
        {
            try
            {
                inStream  = new java.io.BufferedInputStream (new java.io.FileInputStream (fromFile));
                outStream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(toFile));

                if (fromFile.length() > 0)
                {
                    byte[] readBuffer = new byte[FILE_COPY_BUFFER_SIZE];
                    
                    while (true)
                    {
                        int byteCount = inStream.read(readBuffer);
                        if (byteCount > 0)
                        {
                            outStream.write(readBuffer, 0, byteCount);
                        }
                        else
                        {
                            break;
                        }
                    }
                    readBuffer = null;
                    retVal = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                retVal = false;
            }
            finally
            {
                try
                {
                    if (inStream != null)
                    {
                        inStream.close();
                    }
                    if (outStream != null)
                    {
                        outStream.close();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    retVal = false;
                }
            }
        }
        return retVal;
    }
    
    ArchiveAttributes getAttributes()
    {
        if (m_MustReadArchiveFile && !isArchiveInformationRead())
        {
            readInformation();
        }

       return m_HeaderInfo.getLogFileHeader().attributes();
    }
}

