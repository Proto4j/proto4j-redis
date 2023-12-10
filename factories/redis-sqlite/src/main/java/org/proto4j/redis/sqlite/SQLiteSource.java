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
import org.proto4j.redis.sql.SQLSource;

import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

// same as DefaultSQLSource
public class SQLiteSource extends SQLSource {

    private volatile Connection connection;

    protected SQLiteSource(SQLConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection != null) {
            return connection;
        }

        String url = getConfiguration().getQualifiedName();
        Objects.requireNonNull(url);

        if (getConfiguration().getPrincipal() != null) {
            Principal principal = getConfiguration().getPrincipal();
            if (principal instanceof SQLPrincipal) {
                SQLPrincipal up = ((SQLPrincipal) principal);
                connection = DriverManager.getConnection(url, principal.getName(), new String(up.getPassword()));
                up.destroy();
            }
        } else {
            if (getConfiguration().getProperties() != null) {
                connection = DriverManager.getConnection(url, getConfiguration().getProperties());
            } else {
                connection = DriverManager.getConnection(url);
            }
        }
        return connection;
    }

    @Override
    public PreparedStatement prepare(String sql) throws SQLException {
        Connection c = getConnection();
        if (connection == null) {
            c = getConnection();
        }
        if (c == null || sql == null || sql.isEmpty()) {
            throw new SQLException("Statement is null");
        }
        return c.prepareStatement(sql);
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }
}
