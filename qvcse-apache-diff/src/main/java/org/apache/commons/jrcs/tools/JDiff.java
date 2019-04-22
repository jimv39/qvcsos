/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.commons.jrcs.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.Revision;


/**
 * A program to compare two files.
 * <p>JDiff produces the deltas between the two given files in Unix diff
 * format.
 * </p>
 * <p>The program was written as a simple test of the
 * {@linkplain org.apache.commons.jrcs.diff diff} package.
 */
public class JDiff
{

    static final String[] loadFile(String name) throws IOException
    {
        try (BufferedReader data = new BufferedReader(new FileReader(name))) {
            List lines = new ArrayList();
            String s;
            while ((s = data.readLine()) != null)
            {
                lines.add(s);
            }
            return (String[])lines.toArray(new String[lines.size()]);
        }
    }

    static final void usage(String name)
    {
        System.err.println("Usage: " + name + " file1 file2");
    }

    public static void main(String[] argv) throws Exception
    {
        if (argv.length < 2)
        {
            usage("JDiff");
        }
        else
        {
            Object[] orig = loadFile(argv[0]);
            Object[] rev = loadFile(argv[1]);

            Diff df = new Diff(orig);
            Revision r = df.diff(rev);

            System.err.println("------");
            System.out.print(r.toString());
            System.err.println("------" + new Date());

            try
            {
                Object[] reco = r.patch(orig);
                //String recos = Diff.arrayToString(reco);
                if (!Diff.compare(rev, reco))
                {
                    System.err.println("INTERNAL ERROR:"
                                        + "files differ after patching!");
                }
            }
            catch (Throwable o)
            {
                System.out.println("Patch failed");
            }
        }
    }
}

