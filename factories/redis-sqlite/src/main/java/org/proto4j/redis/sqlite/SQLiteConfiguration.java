/*
 * MIT License
 *
 * Copyright (c) 2022 Proto4j
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.proto4j.redis.sqlite; //@date 03.09.2022

import org.proto4j.redis.sql.SQLConfiguration;
import org.proto4j.redis.sql.SQLPrincipal;

import java.util.Objects;

public class SQLiteConfiguration extends SQLConfiguration {

    private final String path;

    public SQLiteConfiguration(String path) {
        this(null, path);
    }

    public SQLiteConfiguration(SQLPrincipal principal, String path) {
        super(SQLiteFactory.FACTORY, null, principal);
        this.path = Objects.requireNonNull(path);
    }

    @Override
    public String getQualifiedName() {
        return String.join(":", "jdbc", getDriverType(), path);
    }

    public String getPath() {
        return path;
    }
}
