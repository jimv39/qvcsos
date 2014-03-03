//     $Log$
//
//     $Log3$
//
// $Filename$
//  $Logfile$
//   $Author$
//    $Owner$
//     $Date$
// $Revision$
//  $Version$
//    $Label$
//   $Header$
//      $VER$
//
// $Copyright$

#include "stdafx.h"
#include <ctype.h>
#include <time.h>
#include <string.h>
#include <mem.h>
#include <qvcs.h>
#include <qvcsMsg.h>
#include <afx.h>
#include "qTime.h"
#include "qProject.h"
#include "qPrjSet.h"
#include <get.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/*
 * The one extern needed here
 */
char    *pcgWorkFileName    = NULL; /* Used by put to give us the name of the workfile */

/*
 * Local routine templates
 */
static int locBuildRevList
(
    FILE                        *ptLogFile,
    struct qvcsRevList          *psRevList,
    struct qvcsRevListHead      *psRevListHead,
    struct qvcsCommandRevDesc   *psFetchDesc
);


static void locConvertToUnixPathSeparators
(
    char    *pcStringToConvert
);


static int locDecideToShow
(
    struct qvcsCommandRevDesc   *psCurrentDesc,     /* the revision we're testing */
    struct qvcsCommandRevDesc   *psFetchDesc        /* the revision we're fetching */
);


static int locExpandCopyright
(
    char    *pcBuf,
    FILE    *ptOutFile,
    char    *pcCopyrightMsg,
    int     iBinaryFile
);


static void locExpandDescription
(
    char                        *pcDescription,
    struct qvcsRevisionInfo     *psRevInfo,
    char                        *pcRevDesc,
    FILE                        *ptOutFile,
    char                        *pcAccessList,
    char                        *pcCommentPrefix,
    struct getCommandArgs       *psCommandArgs
);


static int locExpandKeyWords
(
    char                        *pcLineBuf,
    struct qvcsRevisionInfo     *psRevInfo,
    struct getCommandArgs       *psCommandArgs,
    struct qvcsCommandRevDesc   *psFetchDesc,
    FILE                        *ptLogFile,
    long                        lRevStart,
    long                        lAllRevsStart,
    FILE                        *ptOutFile,
    struct qvcsHeaderInfo       *psHeaderInfo
);

static void locExpandLog
(
    FILE                        *ptLogFile,
    struct qvcsCommandRevDesc   *psFetchDesc,
    long                        lRevStart,
    long                        lAllRevsStart,
    FILE                        *ptOutFile,
    char                        *pcCommentPrefix,
    struct qvcsHeaderInfo       *psHeaderInfo,
    int                         iRevCount,
    struct getCommandArgs       *psCommandArgs
);


static int locExpandWord
(
    char    *pcLineBuf,
    FILE    *ptOutFile,
    char    *pcInsert,
    int     iBinaryFileFlag
);


/*
 *  g e t E x p a n d K e y W o r d s
 */
