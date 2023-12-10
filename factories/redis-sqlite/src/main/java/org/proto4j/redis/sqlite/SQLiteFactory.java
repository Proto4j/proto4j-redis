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

import org.proto4j.redis.FactoryManager;
import org.proto4j.redis.sql.SQLConfiguration;
import org.proto4j.redis.sql.SQLFactory;
import org.proto4j.redis.sql.SQLService;
import org.proto4j.redis.sql.SQLSource;

import java.io.IOException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Properties;

public class SQLiteFactory implements SQLFactory {

    public static final String FACTORY = "sqlite";

    static {
        try {
            // This factory should work only if the SQLite-Driver
            // is registered. Otherwise, this service can not be called
            // because the corresponding driver is missing.
            DriverManager.getDriver("jdbc:sqlite:temp");

            FactoryManager.registerFactory(new SQLiteFactory());
        } catch (SQLException e) {
            System.err.println("WARNING: no suitable driver for jdbc:sqlite");
        }
    }

    public SQLiteFactory() {
    }

    @Override
    public SQLSource engineGetSource(SQLConfiguration conf) throws SQLDataException, SQLWarning {
        return new SQLiteSource(conf);
    }

    @Override
    public SQLService engineGetService(SQLSource source) throws SQLException {
        return new SQLiteService(source);
    }

    @Override
    public String engineDriverType() {
        return FACTORY;
    }

    @Override
    public int getMajorVersion() {
        String[] vInfo = getVersion().split("\\.");
        return vInfo.length > 1 ? Integer.parseInt(vInfo[1]) : 1;
    }

    @Override
    public int getMinorVersion() {
        String[] vInfo = getVersion().split("\\.");
        return vInfo.length > 2 ? Integer.parseInt(vInfo[2]) : 0;
    }

    public static String getVersion() {
        URL versionFile = SQLiteFactory.class.getResource("/redis-sqlite.properties");
        if (versionFile == null) {
            throw new UnsupportedClassVersionError("Could not examine version");
        }

        String version = "unknown";
        try {
            Properties data = new Properties();
            data.load(versionFile.openStream());
            version = data.getProperty("version", version);
            version = version.trim();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return version;
    }
}
