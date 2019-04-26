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
package com.qumasoft.qvcslib;

/**
 * A helper assert class. Assertions using this class survive in production code, and throw RuntimeExceptions if the assertion condition fails.
 * @author Jim Voris
 */
public final class QumaAssert {

    /** Hide the default constructor */
    private QumaAssert() {
    }

    /**
     * Throw a runtime exception if the expression is not true.
     * @param expression must yield boolean true, or this will throw a RuntimeException.
     */
    public static void isTrue(boolean expression) {
        if (!expression) {
            throw new RuntimeException();
        }
    }

    /**
     * Throw a runtime exception if the expression is not true.
     * @param expression the expression that should be true.
     * @param message the message used to construct the RuntimeException that may get thrown.
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new RuntimeException(message);
        }
    }

    /**
     * Throw a runtime exception if the expression is true.
     * @param expression the expression should be false, or this will throw a RuntimeException.
     */
    public static void isFalse(boolean expression) {
        if (expression) {
            throw new RuntimeException();
        }
    }

    /**
     * Throw a runtime exception if the expression is true.
     * @param expression the expression that should be false.
     * @param message the message used to construct the RuntimeException that may get thrown.
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new RuntimeException(message);
        }
    }
}
