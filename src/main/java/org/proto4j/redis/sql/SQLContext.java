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

/**
 * The base class for objects that implement the behaviour of an SQLContext. This
 * class is used within the instance creation process and should not be used
 * at any other place.
 *
 * @see SQLExtractor
 * @author MatrixEditor
 */
public interface SQLContext {

    /**
     * @return the current data source
     */
    SQLSource getSource();

    /**
     * @return the declaring class of the called API method.
     */
    Class<?> getAPIServiceType();

    /**
     * @return a wrapper object for the top level reference.
     */
    Object getReference();

    /**
     * Sets the reference to the given value.
     *
     * @param reference a wrapper object for the top level reference
     */
    void setReference(Object reference);

    /**
     * @return the declaring class of {@link #getReference()}.
     */
    Class<?> getReferenceParent();

    /**
     * Sets the declaring class of {@link #getReference()}.
     *
     * @param cls the defined class instance
     */
    void setReferenceParent(Class<?> cls);
}