void getExpandKeyWords
(
    struct qvcsRevisionInfo     *psRevInfo,
    char                        *pcFileName,
    struct  getCommandArgs      *psCommandArgs,
    FILE                        *ptLogFile,
    long                        lRevStart,
    long                        lAllRevsStart,
    struct qvcsCommandRevDesc   *psFetchDesc,
    struct qvcsHeaderInfo       *psHeaderInfo
)
{
    FILE    *ptOutFile;             /* Expanded file */
    FILE    *ptInFile;              /* File to be expanded */
    long    lBufSize;               /* Size of buffer we read into */
    long    l;                      /* A long counter */
    long    lCount;                 /* Number of bytes to write */
    char    *pcLineBuf;             /* Read into here */
    char    *pc;
    char    *pcStart;               /* The start of a buffer we write */
    char    pcTmpFileNameBuf[qvcsFILENAME_SIZE];

    /*
     * Try to come up with a unique name for this guy yet another temporary
     * file that we expand stuff into.
     */
    if ((libCreateTempFileName (pcTmpFileNameBuf,
                                psCommandArgs->psEnvironVars)) != qvcsSUCCESS)
    {
       return;
    }

    /*
     * Open expanded file.
     */
    if ((ptOutFile = fopen (pcTmpFileNameBuf, "wb")) == NULL)
    {
        return;
    }

    /*
     * Open input file. (file to be expanded).
     */
    if ((ptInFile = fopen (pcFileName, "rb")) == NULL)
    {
        fclose (ptOutFile);
        remove (pcTmpFileNameBuf);
        return;
    }

    /*
     * Find out how big the input file is, so we can allocate a worst
     * case buffer for it (a buffer as big as the file itself).
     */
    fseek (ptInFile, 0L, SEEK_END);
    lBufSize = ftell (ptInFile);
    fseek (ptInFile, 0L, SEEK_SET);

    /*
     * Allocate our buffer that we read into.
     */
    if ((pcLineBuf = (char*)mem_malloc (lBufSize + 1)) == NULL)
    {
        fclose (ptOutFile);
        fclose (ptInFile);
        remove (pcTmpFileNameBuf);
        return;
    }

	/*
	 * NULL terminate the buffer we read into.  This guards against
	 * a possible memory protect that can occur in sscanf, since
	 * sscanf takes a NULL terminated string as an argument.
	 */
	pcLineBuf[lBufSize] = '\0';

    /*
     * Read the input file into the buffer we just alloced
     */
    if ((fread (pcLineBuf, (size_t) lBufSize, 1, ptInFile)) != 1)
    {
        mem_free (pcLineBuf);
        fclose (ptOutFile);
        fclose (ptInFile);
        remove (pcTmpFileNameBuf);
        return;
    }

    /*
     * Copy the buffer to the output file, expanding keywords as we find
     * them.
     */
    for (l = 0L, pc = pcLineBuf, pcStart = pcLineBuf, lCount = 1L; l < lBufSize;
         l++, pc++, lCount++)
    {
        /*
         * Check for the keyword prefix character
         */
        if (*pc == qvcsKEYWORD_PREFIX)
        {
            /*
             * Write what we've looked at so far (including the keyword prefix)
             */
            if ((fwrite (pcStart, (size_t) lCount, 1, ptOutFile)) != 1)
            {
                mem_free (pcLineBuf);
                fclose (ptOutFile);
                fclose (ptInFile);
                remove (pcTmpFileNameBuf);
                return;
            }
            else
            {
                lCount = 0L;
            }

            if (locExpandKeyWords (pc,
                                   psRevInfo,
                                   psCommandArgs,
                                   psFetchDesc,
                                   ptLogFile,
                                   lRevStart,
                                   lAllRevsStart,
                                   ptOutFile,
                                   psHeaderInfo) == TRUE)
            {
                /*
                 * We found a keyword.  locExpandKeyWords expanded it for us,
                 * and wrote it out.  Look for the terminating character so
                 * we can start copying the rest of the file from there.
                 */
                do
                {
                    pc++;
                    l++;
                } while (*pc != qvcsKEYWORD_PREFIX);
            }
            pcStart = &pc[1];
        }
    }

    /*
     * Write out what's left of the file.
     */
    if (lCount > 1L)
    {
        if ((fwrite (pcStart, (size_t) (lCount - 1L), 1, ptOutFile)) != 1)
        {
            fclose (ptOutFile);
            fclose (ptInFile);
            mem_free (pcLineBuf);
            remove (pcTmpFileNameBuf);
            return;
        }
    }

    mem_free (pcLineBuf);
    fclose (ptInFile);
    fclose (ptOutFile);
    remove (pcFileName);
    rename (pcTmpFileNameBuf, pcFileName);
    return;
}

/*
 *  l o c B u i l d R e v L i s t
 *
 * Build a list of revisions that are appropriate for the psFetchDesc revision.
 * Return the number of revisions that we'll need to expand.
 */
static int locBuildRevList
(
    FILE                        *ptLogFile,
    struct qvcsRevList          *psRevList,
    struct qvcsRevListHead      *psRevListHead,
    struct qvcsCommandRevDesc   *psFetchDesc
)
{
    int                         iCount = 0;
    long                        lRevStart;
    struct qvcsCommandRevDesc   *psRevDesc = NULL;
    struct qvcsRevisionInfo     sRevInfo;

    for (iCount = 0 ;;)
    {
        lRevStart = ftell (ptLogFile);
        if ((fread (&sRevInfo, sizeof (sRevInfo), 1, ptLogFile)) != 1)
        {
            break;
        }
        else
        {
            libConstructEffectiveRevision (&psRevDesc, &sRevInfo);

            /*
             * Decide whether this revision should show up in the log
             */
            if (locDecideToShow (psRevDesc, psFetchDesc))
            {
                if (iCount == 0)
                {
                    psRevListHead->iFirstIndex = iCount;
                    psRevListHead->iLastIndex  = iCount;
                    psRevList[iCount].iNextIndex = -1;
                    psRevList[iCount].lRevSeek   = lRevStart;
                }
                else
                {
                    if (sRevInfo.siDepthCount == 0)
                    {
                        /*
                         * Trunks get added to the end.
                         */
                        psRevList[psRevListHead->iLastIndex].iNextIndex = iCount;
                        psRevListHead->iLastIndex = iCount;
                        psRevList[iCount].iNextIndex = -1;
                        psRevList[iCount].lRevSeek   = lRevStart;
                    }
                    else
                    {
                        /*
                         * Branches get added to the beginning.
                         */
                        psRevList[iCount].iNextIndex = psRevListHead->iFirstIndex;
                        psRevListHead->iFirstIndex = iCount;
                        psRevList[iCount].lRevSeek = lRevStart;
                    }
                }
                libFormatRevision (psRevList[iCount].pcRevDesc, psRevDesc);
                iCount++;
            }

            /*
             * Now seek to the next qvcsRevisionInfo structure.
             */
            fseek (ptLogFile, (long) sRevInfo.iDescSize, SEEK_CUR);
            if ((fseek (ptLogFile, sRevInfo.lRevSize, SEEK_CUR)) != 0)
            {
                sprintf (pcgFmtBuf, libMsgGet (genSEEK_ERROR),
                         pcgProgramName, __FILE__, __LINE__);
                libDisplayMsg (pcgFmtBuf, qvcsERROR_MSG_CLASS);
                break;
            }
        }
    }
    if (psRevDesc)  mem_free (psRevDesc);
    return (iCount);
}

