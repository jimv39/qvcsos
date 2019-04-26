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
package org.apache.commons.jrcs.diff;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleDiffTest  {

    static final int LARGE=2*1024;

    protected DiffAlgorithm algorithm = new SimpleDiff();
    Object[] empty;
    Object[] original;
    Object[] rev1;
    Object[] rev2;

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
        empty = new Object[]
                         {};
        original = new String[]
                            {
                            "[1] one",
                            "[2] two",
                            "[3] three",
                            "[4] four",
                            "[5] five",
                            "[6] six",
                            "[7] seven",
                            "[8] eight",
                            "[9] nine"
        };
        // lines 3 and 9 deleted
        rev1 = new String[]
                        {
                        "[1] one",
                        "[2] two",
                        "[4] four",
                        "[5] five",
                        "[6] six",
                        "[7] seven",
                        "[8] eight",
        };

        // lines 7 and 8 changed, 9 deleted
        rev2 = new String[]
                        {
                        "[1] one",
                        "[2] two",
                        "[3] three",
                        "[4] four",
                        "[5] five",
                        "[6] six",
                        "[7] seven revised",
                        "[8] eight revised",
        };
    }

    @After
    public void tearDown()
    {
    }


    @Test
    public void testCompare()
    {
        assertTrue(!Diff.compare(original, empty));
        assertTrue(!Diff.compare(empty, original));
        assertTrue(Diff.compare(empty, empty));
        assertTrue(Diff.compare(original, original));
    }

    @Test
    public void testEmptySequences()
        throws DifferentiationFailedException
    {
        String[] emptyOrig = {};
        String[] emptyRev = {};
        Revision revision = Diff.diff(emptyOrig, emptyRev, algorithm);

        assertEquals("revision size is not zero", 0, revision.size());
    }

    @Test
    public void testOriginalEmpty()
        throws DifferentiationFailedException
    {
        String[] emptyOrig = {};
        String[] rev = {"1", "2", "3"};
        Revision revision = Diff.diff(emptyOrig, rev, algorithm);

        assertEquals("revision size should be one", 1, revision.size());
        assertTrue(revision.getDelta(0) instanceof AddDelta);
    }

    @Test
    public void testRevisedEmpty()
        throws DifferentiationFailedException
    {
        String[] orig = {"1", "2", "3"};
        String[] emptyRev = {};
        Revision revision = Diff.diff(orig, emptyRev, algorithm);

        assertEquals("revision size should be one", 1, revision.size());
        assertTrue(revision.getDelta(0) instanceof DeleteDelta);
    }

    @Test
    public void testDeleteAll()
        throws DifferentiationFailedException, PatchFailedException
    {
        Revision revision = Diff.diff(original, empty, algorithm);
        assertEquals(1, revision.size());
        assertEquals(DeleteDelta.class, revision.getDelta(0).getClass());
        assertTrue(Diff.compare(revision.patch(original), empty));
    }

    @Test
    public void testTwoDeletes()
        throws DifferentiationFailedException, PatchFailedException
    {
        Revision revision = Diff.diff(original, rev1, algorithm);
        assertEquals(2, revision.size());
        assertEquals(DeleteDelta.class, revision.getDelta(0).getClass());
        assertEquals(DeleteDelta.class, revision.getDelta(1).getClass());
        assertTrue(Diff.compare(revision.patch(original), rev1));
        assertEquals("3d2" + Diff.NL +
                     "< [3] three" + Diff.NL +
                     "9d7" + Diff.NL +
                     "< [9] nine" + Diff.NL
                     , revision.toString());
    }

    @Test
    public void testChangeAtTheEnd()
        throws DifferentiationFailedException, PatchFailedException
    {
        Revision revision = Diff.diff(original, rev2, algorithm);
        assertEquals(1, revision.size());
        assertEquals(ChangeDelta.class, revision.getDelta(0).getClass());
        assertTrue(Diff.compare(revision.patch(original), rev2));
        assertEquals("d7 3" + Diff.NL +
                     "a9 2" + Diff.NL +
                     "[7] seven revised" + Diff.NL +
                     "[8] eight revised" + Diff.NL,
                     revision.toRCSString());
    }

    @Test
    public void testPatchFailed()
        throws DifferentiationFailedException
    {
        try
        {
            Revision revision = Diff.diff(original, rev2, algorithm);
            assertTrue(!Diff.compare(revision.patch(rev1), rev2));
            fail("PatchFailedException not thrown");
        }
        catch (PatchFailedException e)
        {
        }
    }

    @Test
    public void testPreviouslyFailedShuffle()
        throws DifferentiationFailedException, PatchFailedException
    {
        Object[] orig = new String[]
                        {
                        "[1] one",
                        "[2] two",
                        "[3] three",
                        "[4] four",
                        "[5] five",
                        "[6] six"
        };

        Object[] rev = new String[]
                       {
                       "[3] three",
                       "[1] one",
                       "[5] five",
                       "[2] two",
                       "[6] six",
                       "[4] four"
        };
        Revision revision = Diff.diff(orig, rev, algorithm);
        Object[] patched = revision.patch(orig);
        assertTrue(Diff.compare(patched, rev));
    }

    @Test
    public void testEdit5()
        throws DifferentiationFailedException, PatchFailedException
    {
        Object[] orig = new String[]
                        {
                        "[1] one",
                        "[2] two",
                        "[3] three",
                        "[4] four",
                        "[5] five",
                        "[6] six"
        };

        Object[] rev = new String[]
                       {
                       "one revised",
                       "two revised",
                       "[2] two",
                       "[3] three",
                       "five revised",
                       "six revised",
                       "[5] five"
        };
        Revision revision = Diff.diff(orig, rev, algorithm);
        Object[] patched = revision.patch(orig);
        assertTrue(Diff.compare(patched, rev));
    }

    @Test
    public void testShuffle()
        throws DifferentiationFailedException, PatchFailedException
    {
        Object[] orig = new String[]
                        {
                        "[1] one",
                        "[2] two",
                        "[3] three",
                        "[4] four",
                        "[5] five",
                        "[6] six"
        };

        for (int seed = 0; seed < 10; seed++)
        {
            Object[] shuffle = Diff.shuffle(orig);
            Revision revision = Diff.diff(orig, shuffle, algorithm);
            Object[] patched = revision.patch(orig);
            if (!Diff.compare(patched, shuffle))
            {
                fail("iter " + seed + " revisions differ after patch");
            }
        }
    }

    @Test
    public void testRandomEdit()
        throws DifferentiationFailedException, PatchFailedException
    {
        Object[] orig = original;
        for (int seed = 0; seed < 10; seed++)
        {
            Object[] random = Diff.randomEdit(orig, seed);
            Revision revision = Diff.diff(orig, random, algorithm);
            Object[] patched = revision.patch(orig);
            if (!Diff.compare(patched, random))
            {
                fail("iter " + seed + " revisions differ after patch");
            }
            orig = random;
        }
    }

    @Test
    public void testVisitor()
    {
        Object[] orig = new String[]
                        {
                        "[1] one",
                        "[2] two",
                        "[3] three",
                        "[4] four",
                        "[5] five",
                        "[6] six"
        };
        Object[] rev = new String[]
                       {
                       "[1] one",
                       "[2] two revised",
                       "[3] three",
                       "[4] four revised",
                       "[5] five",
                       "[6] six"
        };

        class Visitor
            implements RevisionVisitor
        {

            StringBuffer sb = new StringBuffer();

            @Override
            public void visit(Revision revision)
            {
                sb.append("visited Revision\n");
            }

            @Override
            public void visit(DeleteDelta delta)
            {
                visit( (Delta) delta);
            }

            @Override
            public void visit(ChangeDelta delta)
            {
                visit( (Delta) delta);
            }

            @Override
            public void visit(AddDelta delta)
            {
                visit( (Delta) delta);
            }

            public void visit(Delta delta)
            {
                sb.append(delta.getRevised());
                sb.append("\n");
            }

            @Override
            public String toString()
            {
                return sb.toString();
            }
        }

        Visitor visitor = new Visitor();
        try
        {
            Diff.diff(orig, rev, algorithm).accept(visitor);
            assertEquals(visitor.toString(),
                         "visited Revision\n" +
                         "[2] two revised\n" +
                         "[4] four revised\n");
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testAlternativeAlgorithm()
        throws DifferentiationFailedException, PatchFailedException
    {
        Revision revision = Diff.diff(original, rev2, new SimpleDiff());
        assertEquals(1, revision.size());
        assertEquals(ChangeDelta.class, revision.getDelta(0).getClass());
        assertTrue(Diff.compare(revision.patch(original), rev2));
        assertEquals("d7 3" + Diff.NL +
                     "a9 2" + Diff.NL +
                     "[7] seven revised" + Diff.NL +
                     "[8] eight revised" + Diff.NL,
                     revision.toRCSString());
    }

    @Test
    public void testLargeShuffles()
        throws DifferentiationFailedException, PatchFailedException
    {
        Object[] orig = Diff.randomSequence(LARGE);
        for (int seed = 0; seed < 3; seed++)
        {
            Object[] rev = Diff.shuffle(orig);
            Revision revision = Diff.diff(orig, rev, algorithm);
            Object[] patched = revision.patch(orig);
            if (!Diff.compare(patched, rev))
            {
                fail("iter " + seed + " revisions differ after patch");
            }
            orig = rev;
        }
    }

    @Test
    public void testLargeShuffleEdits()
        throws DifferentiationFailedException, PatchFailedException
    {
        Object[] orig = Diff.randomSequence(LARGE);
        for (int seed = 0; seed < 3; seed++)
        {
            Object[] rev = Diff.randomEdit(orig, seed);
            Revision revision = Diff.diff(orig, rev, algorithm);
            Object[] patched = revision.patch(orig);
            if (!Diff.compare(patched, rev))
            {
                fail("iter " + seed + " revisions differ after patch");
            }
        }
    }

    @Test
    public void testLargeAllEdited()
        throws DifferentiationFailedException, PatchFailedException
    {
        Object[] orig = Diff.randomSequence(LARGE);
        Object[] rev = Diff.editAll(orig);
        Revision revision = Diff.diff(orig, rev, algorithm);
        Object[] patched = revision.patch(orig);
        if (!Diff.compare(patched, rev))
        {
            fail("revisions differ after patch");
        }

    }


}
