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

package org.proto4j.redis.sql;//@date 29.08.2022

import java.security.Principal;
import java.util.Properties;

/**
 * The base class for all SQLConfiguration objects. This class is used to provide
 * a set of information about the connection that will be established.
 * <p>
 * The connection url will be provided through {@link #getQualifiedName()}, which
 * is implemented in every inheritor of this class.
 *
 * @since 1.0
 */
public abstract class SQLConfiguration {

    /**
     * The jdbc.driver name.
     */
    private final String driverType;

    /**
     * Additional properties as an alternative to the {@link SQLPrincipal} for
     * authentication.
     */
    private Properties properties;

    /**
     * Used for database authentication.
     */
    private Principal principal;

    /**
     * Create a new configuration instance without any additional configuration.
     *
     * @param driverType the jdbc driver name
     */
    public SQLConfiguration(String driverType) {
        this(driverType, null);
    }

    /**
     * Create a new configuration instance with additional configuration
     * properties. These could also include the username and password
     * property for authentication.
     *
     * @param driverType the jdbc driver name
     * @param properties the configuration properties
     */
    public SQLConfiguration(String driverType, Properties properties) {
        this(driverType, properties, null);
    }


    /**
     * A utility constructor for creating overloaded constructors.
     *
     * @param driverType the jdbc driver name
     * @param properties the configuration properties
     * @param principal the authentication parameters
     */
    protected SQLConfiguration(String driverType, Properties properties, SQLPrincipal principal) {
        this.driverType = driverType;
        this.properties = properties;
        this.principal  = principal;
    }

    /**
     * @return the database connection url
     */
    public abstract String getQualifiedName();

    /**
     * @return the jdbc driver name
     */
    public String getDriverType() {
        return driverType;
    }

    /**
     * @return the authentication parameters
     */
    public Principal getPrincipal() {
        return principal;
    }

    /**
     * @return the configuration properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets new authentication parameters with the dedicated {@link SQLPrincipal}.
     *
     * @param principal the authentication parameters
     */
    public void setPrincipal(SQLPrincipal principal) {
        this.principal = principal;
    }

    /**
     * Applies new properties to this configuration instance.
     *
     * @param properties the configuration properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
