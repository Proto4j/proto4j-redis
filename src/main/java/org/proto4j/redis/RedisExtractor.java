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

package org.proto4j.redis;//@date 31.08.2022

import org.proto4j.redis.sql.SQLExtractor;

/**
 * A specific type of extractor used by the utility class {@link Extractors}
 * to filter registered extractors and create new instances based on the
 * given type;
 *
 * @param <T> the type that should be extracted
 */
public interface RedisExtractor<T> extends SQLExtractor<T> {

    /**
     * Retrieves whether this extractor can handle the given type.
     *
     * @param cls a class instance specifying the type that should be
     *         extracted.
     * @return {@code true} if accepted, otherwise {@code false}
     */
    public boolean accept(Class<?> cls);

    /**
     * Creates a new extractor based on the given type.
     *
     * @param cls a class instance specifying the type that should be
     *         extracted.
     * @param <E> the next return type
     * @return a new RedisExtractor instance for the given type
     */
    public <E> RedisExtractor<E> newInstance(Class<E> cls);
}
