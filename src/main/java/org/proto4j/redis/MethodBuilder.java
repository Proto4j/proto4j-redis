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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

class MethodBuilder {

    private final SQLMethod method = new SQLMethod();

    private final List<SQLMethod.ParamInfo> info = new LinkedList<>();

    public static MethodBuilder newBuilder() {
        return new MethodBuilder();
    }

    public static SQLMethod read(Method method, SQLService service, SQLValidator validator) {
        MethodBuilder builder = newBuilder().verifier(validator);

        AtomicReference<Annotation> used = new AtomicReference<>(null);
        builder.setStatement(Extractors.getSqlFromAnnotation(method, used));
        for (Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Param.class)) {
                continue;
            }
            Param param = parameter.getDeclaredAnnotation(Param.class);

            SQLMethod.ParamInfo paramInfo = new SQLMethod.ParamInfo();
            paramInfo.array  = param.value().equals(SQL.ARRAY)
                    || parameter.getType().isArray();
            paramInfo.mapped = param.value().equals(SQL.MAP)
                    || Map.class.isAssignableFrom(parameter.getType());

            paramInfo.typed = InstanceCreator.isTyped(parameter.getType());

            paramInfo.name = param.value().isEmpty()
                    ? parameter.getName() : param.value();

            builder.add(paramInfo);
        }

        String qMethod = used.get().annotationType()
                             .getSimpleName().toLowerCase();
        Method worker = null;
        // Because the select()-method contains two arguments the secure
        // and concurrent way to retrieve the worker method is used.
        for (Method m0 : service.getClass().getDeclaredMethods()) {
            if (m0.getName().equals(qMethod)) {
                worker = m0;
                break;
            }
        }

        if (worker == null) {
            throw new NullPointerException("Could not find service method: " + qMethod);
        }

        SQLMethod.Worker w;
        if (qMethod.equals(SQL.Select.class.getSimpleName().toLowerCase())) {
            SQLExtractor<?> e = Extractors.getDefinedExtractor(method.getReturnType(), method.getGenericReturnType());
            if (e == null) {
                throw new IllegalArgumentException("Return type not defined");
            }

           w = (stmt) -> service.select(stmt, e);
        }
        else {
            Method finalWorker = worker;
            w = (sql) -> invokeWorker(service, finalWorker, sql);
        }

        return builder.workWith(w).finish();
    }

    private static Object invokeWorker(SQLService service, Method worker, String sql) {
        try {
            return worker.invoke(service, sql);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public MethodBuilder verifier(SQLValidator validator) {
        method.setValidator(validator);
        return this;
    }

    public MethodBuilder setStatement(String sql) {
        Objects.requireNonNull(sql);
        method.setSql(sql);
        return this;
    }

    public MethodBuilder add(SQLMethod.ParamInfo paramInfo) {
        Objects.requireNonNull(paramInfo);
        info.add(paramInfo);
        return this;
    }

    public SQLMethod finish() {
        method.setParams(info.toArray(SQLMethod.ParamInfo[]::new));
        return method;
    }

    public MethodBuilder workWith(SQLMethod.Worker worker) {
        Objects.requireNonNull(worker);
        method.setWorker(worker);
        return this;
    }
}
