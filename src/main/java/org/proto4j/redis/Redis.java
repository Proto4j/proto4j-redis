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

package org.proto4j.redis; //@date 01.09.2022

import org.proto4j.redis.sql.*;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Redis adapts a Java interface into calls to a connected database by using
 * annotations on the declared methods and its parameters. Create instances
 * with providing your interface class and the {@link SQLConfiguration} or
 * {@link SQLSource} to the {@link #create} method.
 * <p>
 * For example (with usage of the SQLite factory),
 * <pre><code>
 * SQLConfiguration config = new SQLiteConfiguration("mydb");
 * MyApi api = Redis.create(MyApi.class, config);
 *
 * List&lt;User&gt; list = api.fetchUsersFrom("table_two");
 * </code></pre>
 *
 * @author MatrixEditor
 * @version 1.0
 */
public final class Redis {

    // no instance creation allowed
    private Redis() {}

    /**
     * Create an implementation of the API methods defined by the {@code api}
     * interface.
     * <p>
     * Method parameters annotated with {@link Param} can be used to dynamically
     * replace parts of the sql statement. Replacement sections are denoted by
     * an identifier surrounded by curly braces (e.g., "{abc}").
     * <p>
     * The creation of {@link SQLSource} and {@link SQLService} objects are
     * delegated to the specified class that implements the basic functions
     * from the {@link SQLFactory} interface.
     * <p>
     * For example,
     * <pre>
     * &#64;SQL(SQLiteFactory.FACTORY)
     * public interface UserStorage {
     *     &#64;SQL.Select("select * from {table}")
     *     public List&lt;User&gt; fetchUsersFrom(&#64;Param("table") String tb);
     * }
     * </pre>
     *
     * @param cls the interface tha will be implemented
     * @param configuration the {@link SQLConfiguration} instance used to
     *         create a {@link SQLSource}.
     * @param <API> the type of the api
     * @return a new instance for the specified interface type
     */
    public static <API> API create(Class<API> cls, SQLConfiguration configuration) {
        validateClass(cls, configuration);

        SQLService service = buildService(cls, configuration);
        return getApiService(cls, service);
    }

    /**
     * Create an implementation of the API methods defined by the {@code api}
     * interface.
     *
     * @param cls the interface tha will be implemented
     * @param source the {@link SQLSource} instance with a {@link SQLConfiguration}.
     * @param <API> the type of the api
     * @see #create(Class, SQLConfiguration)
     * @return a new instance for the specified interface type
     */
    public static <API> API create(Class<API> cls, SQLSource source) {
        validateClass(cls, source);

        SQLService service = buildService(cls, source);
        return getApiService(cls, service);
    }

    private static <API> void validateClass(Class<API> cls, Object source) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(source);

        if (!cls.isInterface()) {
            throw new IllegalArgumentException("API type is not an interface");
        }
    }

    private static <API> API getApiService(Class<API> cls, SQLService service) {
        SQLValidator validator = buildValidator(cls);

        InvocationHandler handler = buildHandler(service, validator);
        //noinspection unchecked
        return (API) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, handler);
    }

    private static InvocationHandler buildHandler(SQLService service, SQLValidator validator) {
        // put methods into map with name identifier
        return new InvocationHandler() {
            private final ConcurrentMap<Method, SQLMethod> methodCache = new ConcurrentHashMap<>();

            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    // A common mistake is to invoke this method with the proxy
                    // object given as an argument value. If the 'proxy' object
                    // is used to invoke the method it will loop infinitely,
                    // because the proxy object is referring to the InvocationHandler
                    // object. 'this' will be the equivalent to the used
                    // class instance.
                    return method.invoke(this, args);
                }

                SQLMethod apiMethod = methodCache.get(method);
                if (apiMethod == null) {
                    apiMethod = MethodBuilder.read(method, service, validator);
                    methodCache.put(method, apiMethod);
                }

                return apiMethod.invoke(args);
            }
        };
    }

    static SQLService buildService(Class<?> cls, Object obj) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(obj);

        if (!cls.isAnnotationPresent(SQL.class)) {
            throw new IllegalArgumentException("SQLService not defined!");
        }

        try {
            String driverType =
                    cls.getDeclaredAnnotation(SQL.class).value();

            SQLFactory factory = FactoryManager.getFactory(driverType);
            if (obj instanceof SQLSource) {
                return factory.engineGetService((SQLSource) obj);
            } else if (obj instanceof SQLConfiguration) {
                SQLSource source =
                        factory.engineGetSource((SQLConfiguration) obj);

                return factory.engineGetService(source);
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("SQLService.<init>(SQLSource.class) not called");
        }
        throw new IllegalArgumentException("Could not build SQLService");
    }

    static SQLValidator buildValidator(Class<?> cls) {
        Objects.requireNonNull(cls);

        if (!cls.isAnnotationPresent(Validator.class)) {
            return null;
        }

        try {
            Class<? extends SQLValidator> type =
                    cls.getDeclaredAnnotation(Validator.class).value();

            Constructor<? extends SQLValidator> constructor =
                    type.getDeclaredConstructor();

            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("SQLValidator.<init>() not defined!");

        } catch (InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new IllegalCallerException("Could not create SQLValidator instance");
        }
    }
}