/*
 *  l o c D e c i d e T o S h o w
 *
 * Decide whether to show a given revision in the log keyword expansion.
 * Return TRUE if the revision should be shown, FALSE if not.
 */
static int locDecideToShow
(
    struct qvcsCommandRevDesc   *psCurrentDesc,     /* the revision we're testing */
    struct qvcsCommandRevDesc   *psFetchDesc        /* the revision we're fetching */
)
{
    int     iRetVal = FALSE;

    if (psFetchDesc->iElementCount == 1)
    {
        /*
         * We're interested in a revision that's on the TRUNK.
         */
        if (psCurrentDesc->iElementCount == 1)
        {
            char fmtFetchBuf[130];
            char fmtCurrentBuf[130];
            sprintf (fmtFetchBuf,   "%04d%04d", psFetchDesc->sRevDesc[0].iMajorNumber,   psFetchDesc->sRevDesc[0].iMinorNumber);
            sprintf (fmtCurrentBuf, "%04d%04d", psCurrentDesc->sRevDesc[0].iMajorNumber, psCurrentDesc->sRevDesc[0].iMinorNumber);
            if (strcmp(fmtFetchBuf, fmtCurrentBuf) >= 0)
            {
                iRetVal = TRUE;
            }
        }
    }
    else
    {
        /*
         * We're interested in a revision that's on a BRANCH.
         */
        if (psCurrentDesc->iElementCount == 1)
        {
            /*
             * The current revision is on the TRUNK.  See if it's
             * one that preceeds the revision that we're "fetching".
             */
            char fmtFetchBuf[130];
            char fmtCurrentBuf[130];
            sprintf (fmtFetchBuf,   "%04d%04d", psFetchDesc->sRevDesc[0].iMajorNumber,   psFetchDesc->sRevDesc[0].iMinorNumber);
            sprintf (fmtCurrentBuf, "%04d%04d", psCurrentDesc->sRevDesc[0].iMajorNumber, psCurrentDesc->sRevDesc[0].iMinorNumber);
            if (strcmp(fmtFetchBuf, fmtCurrentBuf) >= 0)
            {
                iRetVal = TRUE;
            }
        }
        else
        {
            int     i;
            if (psCurrentDesc->iElementCount <= psFetchDesc->iElementCount)
            {
                /*
                 * Current revision is closer to trunk or at same depth as the
                 * requested revision.  Make sure it's on the same branch or on
                 * the trunk before the requested revision.
                 */
                iRetVal = TRUE;
                /*
                 * All segments before the last must match exactly
                 */
                for (i = 0; i < (psCurrentDesc->iElementCount - 1); i++)
                {
                    if ((psCurrentDesc->sRevDesc[i].iMajorNumber !=
                         psFetchDesc->sRevDesc[i].iMajorNumber) ||
                        (psCurrentDesc->sRevDesc[i].iMinorNumber !=
                         psFetchDesc->sRevDesc[i].iMinorNumber))
                    {
                        iRetVal = FALSE;
                        break;
                    }
                }

                /*
                 * The last segment, the major numbers must match, and the minor
                 * number must be less than or equal to the fetched revision.
                 */
                if (iRetVal == TRUE)
                {
                    if ((psCurrentDesc->sRevDesc[i].iMajorNumber !=
                        psFetchDesc->sRevDesc[i].iMajorNumber) ||
                        (psCurrentDesc->sRevDesc[i].iMinorNumber >
                        psFetchDesc->sRevDesc[i].iMinorNumber))
                    {
                        iRetVal = FALSE;
                    }
                }
            }
        }
    }

    return iRetVal;
}

