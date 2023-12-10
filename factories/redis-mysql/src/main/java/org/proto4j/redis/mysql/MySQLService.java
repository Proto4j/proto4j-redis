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

package org.proto4j.redis.mysql; //@date 03.09.2022

import org.proto4j.redis.sql.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class MySQLService extends SQLService {

    private final SQLContext context;

    public MySQLService(SQLSource source) {
        super(source);
        context = new MySQLContext(source);
    }

    @Override
    public <T> T select(String sql, SQLExtractor<? extends T> extractor) throws SQLException {
        return rundml(sql, extractor);
    }

    @Override
    public boolean insert(String sql) throws SQLException {
        return (boolean) raw(sql);
    }

    @Override
    public boolean update(String sql) throws SQLException {
        return (boolean) raw(sql);
    }

    @Override
    public boolean create(String sql) throws SQLException {
        return (boolean) raw(sql);
    }

    @Override
    public boolean drop(String sql) throws SQLException {
        return (boolean) raw(sql);
    }

    @Override
    public Object raw(String sql) throws SQLException {
       return rundml(sql, null) == null;
    }

    private <T> T rundml(String sql, SQLExtractor<T> extractor) throws SQLException {
        Objects.requireNonNull(sql);

        try (PreparedStatement pst = getSource().prepare(sql)) {
            Objects.requireNonNull(pst);

            if (pst.execute()) {
                if (extractor != null) {
                    return extractor.read(pst.getResultSet(), context);
                }
            }
            return null;
        }
    }
}
