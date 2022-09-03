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

package org.proto4j.redis; //@date 02.09.2022

import org.proto4j.redis.sql.SQLFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * The basic service for managing a set of SQLFactories.
 * <p>
 * As part of its initialization, the {@code FactoryManager} class will
 * attempt to load available {@code SQLFactory} classes by using:
 * <ul>
 * <li>Service providers of the {@code java.sql.Driver} class, that are loaded
 * via the {@link ServiceLoader#load(Class)} mechanism.</li>
 * </ul>
 *
 * @apiNote {@code FactoryManager} initialization is done lazily and looks up
 *         service providers using the thread context class loader. The drivers loaded
 *         and available to an application will depend on the thread context class
 *         loader of the thread that triggers driver initialization by
 *         {@code FactoryManager}.
 *
 * @see SQLFactory
 * @since 1.0
 */
public class FactoryManager {

    // list of registered SQLFactory classes
    private static final CopyOnWriteArrayList<FactoryInfo> registeredFactories =
            new CopyOnWriteArrayList<>();

    // used to synchronize all callers that access the registered factories
    private static final Object FACTORY_LOCK = new Object();

    // used to indicate whether factories were initialized once
    private static volatile boolean factoriesLoaded;

    // Prevents the FactoryManager class from being instantiated.
    private FactoryManager() {}

    /**
     * Registers the given factory with the {@code FactoryManager}.
     * A newly-loaded driver class should call the method {@code registerDriver}
     * to make itself known to the {@code FactoryManager}. If the driver is currently
     * registered, no action is taken.
     *
     * @param factory the new {@link SQLFactory} that is to be registered with the
     *         {@code FactoryManager}
     * @throws NullPointerException if {@code factory} is null
     */
    public static void registerFactory(SQLFactory factory) {
        synchronized (FACTORY_LOCK) {
            Objects.requireNonNull(factory);

            registeredFactories.addIfAbsent(new FactoryInfo(factory));
        }
    }

    /**
     * Removes the specified driver from the {@code FactoryManager}'s list of
     * registered factories.
     * <p>
     * If a {@code null} value is specified for the driver to be removed, then no
     * action is taken.
     * <p>
     * If the specified factory is not found in the list of registered factories,
     * then no action is taken. If the factory was found, it will be removed
     * from the list of registered factories.
     *
     * @param factory the {@link SQLFactory} to remove
     */
    public static void deregisterFactory(SQLFactory factory) {
        if (factory == null) {
            return;
        }

        synchronized (FACTORY_LOCK) {
            FactoryInfo info = new FactoryInfo(factory);

            registeredFactories.remove(info);
        }
    }

    /**
     * Attempts to locate a factory that is bound to the given driver type.
     *
     * @param driverType a string representation of the driver type
     * @return a {@link SQLFactory} object that can create {@code SQLService}
     *         and {@code SQLSource} instances.
     */
    public static SQLFactory getFactory(String driverType) {
        Objects.requireNonNull(driverType);

        ensureDriversInitialized();

        synchronized (FACTORY_LOCK) {
            for (FactoryInfo info : registeredFactories) {
                if (info.factory.engineDriverType()
                                .equalsIgnoreCase(driverType)) {
                    return info.factory;
                }
            }
        }

        throw new IllegalArgumentException("No suitable factory found for " + driverType);
    }

    /**
     * Retrieves an Enumeration with all the currently loaded factories
     * to which the current caller has access.
     *
     * <P><B>Note:</B> The classname of a driver can be found using
     * <CODE>d.getClass().getName()</CODE>
     *
     * @return the list of {@link SQLFactory} loaded by the caller's class loader
     * @see #factories()
     */
    public static Enumeration<SQLFactory> getFactories() {
        return Collections.enumeration(getFactoriesInternal());
    }

    /**
     * Retrieves a Stream with all the currently loaded factories
     * to which the current caller has access.
     *
     * @return the stream of {@link SQLFactory} loaded by the caller's
     *         class loader
     * @since Java 9
     */
    public static Stream<SQLFactory> factories() {
        return getFactoriesInternal().stream();
    }

    private static List<SQLFactory> getFactoriesInternal() {
        List<SQLFactory> result = new LinkedList<>();

        for (FactoryInfo info : registeredFactories) {
            result.add(info.factory);
        }
        return result;
    }

    private static void ensureDriversInitialized() {
        if (factoriesLoaded) {
            return;
        }

        synchronized (FACTORY_LOCK) {
            if (factoriesLoaded) {
                return;
            }

            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    ServiceLoader<SQLFactory> factories = ServiceLoader.load(SQLFactory.class);
                    Iterator<SQLFactory>      iterator  = factories.iterator();

                    try {
                        while (iterator.hasNext()) {
                            SQLFactory factory = iterator.next();
                        }
                    } catch (Throwable t) {
                        // ignore that exception
                    }
                    return null;
                }
            });

            factoriesLoaded = true;
        }
    }

    private static class FactoryInfo {

        final SQLFactory factory;

        private FactoryInfo(SQLFactory factory) {this.factory = factory;}

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FactoryInfo
                    && this.factory == ((FactoryInfo) obj).factory;
        }

        @Override
        public int hashCode() {
            return factory.hashCode();
        }
    }

}