/*
 *  l o c E x p a n d K e y W o r d s
 *
 * Check for the presence of keywords.  If we find one, expand it and write
 * it to the output file and return TRUE.  If we don't find one, return FALSE.
 * When called, the buffer pointer points to the qvcsKEYWORD_PREFIX character.
 * This routine doesn't write that character out, but writes all expanded
 * characters after that (including the ending qvcsKEYWORD_PREFIX character).
 */
static int locExpandKeyWords
(
    char                        *pcBuf,
    struct qvcsRevisionInfo     *psRevInfo,
    struct getCommandArgs       *psCommandArgs,
    struct qvcsCommandRevDesc   *psFetchDesc,
    FILE                        *ptLogFile,
    long                        lRevStart,
    long                        lAllRevsStart,
    FILE                        *ptOutFile,
    struct qvcsHeaderInfo       *psHeaderInfo
)
{
    static  char    pcExpandPath[_MAX_PATH];
    int             iRetVal = TRUE;
    int             iRevCount;
    char            *pc;
    char            *pc1;
    char            pcFmtBuf [(4 * qvcsMAX_BRANCH_DEPTH) + 2];
    char            pcFmtBuf1[(4 * qvcsMAX_BRANCH_DEPTH) + 2];
    int             iBinaryFile = FALSE;
    char            cSearchTerminator = qvcsKEYWORD_PREFIX;

    pc = pcBuf;
    pc++;

    if (psHeaderInfo->sDifFileHdr.iAttributes & qvcsBINARYFILE_BIT)
    {
        iBinaryFile = TRUE;
        cSearchTerminator = qvcsKEYWORD_EXPREFIX;
    }

    /*
     * Pointing to the keyword candidate... See if it's a keyword we know.
     */
    if ((strncmp (pc, libMsgGet (keyVERSION), strlen (libMsgGet (keyVERSION))) == 0) &&
        (pc [strlen (libMsgGet (keyVERSION))] == cSearchTerminator))
    {
        iRetVal = locExpandWord (pc, ptOutFile, psCommandArgs->pcVersion, iBinaryFile);
    }
    else if ((strncmp (pc, libMsgGet (keyAUTHOR), strlen (libMsgGet (keyAUTHOR))) == 0) &&
        (pc [strlen (libMsgGet (keyAUTHOR))] == cSearchTerminator))
    {
        char    *pcLocAccessList;
        pcLocAccessList = (char*)mem_malloc (strlen (psHeaderInfo->pcModifierList) + 1);
        strcpy (pcLocAccessList, psHeaderInfo->pcModifierList);

        iRetVal = locExpandWord (pc, ptOutFile,
                       libConvertAccessIndex (pcLocAccessList, psRevInfo->iCreatorIndex), iBinaryFile);
        mem_free (pcLocAccessList);
    }
    else if ((strncmp (pc, libMsgGet (keyREVISION), strlen (libMsgGet (keyREVISION))) == 0) &&
             (pc [strlen (libMsgGet (keyREVISION))] == cSearchTerminator))
    {
        libFormatRevision (pcFmtBuf, psFetchDesc);
        iRetVal = locExpandWord (pc, ptOutFile, pcFmtBuf, iBinaryFile);
    }
    else if ((strncmp (pc, libMsgGet (keyDATE), strlen (libMsgGet (keyDATE))) == 0) &&
             (pc [strlen (libMsgGet (keyDATE))] == cSearchTerminator))
    {
		CQTime lastEditTime(psRevInfo->tFileDate);
		CString lastEditString(lastEditTime.LongDateTimeFormatForCurrentLocale(psCommandArgs->psEnvironVars));
        strcpy (pcFmtBuf, LPCTSTR(lastEditString));
        iRetVal = locExpandWord (pc, ptOutFile, pcFmtBuf, iBinaryFile);
    }
    else if ((strncmp (pc, libMsgGet (keyOWNER), strlen (libMsgGet (keyOWNER))) == 0) &&
             (pc [strlen (libMsgGet (keyOWNER))] == cSearchTerminator))
    {
        iRetVal = locExpandWord (pc, ptOutFile, psHeaderInfo->pcOwner, iBinaryFile);
    }
    else if ((strncmp (pc, libMsgGet (keyHEADER), strlen (libMsgGet (keyHEADER))) == 0) &&
             (pc [strlen (libMsgGet (keyHEADER))] == cSearchTerminator))
    {
		char* pcFullWorkfileName;
		char* pcShowWorkfileName;
        libFormatRevision (pcFmtBuf, psFetchDesc);
		CQTime lastEditTime(psRevInfo->tFileDate);
		CString lastEditString(lastEditTime.LongDateTimeFormatForCurrentLocale(psCommandArgs->psEnvironVars));
        strcpy (pcFmtBuf1, LPCTSTR(lastEditString));

		/* Show just the last portion of the workfile name */
		pcFullWorkfileName = pcgWorkFileName == NULL ? psCommandArgs->pcOutFileName : pcgWorkFileName;
		pcShowWorkfileName = strrchr(pcFullWorkfileName, '\\');
		if (pcShowWorkfileName)
		{
			pcShowWorkfileName++;
		}
		else
		{
			pcShowWorkfileName = pcFullWorkfileName;
		}

        sprintf (pcExpandPath, "%s  %s:%s  %s  %s",
                 pcShowWorkfileName,
                 libMsgGet (keyREVISION),
                 pcFmtBuf,
                 pcFmtBuf1,
                 psHeaderInfo->pcOwner);
        iRetVal = locExpandWord (pc, ptOutFile, pcExpandPath, iBinaryFile);
    }
    else if ((strncmp (pc, libMsgGet (keyLOGFILE), strlen (libMsgGet (keyLOGFILE))) == 0) &&
             (pc [strlen (libMsgGet (keyLOGFILE))] == cSearchTerminator))
    {
        if (libExpandPath (psCommandArgs->pcLogFileName, pcExpandPath)
                                                    == qvcsSUCCESS)
        {
            if (psCommandArgs->psEnvironVars->iUseUnixPathSeparator)
            {
                locConvertToUnixPathSeparators(pcExpandPath);
            }
            iRetVal = locExpandWord (pc, ptOutFile, pcExpandPath, iBinaryFile);
        }
        else
        {
            if (psCommandArgs->psEnvironVars->iUseUnixPathSeparator)
            {
                locConvertToUnixPathSeparators(psCommandArgs->pcLogFileName);
            }
            iRetVal = locExpandWord (pc, ptOutFile, psCommandArgs->pcLogFileName, iBinaryFile);
        }
    }
    else if ((strncmp (pc, libMsgGet (keyLOG), strlen (libMsgGet (keyLOG))) == 0) &&
             (pc [strlen (libMsgGet (keyLOG))] == qvcsKEYWORD_PREFIX) &&
             (iBinaryFile == FALSE))
    {
        if (libExpandPath (psCommandArgs->pcLogFileName, pcExpandPath)
                                                    == qvcsSUCCESS)
        {
            if (psCommandArgs->psEnvironVars->iUseUnixPathSeparator)
            {
                locConvertToUnixPathSeparators(pcExpandPath);
            }
            iRetVal = locExpandWord (pc, ptOutFile, pcExpandPath, iBinaryFile);
        }
        else
        {
            if (psCommandArgs->psEnvironVars->iUseUnixPathSeparator)
            {
                locConvertToUnixPathSeparators(psCommandArgs->pcLogFileName);
            }
            iRetVal = locExpandWord (pc, ptOutFile, psCommandArgs->pcLogFileName, iBinaryFile);
        }
        iRevCount = psHeaderInfo->sDifFileHdr.iRevisionCount;
        locExpandLog (ptLogFile,
                      psFetchDesc,
                      lRevStart,
                      lAllRevsStart,
                      ptOutFile,
                      psCommandArgs->pcCommentPrefix,
                      psHeaderInfo,
                      iRevCount,
                      psCommandArgs);
        fprintf (ptOutFile, "%s$%s$",
                      psCommandArgs->pcCommentPrefix,
                      libMsgGet (keyENDLOG));
    }
    else if (strncmp (pc, libMsgGet (keyLOG), strlen (libMsgGet (keyLOG))) == 0)
    {
        /*
         * Make sure we have just numbers between the keyword and the
         * qvcsKEYWORD_PREFIX character.
         */
        for (pc1 = &pc[strlen (libMsgGet (keyLOG))]; isdigit (*pc1); pc1++)
        {
            ;
        }
        if ((*pc1 == qvcsKEYWORD_PREFIX) && (iBinaryFile == FALSE))
        {
            if (libExpandPath (psCommandArgs->pcLogFileName, pcExpandPath)
                                                        == qvcsSUCCESS)
            {
                if (psCommandArgs->psEnvironVars->iUseUnixPathSeparator)
                {
                    locConvertToUnixPathSeparators(pcExpandPath);
                }
                iRetVal = locExpandWord (pc, ptOutFile, pcExpandPath, iBinaryFile);
            }
            else
            {
                if (psCommandArgs->psEnvironVars->iUseUnixPathSeparator)
                {
                    locConvertToUnixPathSeparators(psCommandArgs->pcLogFileName);
                }
                iRetVal = locExpandWord (pc, ptOutFile, psCommandArgs->pcLogFileName, iBinaryFile);
            }
            iRevCount = psHeaderInfo->sDifFileHdr.iRevisionCount;
            sscanf (&pc [strlen (libMsgGet (keyLOG))], "%d", &iRevCount);
            locExpandLog (ptLogFile,
                          psFetchDesc,
                          lRevStart,
                          lAllRevsStart,
                          ptOutFile,
                          psCommandArgs->pcCommentPrefix,
                          psHeaderInfo,
                          iRevCount,
                          psCommandArgs);
            fprintf (ptOutFile, "%s$%s$",
                          psCommandArgs->pcCommentPrefix,
                          libMsgGet (keyENDLOG));
        }
        else
        {
            iRetVal = FALSE;
        }
    }
    else if ((strncmp (pc, libMsgGet (keyFILENAME), strlen (libMsgGet (keyFILENAME))) == 0) &&
             (pc [strlen (libMsgGet (keyFILENAME))] == cSearchTerminator))
    {
        if (pcgWorkFileName != NULL)
        {
            if (psCommandArgs->psEnvironVars->iUseUnixPathSeparator)
            {
                locConvertToUnixPathSeparators(pcgWorkFileName);
            }
            iRetVal = locExpandWord (pc, ptOutFile, pcgWorkFileName, iBinaryFile);
        }
        else
        {
            if (psCommandArgs->psEnvironVars->iUseUnixPathSeparator)
            {
                locConvertToUnixPathSeparators(psCommandArgs->pcOutFileName);
            }
            iRetVal = locExpandWord (pc, ptOutFile, psCommandArgs->pcOutFileName, iBinaryFile);
        }
    }
    else if ((strncmp (pc, libMsgGet (keyVER), strlen (libMsgGet (keyVER))) == 0) &&
             (pc [strlen (libMsgGet (keyVER))] == cSearchTerminator))
    {
        char    *pcTmpFileName;
        char    *pc1;

        pcTmpFileName = (char*)mem_malloc (strlen (psCommandArgs->pcFileName) + 1);
        if (pcTmpFileName)
        {
            strcpy (pcTmpFileName, psCommandArgs->pcFileName);
            pc1 = strrchr (pcTmpFileName, '.');
            if (pc1)
            {
                *pc1 = '\0';
            }
            pc1 = strrchr (pcTmpFileName, '\\');
            if (pc1)
            {
                pc1++;
            }
            else
            {
                pc1 = pcTmpFileName;
            }

            sprintf (pcExpandPath, "%s %s", pc1, psCommandArgs->pcVersion);
            iRetVal = locExpandWord (pc, ptOutFile, pcExpandPath, iBinaryFile);
            mem_free (pcTmpFileName);
        }
    }
    else if ((strncmp (pc, libMsgGet (keyCOPYRIGHT), strlen (libMsgGet (keyCOPYRIGHT))) == 0) &&
             (pc [strlen (libMsgGet (keyCOPYRIGHT))] == cSearchTerminator))
    {
        char* pcCopyrightMessage = psCommandArgs->psEnvironVars->m_pProject->projectSettings()->getCopyrightString();
        iRetVal = locExpandCopyright (pc, ptOutFile, pcCopyrightMessage, iBinaryFile);
    }
    else
    {
        iRetVal = FALSE;
    }

    return (iRetVal);
}

