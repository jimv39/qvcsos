/*
 * Copyright 2014 JimVoris.
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

package com.qumasoft.server.filehistory;

/**
 * Enumerate the types of compression that we may use.
 * @author JimVoris
 */
public enum CompressionType {
    /** Not compressed. */
    NOT_COMPRESSED,
    /** Compressed using JVM zlib compression. */
    ZLIB_COMPRESSED;
}
