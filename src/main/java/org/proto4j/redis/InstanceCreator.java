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

import org.proto4j.redis.sql.Entity;
import org.proto4j.redis.sql.SQL;
import org.proto4j.redis.sql.SQLContext;
import org.proto4j.redis.sql.SQLExtractor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class InstanceCreator<T> implements RedisExtractor<T> {

    private final Class<T> type;

    private Constructor<T> constructor;

    InstanceCreator(Class<T> type) {
        this.type = type;
        if (type != null) {
            collect();
        }
    }

    private final ConcurrentMap<String, Field> columns = new ConcurrentHashMap<>();

    private void collect() {
        if (!type.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Type is not an Entity.class");
        }

        if (!columns.isEmpty()) {
            throw new IllegalCallerException("This method should be called only once!");
        }

        for (Field field : type.getDeclaredFields()) {
            SQL.Column column = field.getDeclaredAnnotation(SQL.Column.class);
            if (column != null) {
                String name = column.value();
                if (name.isEmpty()) {
                    name = field.getName();
                }
                columns.putIfAbsent(name, field);
            }
        }

        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public final T internalInstance() {
        try {
            return constructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException f) {
            return null;
        }
    }

    @Override
    public <E> RedisExtractor<E> newInstance(Class<E> cls) {
        return new InstanceCreator<>(cls);
    }

    @Override
    public T read(ResultSet resultSet, SQLContext context) throws SQLException {
        if (resultSet.isClosed()) {
            throw new SQLException("ResultSet was closed");
        }

        T value = internalInstance();
        if (value == null) {
            throw new SQLException("Could not create entity instance");
        }

        Object ref = context.getReference();
        Class<?> parent = context.getReferenceParent();

        // We are using this approach to fill up all fields that are stored
        // as a column in the ResultSet object. In order to make it possible
        // to fetch partial object data, this approach suits very well.
        for (int i = 1; i < resultSet.getMetaData().getColumnCount(); i++) {
            String column = resultSet.getMetaData().getColumnName(i);
            if (!columns.containsKey(column)) {
                continue;
            }

            Field field = columns.get(column);
            context.setReference(column);
            context.setReferenceParent(field.getType());

            SQLExtractor<?> extractor = Extractors.getDefinedExtractor(field.getType());
            if (extractor != null) {
                Object v = extractor.read(resultSet, context);
                field.setAccessible(true);
                try {
                    field.set(value, v);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        context.setReference(ref);
        context.setReferenceParent(parent);
        return value;
    }

    @Override
    public boolean accept(Class<?> cls) {
        return isTyped(cls);
    }

    public static boolean isTyped(Class<?> cls) {
        return cls.isAnnotationPresent(Entity.class);
    }
}