/*
 *  l o c E x p a n d C o p y r i g h t
 *
 * Expand the copyright keyword by translating the ending '$' to its expanded
 * value.
 */
static int locExpandCopyright
(
    char    *pcBuf,
    FILE    *ptOutFile,
    char    *pcCopyrightMsg,
    int     iBinaryFile
)
{
    char    *pc;
    int     iRetVal = TRUE;

    if (iBinaryFile == FALSE)
    {
        /*
         * Find the ending qvcsKEYWORD_PREFIX character.
         */
        pc = strchr (pcBuf, qvcsKEYWORD_PREFIX);
        *pc = '\0';
        fprintf (ptOutFile, "%s %c %s $", pcBuf, qvcsCOPYRIGHT_EXPREFIX,
                 pcCopyrightMsg);
        *pc = qvcsKEYWORD_PREFIX;
    }
    else
    {
        // Handle copyright expansion for binary files.
        char    *pcTmpBuf = (char*)mem_malloc(strlen(pcCopyrightMsg) + 3);
        sprintf (pcTmpBuf, "%c %s", qvcsCOPYRIGHT_EXPREFIX, pcCopyrightMsg);
        iRetVal = locExpandWord(pcBuf, ptOutFile, pcTmpBuf, iBinaryFile);
        mem_free(pcTmpBuf);
    }

    return iRetVal;
}

