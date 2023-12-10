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

package org.proto4j.redis.sql; //@date 31.08.2022

import java.sql.SQLException;
import java.util.Objects;

/**
 * The base class for all services that execute the given sql statement on
 * the connected database. This class is provided by a {@link SQLFactory}
 * in order to make this framework more applicable to extensions.
 *
 * @since 1.0
 */
public abstract class SQLService {

    /**
     * The source storing a connection to the database.
     */
    private SQLSource source;

    /**
     * Creates a new SQLService with the given data source. The connection to
     * the database might not be opened yet, because will be initiated lazily.
     *
     * @param source a source storing the database connection.
     */
    public SQLService(SQLSource source) {
        this.source = Objects.requireNonNull(source);
    }

    /**
     * Executes a SELECT-statement and wraps the returned {@link java.sql.ResultSet}
     * with the given {@link SQLExtractor} to a given type.
     *
     * @param sql the sql statement
     * @param extractor the type extractor to create the type instance
     * @param <T> the type to be extracted
     * @return a new instance of type {@code T}
     * @throws SQLException if an error occurs
     */
    public abstract <T> T select(String sql, SQLExtractor<? extends T> extractor) throws SQLException;

    /**
     * Executes the given INSERT-statement.
     *
     * @param sql the sql statement
     * @return {@code true} if the statement was executed successfully,
     *         otherwise {@code false}
     * @throws SQLException if an error occurs
     */
    public abstract boolean insert(String sql) throws SQLException;

    /**
     * Executes the given UPDATE-statement.
     *
     * @param sql the sql statement
     * @return {@code true} if the statement was executed successfully,
     *         otherwise {@code false}
     * @throws SQLException if an error occurs
     */
    public abstract boolean update(String sql) throws SQLException;

    /**
     * Executes the given CREATE-statement.
     *
     * @param sql the sql statement
     * @return {@code true} if the statement was executed successfully,
     *         otherwise {@code false}
     * @throws SQLException if an error occurs
     */
    public abstract boolean create(String sql) throws SQLException;

    /**
     * Executes the given DROP-statement.
     *
     * @param sql the sql statement
     * @return {@code true} if the statement was executed successfully,
     *         otherwise {@code false}
     * @throws SQLException if an error occurs
     */
    public abstract boolean drop(String sql) throws SQLException;

    /**
     * Executes the given sql statement and returns the result wrapped into
     * an object.
     * <p>
     * It is strongly recommended to NOT return a {@link java.sql.ResultSet}
     * which was opened within a try-catch block, because it will be closed
     * automatically and no data would be read.
     *
     * @param sql the sql statement
     * @return {@code true} if the statement was executed successfully,
     *         otherwise {@code false}
     * @throws SQLException if an error occurs
     */
    public abstract Object raw(String sql) throws SQLException;

    /**
     * @return the data source
     */
    public SQLSource getSource() {
        return source;
    }

    /**
     * Sets a new data source instance.
     *
     * @param source the new data source.
     */
    public void setSource(SQLSource source) {
        this.source = Objects.requireNonNull(source);
    }

}
