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

/**
 * Used to set a specific name to the annotated parameter. This declaration
 * sets the column name to a parameter within a method.
 * <p>
 * The following example shows how to use this annotation on a parameter. It
 * is strongly recommended to use this annotation for declaring column names,
 * because the parameter names starting from {@code arg0} are used otherwise.
 * <p>
 * Arguments from methods that are annotated with {@link Param} can be
 * used to change the statement at runtime. The provided name has to be
 * present in the sql statement:
 * <pre>
 *      PATTERN := '{' NAME '}';
 * </pre>
 * Based on that pattern definition the sql statement can be defined as
 * follows:
 * <pre>
 *      &#064;SQL.Select("select * from {table}")
 *      //..
 * </pre>
 *
 * @since 1.0
 * @author MatrixEditor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Param {
    /**
     * @return the specified column name.
     */
    String value() default "";
}