/*
 *  l o c E x p a n d W o r d
 *
 * Expand a single keyword by translating the ending '$' to its expanded
 * value.
 */
static int locExpandWord
(
    char    *pcBuf,
    FILE    *ptOutFile,
    char    *pcInsert,
    int     iBinaryFile
)
{
    int     iRetVal = TRUE;
    char    *pc;
    char    *pc1;
    char    *pc2;

    if (iBinaryFile == FALSE)
    {
        /*
         * Find the ending qvcsKEYWORD_PREFIX character.
         */
        pc = strchr (pcBuf, qvcsKEYWORD_PREFIX);
        *pc = '\0';
        fprintf (ptOutFile, "%s: %s $", pcBuf, pcInsert);
        *pc = qvcsKEYWORD_PREFIX;
    }
    else
    {
        /*
         * Expand a keyword within a binary file.  Be careful to use only the space
         * the user has allocated for our use.
         */
        int iInputLength = strlen(pcInsert);
        int iUserDefinedSpaceLength;
        char    *pcUserBuf = NULL;

        /*
         * Look for the ':'
         */
        pc = strchr(pcBuf, qvcsKEYWORD_EXPREFIX);
        *pc = '\0';                                 // pc points to the ':' character
        pc1 = pc;
        pc1++;                                      // pc1 points to the stuff following the keyword
        pc2 = strchr(pc1, qvcsKEYWORD_PREFIX);
        if (pc2)
        {
            *pc2 = '\0';

            // fill the user-defined area with spaces.
            iUserDefinedSpaceLength = strlen(pc1);
            if (iUserDefinedSpaceLength > 1)
            {
                int iBytesToCopy = iInputLength < iUserDefinedSpaceLength - 2 ? iInputLength : iUserDefinedSpaceLength - 2;
                pcUserBuf = (char*)mem_malloc(iUserDefinedSpaceLength);
                memset(pcUserBuf, ' ', iUserDefinedSpaceLength);
                memcpy(&pcUserBuf[1], pcInsert, iBytesToCopy);
                fwrite(pcBuf, strlen(pcBuf), 1, ptOutFile);
                fwrite(":", 1, 1, ptOutFile);
                fwrite(pcUserBuf, iUserDefinedSpaceLength, 1, ptOutFile);
                fwrite("$", 1, 1, ptOutFile);
                mem_free(pcUserBuf);
            }
            else
            {
                // The user didn't provide any space to expand into.  Bail out without
                // expanding anything.
                iRetVal = FALSE;
            }

            *pc2 = qvcsKEYWORD_PREFIX;
        }
        else
        {
            // The user didn't terminate a user area with the '$' character.  Bail out
            // of here without expanding anything.
            iRetVal = FALSE;
        }
        *pc = qvcsKEYWORD_EXPREFIX;
    }

    return iRetVal;
}

