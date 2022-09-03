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

import org.proto4j.redis.sql.SQLConfiguration;
import org.proto4j.redis.sql.SQLPrincipal;

import java.util.Objects;
import java.util.Properties;

public class MySQLConfiguration extends SQLConfiguration {

    private final String host;
    private final int    port;
    private final String database;

    public MySQLConfiguration(String host, int port, String database) {
        this(null, host, port, database);
    }

    public MySQLConfiguration(SQLPrincipal principal,
                              String host, int port, String database) {
        this(null, principal, host, port, database);
    }

    public MySQLConfiguration(Properties properties, SQLPrincipal principal,
                              String host, int port, String database) {
        super(MySQLFactory.FACTORY, properties, principal);
        this.host     = Objects.requireNonNull(host);
        this.port     = port;
        this.database = Objects.requireNonNull(database);
    }


    @Override
    public String getQualifiedName() {
        String path = "//" + host + ':' + port + '/' + database;
        return String.join(":", "jdbc", getDriverType(), path);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }
}
