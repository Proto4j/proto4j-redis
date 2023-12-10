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

package org.proto4j.redis.sql;//@date 31.08.2022

import java.lang.annotation.*;
import java.util.Objects;
import java.util.Properties;

/**
 * Sets the used {@link SQLFactory} and {@link java.sql.Driver} by providing
 * its identifier.
 *
 * @apiNote It is recommended to use the SQL.Environment class to save the
 *         used sql statement templates temporary.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SQL {

    /**
     * Used within a {@link Param} annotation it can be used to indicate the
     * sql statement is saved in the SQL.Environment class.
     */
    public static final String ENV = "$sql:env";

    /**
     * Indicates the annotated parameter stores its values in a {@link java.util.Map}.
     */
    public static final String MAP = "$sql:type.map";

    /**
     * Indicates all parameters declared in the sql statement will be replaced
     * step by step while iterating over the given array.
     */
    public static final String ARRAY = "$sql:type.array";

    /**
     * @return the jdbc driver type name.
     */
    String value();

    /**
     * Indicates the annotated method should execute a SQL-Select statement.
     * The result depends on the given return type and can be wrapped into
     * an {@link Entity} annotated type.
     *
     * @since 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Select {
        /**
         * @return the sql statement with argument patterns
         */
        String value();

        /**
         * @return the property descriptor if an environment statement should
         *         be used.
         */
        String property() default "";
    }

    /**
     * Indicates the annotated method should execute a SQL-Update statement.
     *
     * @since 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Update {
        /**
         * @return the sql statement with argument patterns
         */
        String value();

        /**
         * @return the property descriptor if an environment statement should
         *         be used.
         */
        String property() default "";
    }

    /**
     * Indicates the annotated method should execute a SQL-Insert statement.
     *
     * @since 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Insert {
        /**
         * @return the sql statement with argument patterns
         */
        String value();

        /**
         * @return the property descriptor if an environment statement should
         *         be used.
         */
        String property() default "";
    }

    /**
     * Indicates the annotated method should execute a SQL-Delete statement.
     *
     * @since 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Delete {
        /**
         * @return the sql statement with argument patterns
         */
        String value();

        /**
         * @return the property descriptor if an environment statement should
         *         be used.
         */
        String property() default "";
    }

    /**
     * Indicates the annotated method should execute a SQL-Create statement.
     *
     * @since 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Create {
        /**
         * @return the sql statement with argument patterns
         */
        String value();

        /**
         * @return the property descriptor if an environment statement should
         *         be used.
         */
        String property() default "";
    }

    /**
     * Indicates the annotated method should execute a SQL-Drop statement.
     *
     * @since 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Drop {
        /**
         * @return the sql statement with argument patterns
         */
        String value();

        /**
         * @return the property descriptor if an environment statement should
         *         be used.
         */
        String property() default "";
    }

    /**
     * Indicates the annotated method should execute a SQL-Raw statement.
     *
     * @since 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Raw {
        /**
         * @return the sql statement with argument patterns
         */
        String value();

        /**
         * @return the property descriptor if an environment statement should
         *         be used.
         */
        String property() default "";
    }

    /**
     * Fields annotated with the {@link Column} annotation will be used by the
     * {@code InstanceCreator} to wrap data in a {@link java.sql.ResultSet} into
     * a new type instance.
     * <p>
     * The name of column provided here has to be defined in the database,
     * otherwise an {@link java.sql.SQLException} will be thrown.
     *
     * @since 1.0
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Column {
        /**
         * @return the name of the corresponding column in the database.
         */
        String value();
    }

    /**
     * The SQL.Environment class can be used to store SQL statements globally
     * for a specific time period. It can be used within the {@link #ENV}
     * variable in a {@link Param} annotation.
     *
     * @since 1.0
     */
    public static class Environment {

        /**
         * The provided properties with all stored template statements.
         */
        private static volatile Properties properties;

        /**
         * Returns whether the given input string equals the {@link #ENV}
         * indicator.
         *
         * @param s the input string
         * @return s == {@link #ENV} (content equals)
         */
        public static boolean isDefined(String s) {
            return s != null && s.equals(ENV);
        }

        /**
         * Sets up a new environment with the given properties instance.
         *
         * @param p the new properties
         */
        public static synchronized void setupEnvironment(Properties p) {
            Objects.requireNonNull(p);
            properties = p;
        }

        /**
         * Retrieves a stored property for the given name.
         *
         * @param name the name of the stored property
         * @return the mapped property value
         */
        public static String getProperty(String name) {
            Objects.requireNonNull(name);
            return properties.getProperty(name);
        }

        /**
         * Resets the current environment.
         */
        public static synchronized void clear() {
            if (properties != null) {
                properties.clear();
                properties = null;
            }
        }
    }


}
