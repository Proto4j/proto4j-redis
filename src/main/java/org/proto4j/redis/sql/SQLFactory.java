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

package org.proto4j.redis.sql; //@date 02.09.2022

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * The base class every external factory must implement. This framework allows
 * multiple factories.
 * <p>
 * Each factory should apply a class that implements the SQLFactory interface. The
 * {@code FactoryManager} will try to load as many drivers as it can find and then
 * will be queried by the {@link org.proto4j.redis.Redis} class in order to
 * retrieve the right factory for creating the SQLService instance.
 * <p>
 * It is strongly recommended that each SQLFactory class should be small and
 * standalone so that the SQLFactory class can be loaded and queried without
 * bringing in vast quantities of supporting code.
 * <p>
 * When a Factory class is loaded, it should create an instance of
 * itself and register it with the FactoryManager. This means that a
 * user can load and register a factory by following the next instructions:
 * <ul>
 * <li>{@code Class.forName("com.abc.Factory")}</li>
 * <li>Creating a file named {@code META-INF/services/org.proto4j.redis.sql.SQLFactory}
 * with the path to the own implementation of the SQLFactory. For example,
 * <pre>
 *     com.example.impl.MyFactory # own implementation declared in that file
 * </pre>
 * For basic implementations visit the repository on the
 * <a href="https://github.com/Proto4j/proto4j-redis">proto4j-redis</a> repository
 * on github.
 * </li>
 * </ul>
 *
 * @see org.proto4j.redis.FactoryManager
 * @see org.proto4j.redis.Redis
 * @since 1.0
 */
public interface SQLFactory {

    /**
     * Attempts to create a new {@link SQLSource} instance with the given
     * configuration. Database connections are loaded and instantiated lazily,
     * so this method call will cause no connection to a database.
     *
     * @param conf the given configuration instance
     * @return a source object which will delegate the connecting process
     * @throws SQLDataException if the configuration was incorrect
     * @throws SQLWarning       if any deprecation warning should be displayed
     */
    public SQLSource engineGetSource(SQLConfiguration conf)
            throws SQLDataException, SQLWarning;

    /**
     * Retrieves a new {@link SQLService} instance for the registered factory.
     * By using this method, the source already should have created a connection
     * to the used database.
     *
     * @param source the data source
     * @return a service wrapper for executing sql queries.
     * @throws SQLException if an error occurs while creating the instance.
     */
    public SQLService engineGetService(SQLSource source)
            throws SQLException;

    /**
     * Gets the corresponding driver type that is registered to the
     * {@link java.sql.DriverManager}.
     *
     * @return the sql driver type
     */
    public String engineDriverType();

    /**
     * Gets the factory's minor version number. Initially this should be 0.
     *
     * @return this factory's minor version number
     */
    public int getMinorVersion();

    /**
     * Retrieves the factory's major version number. Initially this should
     * be 1.
     *
     * @return this factory's major version number
     */
    public int getMajorVersion();

}