/*
 *  l o c E x p a n d L o g
 *
 * Expand the revision info into comment lines in the source code.
 */
static void locExpandLog
(
    FILE                        *ptLogFile,
    struct qvcsCommandRevDesc   *psFetchDesc,
    long                        lRevStart,
    long                        lAllRevsStart,
    FILE                        *ptOutFile,
    char                        *pcCommentPrefix,
    struct qvcsHeaderInfo       *psHeaderInfo,
    int                         iRevCount,
    struct getCommandArgs       *psCommandArgs
)
{
    char                        *pcDescription;
    char                        *pcModDescription;
    char                        *pc;
    int                         iExpandedRevs;
    int                         i;
    struct qvcsRevisionInfo     sRevInfo;
    struct qvcsRevListHead      sRevListHead;
    struct qvcsRevList          *psRevList;
    struct qvcsRevList          *psRevToExpand;
    char                        *pcEOL;

    // Figure out what we'll use for end-of-line.
    if (psCommandArgs->psEnvironVars->iUseUnixEOL)
    {
        pcEOL = "\n";
    }
    else
    {
        pcEOL = "\r\n";
    }

    fprintf (ptOutFile, "%s%s%s", pcEOL, pcCommentPrefix, pcEOL);

    /*
     * Report the Module description
     */
    pcModDescription = psHeaderInfo->pcModDesc;
    while (pc = strchr (pcModDescription, '\n'))
    {
        *pc = '\0';
        fprintf (ptOutFile, "%s  %s%s",
                 pcCommentPrefix,
                 pcModDescription, 
                 pcEOL);
        *pc = '\n';     /* So we don't change the string */
        pc++;
        pcModDescription = pc;
    }
    fprintf (ptOutFile, "%s  %s%s%s%s",
                 pcCommentPrefix,
                 pcModDescription,
                 pcEOL,
                 pcCommentPrefix,
                 pcEOL);

    /*
     * Build a list of revisions to report on.
     */
    sRevListHead.iFirstIndex = -1;
    sRevListHead.iLastIndex  = -1;
    if ((psRevList = (struct qvcsRevList*)mem_calloc (psHeaderInfo->sDifFileHdr.iRevisionCount *
                             sizeof (struct qvcsRevList))) == NULL)
    {
        return;
    }
    fseek (ptLogFile, lAllRevsStart, SEEK_SET);
    iExpandedRevs = locBuildRevList (ptLogFile, psRevList,
                                     &sRevListHead, psFetchDesc);

    /*
     * Report on requested revisions.
     */
    for (i = 0, psRevToExpand = &psRevList[sRevListHead.iFirstIndex];
         (i < iExpandedRevs) && (i < iRevCount);
         i++, psRevToExpand = &psRevList[psRevToExpand->iNextIndex])
    {
        fseek (ptLogFile, psRevToExpand->lRevSeek, SEEK_SET);

        if ((fread (&sRevInfo, sizeof (sRevInfo), 1, ptLogFile)) != 1)
        {
            break;
        }
        else
        {
            pcDescription = (char*)mem_malloc (sRevInfo.iDescSize);
            fread (pcDescription, 1, sRevInfo.iDescSize, ptLogFile);

            locExpandDescription (pcDescription,
                                  &sRevInfo,
                                  psRevToExpand->pcRevDesc,
                                  ptOutFile,
                                  psHeaderInfo->pcModifierList,
                                  pcCommentPrefix,
                                  psCommandArgs);

            mem_free (pcDescription);
        }
    }

    mem_free (psRevList);
    return;
}

