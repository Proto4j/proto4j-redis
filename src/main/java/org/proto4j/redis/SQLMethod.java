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

import org.proto4j.redis.sql.SQLValidator;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.Map;

class SQLMethod {

    private Worker worker;
    private SQLValidator validator;
    private String       sql;
    private ParamInfo[]  params;

    public static SQLMethod ofNullable() {
        return new SQLMethod() {
            @Override
            public Object invoke(Object[] args) throws SQLException {
                return null;
            }
        };
    }

    public Object invoke(Object[] args) throws SQLException {
        // apply parameters to sql
        // verify sql
        // execute the worker
        if (params.length != args.length) {
            throw new SQLException("Invalid parameter length: must be " + params.length);
        }

        String statement = sql;
        for (int i = 0; i < params.length; i++) {
            String nmParam = '{' + params[i].name + '}';
            if (!sql.contains(nmParam)) {
                throw new SQLException("Parameter pattern not found: " + nmParam);
            }

            Object value       = args[i];
            String replacement = value.toString();
            if (!(value instanceof String) && !(value instanceof Character)) {
                if (params[i].array) {
                    replacement = fromArray(value);
                }
                else if (params[i].typed) {
                    throw new UnsupportedOperationException("Typed parameters are not allowed");
                }
                else if (params[i].mapped) {
                    statement = fromMap(value);
                    continue;
                }
            } else {
                replacement = "'" + replacement + "'";
            }
            statement = statement.replace(nmParam, replacement);
        }

        if (getValidator() != null) {
            if (!getValidator().verify(statement)) {
                throw new SQLException("Could not verify SQL statement");
            }
        }

        return worker.invoke(statement);
    }

    private String fromArray(Object value) {
        StringBuilder sb = new StringBuilder(", ");

        for (int j = 0; j < Array.getLength(value); j++) {
            Object next = Array.get(value, j);

            String rp = next.toString();
            if (next instanceof String || next instanceof Character) {
                rp = "'" + rp + "'";
            }
            sb.append(rp);
        }
        return sb.toString();
    }

    private String fromMap(Object value) {
        Map<?, ?> map = (Map<?, ?>) value;
        String st = sql;

        for (Object key : map.keySet()) {
            String pattern = '{' + key.toString() + '}';
            if (st.contains(pattern)) {
                Object mappedValue = map.get(key);
                if (mappedValue instanceof String
                        || mappedValue instanceof Character) {
                    st = st.replace(pattern, "'" + mappedValue.toString() + "'");
                }
                else {
                    st = st.replace(pattern, String.valueOf(mappedValue));
                }
            }
        }
        return st;
    }

    public SQLValidator getValidator() {
        return validator;
    }

    public void setValidator(SQLValidator validator) {
        this.validator = validator;
    }

    public void setParams(ParamInfo[] params) {
        this.params = params;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public static interface Worker {
        public Object invoke(String sql) throws SQLException;
    }

    public static class ParamInfo {
        boolean typed;
        boolean mapped;
        boolean array;

        String name;
    }
}
