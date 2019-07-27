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

import java.io.OutputStream;
import java.io.Writer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * Builder for a pipe.
 * 
 * @author Dieter König
 */
public interface ReadExecutorDefinedPipeBuilder<T> {

	/**
	 * Configures this builder to use {@link ForkJoinPool#commonPool()} as {@link Executor} for the write operations on {@link OutputStream} resp. {@link Writer}. Call to
	 * {@link Supplier#get()} method on the returned {@link Supplier} will start the piped processing.
	 * 
	 * @return {@link Supplier}
	 */
	default public Supplier<CompletableFuture<T>> asyncWrite() {
		return asyncWrite(ForkJoinPool.commonPool());
	}

	/**
	 * Configures this builder to use the given {@link Executor} for the write operations on {@link OutputStream} resp. {@link Writer}. Call to {@link Supplier#get()} method on the
	 * returned {@link Supplier} will start the piped processing.
	 * 
	 * @param writeExecutor
	 *            The {@link Executor} to use for the write operations on {@link OutputStream} resp. {@link Writer}.
	 * @return {@link Supplier}
	 */
	public Supplier<CompletableFuture<T>> asyncWrite(final Executor writeExecutor);

}