/*
 *  l o c E x p a n d D e s c r i p t i o n
 */
static void locExpandDescription
(
    char                        *pcDescription,
    struct qvcsRevisionInfo     *psRevInfo,
    char                        *pcRevDesc,
    FILE                        *ptOutFile,
    char                        *pcModifierList,
    char                        *pcCommentPrefix,
    struct getCommandArgs       *psCommandArgs
)
{
    char    *pcLocModifierList;
    char    *pc;
    char    pcFormattedTime[128];    /* A buffer to fix the \r\n at end */

    char                        *pcEOL;

    // Figure out what we'll use for end-of-line.
    if (psCommandArgs->psEnvironVars->iUseUnixEOL)
    {
        pcEOL = "\n";
    }
    else
    {
        pcEOL = "\r\n";
    }

    /*
     * Make a local copy of the modifier list 'cuz it gets clobbered here
     */
    pcLocModifierList = (char*)mem_malloc (strlen (pcModifierList) + 1);
    strcpy (pcLocModifierList, pcModifierList);

    /*
     * Fix format of the time stamp.
     */
    CQTime revisionCreationTime(psRevInfo->tPutDate);
    CString revisionCreationTimeString(revisionCreationTime.FormatForCurrentLocale(psCommandArgs->psEnvironVars));
    strcpy (pcFormattedTime, LPCTSTR(revisionCreationTimeString));
    strcat (pcFormattedTime, pcEOL);

    fprintf (ptOutFile, libMsgGet (getREV_DESC_MSG),
             pcCommentPrefix,
             pcRevDesc,
             libConvertAccessIndex (pcLocModifierList, psRevInfo->iCreatorIndex),
             pcFormattedTime);

    while (pc = strchr (pcDescription, '\n'))
    {
        *pc = '\0';
        pc++;
        fprintf (ptOutFile, "%s  %s%s",
                 pcCommentPrefix,
                 pcDescription,
                 pcEOL);
        pcDescription = pc;
    }
    fprintf (ptOutFile, "%s  %s%s%s%s",
                 pcCommentPrefix,
                 pcDescription,
                 pcEOL,
                 pcCommentPrefix,
                 pcEOL);

    mem_free (pcLocModifierList);
    return;
}

/*
 *  l o c C o n v e r t T o U n i x P a t h S e p a r a t o r s
 */
static void locConvertToUnixPathSeparators
(
    char    *pcStringToConvert
)
{
    char    *pc;

    // Do the conversion in place.
    for (pc = pcStringToConvert; *pc; pc++)
    {
        if (*pc == '\\')
        {
            *pc = '/';
        }
    }
    return;
}

/* End of File */
