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

package org.apache.commons.jrcs.rcs;

import java.util.Arrays;
import java.util.StringTokenizer;
import org.apache.commons.jrcs.util.ToString;

/**
 * Contains and manages a version number of the form "x(\.y)*".
 * This class is NOT thread safe.
 *
 * @see Archive
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @version $Id: Version.java,v 1.5 2004/02/28 03:35:36 bayard Exp $
 */
public class Version
        extends ToString
        implements Cloneable, Comparable
{
    private int[] numbers = new int[0];


    /**
     * Creates a new Version with a single digit version number
     * @param major the version number
     */
    public Version(int major)
    {
        numbers = new int[]{major};
    }

    /**
     * Creates a new Version with a major.minor version number.
     * @param major the major version number
     * @param minor the minor version number
     */
    public Version(int major, int minor)
    {
        numbers = new int[]{major, minor};
    }

    /**
     * Converts an array of Integer to a Version.
     * @param num an array of Integers
     */
    public Version(Integer[] num)
    {
        numbers = new int[num.length];
        for (int i = 0; i < num.length; i++)
        {
            numbers[i] = num[i].intValue();
        }
    }

    /**
     * Converts an array of int to a Version.
     * @param num an array of int
     */
    public Version(int[] num)
    {
        numbers = (int[]) num.clone();
    }

    /**
     * Converts string to a version.
     * @param v a string accepted by the following regular expression.
     * <code>
     *   [0-9]+(.[0-9]+)*
     * </code>
     * @throws InvalidVersionNumberException if the string cannot be parsed
     */
    public Version(String v)
            throws InvalidVersionNumberException
    {
        if (v.endsWith("."))
        {
            v = v + "0";
        }
        StringTokenizer t = new StringTokenizer(v, ".");

        int count = t.countTokens();
        if (even(count) && v.endsWith(".0"))
        {
            count--;
        } // allow a .0 ending only in branch revisions

        numbers = new int[count];
        for (int i = 0; i < count; i++)
        {
            try
            {
                numbers[i] = Integer.parseInt(t.nextToken());
            }
            catch (NumberFormatException e)
            {
                throw new InvalidVersionNumberException(v);
            }
        }
    }

    /**
     * Create a new Version by copying another.
     * @param v the version to copy
     */
    public Version(Version v)
    {
        this.numbers = (int[]) v.numbers.clone();
        if (!Arrays.equals(this.numbers, v.numbers))
        {
            throw new IllegalStateException(Arrays.toString(numbers));
        }
    }

    /**
     * Create an empty version number.
     */
    public Version()
    {
    }

    public Object clone()
    {
        return new Version(this);
    }


    /**
     * Return the current version number as an array of int.
     * @return the current version number as an array of int.
     */
    public int[] getNumbers()
    {
        return (int[]) this.numbers.clone();
    }


    /**
     * Compares two versions.
     * The comparison is done the usual way, i.e.,  2.0 is greter than 1.99.1,
     * and 0.1.2 is greater than 0.1
     * @param ver the version to compare to.
     * @return 0 if this == ver, 1 if this greater than ver, -1 otherwise.
     */
    public int compareVersions(Version ver)
    {
        int[] nthis = this.numbers;
        int[] nthat = ver.numbers;

        int i;
        for (i = 0; i < nthis.length; i++)
        {
            if (i >= nthat.length || nthis[i] > nthat[i])
            {
                return 1;
            }
            else if (nthis[i] < nthat[i])
            {
                return -1;
            }
        }
        // all matched up to i-1
        if (nthat.length > i)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }

    /**
     * Compares two versions in lexigographical order.
     * Unlike compareVersions, this comparison is not done in
     * the way usual for versions numbers. The order relationship
     * stablished here is the one CVS used to store nodes into archive
     * files.
     * @param other The version to compare to
     * @see #compareVersions
     */
    public int compareTo(Object other)
    {
        if (other == this)
        {
            return 0;
        }
        else if (!(other instanceof Version))
        {
            throw new IllegalArgumentException(other.toString());
        }
        else {
            Version otherVer = (Version) other;
            if (this.size() != otherVer.size())
            {
              return this.size() - otherVer.size();
            }
            else
            {
                return -compareVersions(otherVer);
            }
        }
    }

    /**
     * Determine if this version is greater than the given one.
     * @param ver the version to compare to.
     * @return true if compareVersions(ver) &gt; 0
     * @see #compareVersions
     */
    public boolean isGreaterThan(Version ver)
    {
        return compareVersions(ver) > 0;
    }

    /**
     * Determine if this version is greater than or equal to the given one.
     * @param ver the version to compare to.
     * @return true if compareVersions(ver) &gt;= 0
     * @see #compareVersions
     */
    public boolean isGreaterOrEqualThan(Version ver)
    {
        return compareVersions(ver) >= 0;
    }

    /**
     * Determine if this version is less than the given one.
     * @param ver the version to compare to.
     * @return true if compareVersions(ver) &lt; 0
     * @see #compareVersions
     */
    public boolean isLessThan(Version ver)
    {
        return compareVersions(ver) < 0;
    }

    /**
     * Determine if this version is less than or equal to the given one.
     * @param ver the version to compare to.
     * @return true if compareVersions(ver) &lt;= 0
     * @see #compareVersions
     */
    public boolean isLessOrEqualThan(Version ver)
    {
        return compareVersions(ver) <= 0;
    }

    /**
     * Determine if two versions are equal.
     * @param o the version to compare to
     * @return true if both versions represent the same version number
     */
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        else if (!(o instanceof Version))
        {
            return false;
        }
        else if (hashCode() != o.hashCode())
        {
            return false;
        }
        else
        {
            return compareTo((Version) o) == 0;
        }
    }

    public int hashCode()
    {
        return toString().hashCode();
    }

    /**
     * Return the version number at the given position.
     * @param pos the position.
     * @return the number.
     */
    public int at(int pos)
    {
        return numbers[pos];
    }

    /**
     * Return the last number in the version number.
     * @return the number.
     */
    public int last()
    {
        return at(size() - 1);
    }

    /**
     * Return the last number in the version number.
     * @return the number.
     */
    public Version getBase(int positions)
    {
        positions = (positions > numbers.length ? numbers.length : positions);
        int[] result = new int[positions];
        System.arraycopy(this.numbers, 0, result, 0, positions);
        return new Version(result);
    }

    public Version getBranchPoint()
    {
        return getBase(size() - 1);
    }

    public Version next()
    {
        Version result = new Version(this);
        result.numbers[this.numbers.length - 1] = this.last() + 1;
        return result;
    }

    protected void __addBranch(Integer branch)
    {
        __addBranch(branch.intValue());
    }

    protected void __addBranch(int branch)
    {
        int[] newnum = new int[numbers.length + 1];
        System.arraycopy(this.numbers, 0, newnum, 0, numbers.length);
        newnum[numbers.length] = branch;
        this.numbers = newnum;
    }

    public Version newBranch(int branch)
    {
        int[] newnum = new int[numbers.length + 1];
        System.arraycopy(this.numbers, 0, newnum, 0, numbers.length);
        newnum[numbers.length] = branch;

        Version result = new Version();
        result.numbers = newnum;
        return result;
    }

    public int size()
    {
        return numbers.length;
    }

    public boolean isTrunk()
    {
        return (size() >= 1) && (size() <= 2);
    }

    public boolean isBranch()
    {
        return size() > 2;
    }

    public boolean isRevision()
    {
        return even();
    }

    public boolean isGhost()
    {
        for (int i = 0; i < size(); i++)
        {
            if (numbers[i] <= 0)
            {
                return true;
            }
        }
        return false;
    }

    public boolean even(int n)
    {
        return n % 2 == 0;
    }

    public boolean even()
    {
        return even(size());
    }

    public boolean odd(int n)
    {
        return !even(n);
    }

    public boolean odd()
    {
        return !even();
    }

    public void toString(StringBuffer s)
    {
        if (size() > 0)
        {
            s.append(Integer.toString(numbers[0]));
            for (int i = 1; i < numbers.length; i++)
            {
                s.append(".");
                s.append(Integer.toString(numbers[i]));
            }
        }
    }
}

