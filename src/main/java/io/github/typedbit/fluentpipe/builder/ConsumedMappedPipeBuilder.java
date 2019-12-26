/*
 * Copyright 2019 Dieter König
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package io.github.typedbit.fluentpipe.builder;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Builder for a stream ({@link InputStream} and {@link OutputStream}) or {@link Reader} and {@link Writer} based pipe.
 * 
 * @author Dieter König
 */
public interface ConsumedMappedPipeBuilder<T> {

	/**
	 * Configures this builder to use {@link ForkJoinPool#commonPool()} as {@link Executor} for the read operations on {@link InputStream} resp. {@link Reader}.
	 * 
	 * @return {@link ReadExecutorDefinedPipeBuilder}
	 */
	default public ReadExecutorDefinedPipeBuilder<T> asyncRead() {
		return asyncRead(ForkJoinPool.commonPool());
	}

	/**
	 * Configures this builder to use the given {@link Executor} for the read operations on {@link InputStream} resp. {@link Reader}.
	 * 
	 * @param readExecutor
	 *            The {@link Executor} to use for the read operations on {@link InputStream} resp. {@link Reader}.
	 * @return {@link ReadExecutorDefinedPipeBuilder}
	 */
	public ReadExecutorDefinedPipeBuilder<T> asyncRead(final Executor readExecutor);

}
