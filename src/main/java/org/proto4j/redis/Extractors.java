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

package org.proto4j.redis; //@date 31.08.2022

import org.proto4j.redis.sql.SQL;
import org.proto4j.redis.sql.SQLContext;
import org.proto4j.redis.sql.SQLExtractor;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A utility class used to register new {@link SQLExtractor} instances and
 * get a defined one from the given type.
 *
 * @see RedisExtractor
 * @since 1.0
 */
public final class Extractors {

    /**
     * A list of declared {@link RedisExtractor} instances
     */
    private static final List<RedisExtractor<?>> EXTRACTORS = new LinkedList<>();

    static {
        // the default extractors are added by default
        registerExtractor(new PrimitiveExtractor());
        registerExtractor(new InstanceCreator<>(null));
        registerExtractor(new RawExtractor());
        registerExtractor(new ListExtractor());
    }

    // No instance(s) of this class should be defined externally
    private Extractors() {}

    /**
     * Returns the defined {@link RedisExtractor} for the given parameterized
     * type.
     *
     * @param cls the base type with a generic type declaration
     * @param genericType a parameterized type instance
     * @return {@code null} if no {@link RedisExtractor} instance could handle
     *         the base type, otherwise the corresponding extractor instance.
     * @see #getDefinedExtractor(Class)
     */
    public static RedisExtractor<?> getDefinedExtractor(Class<?> cls, Type genericType) {
        Objects.requireNonNull(cls);

        synchronized (EXTRACTORS) {
            for (RedisExtractor<?> extractor : EXTRACTORS) {
                if (extractor.accept(cls)) {
                    if (extractor instanceof ListExtractor) {
                        return new ListExtractor(cls, genericType);
                    }
                    return extractor.newInstance(cls);
                }
            }
        }
        return null;
    }

    /**
     * Returns the defined {@link RedisExtractor} for the given base type.
     *
     * @param cls the base type with a generic type declaration
     * @return {@code null} if no {@link RedisExtractor} instance could handle
     *         the base type, otherwise the corresponding extractor instance.
     */
    public static RedisExtractor<?> getDefinedExtractor(Class<?> cls) {
        return getDefinedExtractor(cls, null);
    }

    /**
     * Registers the given extractor the default ones.
     *
     * @param extractor The new extractor instance.
     */
    public static void registerExtractor(RedisExtractor<?> extractor) {
        Objects.requireNonNull(extractor);

        synchronized (EXTRACTORS) {
            EXTRACTORS.add(extractor);
        }
    }

    static String getSqlFromAnnotation(Method method, AtomicReference<Annotation> declaredAnnotation) {
        Objects.requireNonNull(method);

        Annotation[] annotations = method.getDeclaredAnnotations();
        if (annotations.length == 0) {
            return null;
        }

        String sql = null;
        try {
            for (Annotation a : annotations) {
                if (a.annotationType().getDeclaringClass() == SQL.class) {
                    Object value = a.annotationType().getDeclaredMethod("value")
                                    .invoke(a);
                    sql = value.toString();

                    if (SQL.Environment.isDefined(sql)) {
                        Object key = a.annotationType().getDeclaredMethod("property")
                                      .invoke(a);
                        sql = SQL.Environment.getProperty(key.toString());
                    }
                    declaredAnnotation.set(a);
                    break;
                }
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }

        return sql;
    }

    public static class PrimitiveExtractor implements RedisExtractor<Object> {

        public static boolean isPrimitive(Class<?> cls) {
            if (cls == null || isChar(cls)) return false;

            if (cls.isPrimitive() || cls == String.class) return true;

            try {
                Field type = cls.getDeclaredField("TYPE");
                return true;

            } catch (NoSuchFieldException e) {
                return false;
            }
        }

        private static boolean isChar(Class<?> cls) {
            return cls == char.class || cls == Character.class;
        }

        @Override
        public Object read(ResultSet resultSet, SQLContext context) throws SQLException {
            if (context.getReferenceParent() != null) {
                Class<?> parent = context.getReferenceParent();

                if (parent == String.class) {
                    return resultSet.getString((String) context.getReference());
                }
                try {
                    String typename = parent.getName();
                    if (!parent.isPrimitive()) {
                        Field type = parent.getDeclaredField("TYPE");

                        Class<?> primitiveType = (Class<?>) type.get(null); // static
                        typename = primitiveType.getSimpleName();
                    }

                    String name = "get" + typename.toUpperCase().charAt(0) + typename.substring(1);

                    // assert method is defined
                    Method method = resultSet.getClass().getMethod(name, String.class);
                    return method.invoke(resultSet, context.getReference().toString());

                } catch (ReflectiveOperationException e) {
                    throw new SQLException(e);
                }
            }
            throw new IllegalArgumentException();
        }

        @Override
        public boolean accept(Class<?> cls) {
            return isPrimitive(cls);
        }

        @Override
        public <E> RedisExtractor<E> newInstance(Class<E> cls) {
            //noinspection unchecked
            return (RedisExtractor<E>) this;
        }
    }

    public static class RawExtractor implements RedisExtractor<ResultSet> {

        @Override
        public boolean accept(Class<?> cls) {
            return ResultSet.class.isAssignableFrom(cls);
        }

        @Override
        public <E> RedisExtractor<E> newInstance(Class<E> cls) {
            //noinspection unchecked
            return (RedisExtractor<E>) new RawExtractor();
        }

        @Override
        public ResultSet read(ResultSet resultSet, SQLContext context) throws SQLException {
            return resultSet;
        }
    }

    public static class ListExtractor implements RedisExtractor<List<?>> {

        private final Class<?> type;

        private RedisExtractor<?> extractor;

        public ListExtractor() {
            this(null, null);
        }

        public ListExtractor(Class<?> type, Type genericType) {
            this.type      = type;
            this.extractor = null;
            if (genericType != null) {
                if (genericType instanceof ParameterizedType) {
                    Type[] types = ((ParameterizedType) genericType)
                            .getActualTypeArguments();
                    if (types.length == 0) {
                        throw new IllegalArgumentException("CRITICAL: class is not typed");
                    }

                    Class<?> cls = (Class<?>) types[0];
                    this.extractor = getDefinedExtractor(cls);
                    if (extractor == null) {
                        throw new IllegalArgumentException(
                                "No extractor for type '" + cls.getSimpleName() + "' defined"
                        );
                    }
                }
            }
        }


        @Override
        public boolean accept(Class<?> cls) {
            return List.class.isAssignableFrom(cls);
        }

        @Override
        public <E> RedisExtractor<E> newInstance(Class<E> cls) {
            //noinspection unchecked
            return (RedisExtractor<E>) new ListExtractor(cls, null);
        }

        @Override
        public List<?> read(ResultSet resultSet, SQLContext context) throws SQLException {
            Class<?> type = this.type;
            if (type == null) return Collections.emptyList();

            if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
                type = ArrayList.class;
            }

            try {
                //noinspection unchecked
                List<Object> list = (List<Object>) type.getDeclaredConstructor().newInstance();

                while (resultSet.next()) {
                    Object o = extractor.read(resultSet, context);
                    list.add(o);
                }
                return list;
            } catch (ReflectiveOperationException e) {
                throw new SQLException("could not create list instance");
            }
        }
    }
}
