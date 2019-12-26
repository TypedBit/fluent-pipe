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
package io.github.typedbit.fluentpipe;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of Pipe connecting a {@link Consumer} with a {@link Function}. The {@link Consumer} is writing to a {@link Writer} and the {@link Function} is reading from a
 * {@link Reader} which in fact will read the bytes written by the {@link Consumer}.
 * <p>
 * This implementation executes {@link Function#apply(Object)} of {@code readerMapper} <b>asynchronously</b> on the given {@link Executor}. {@link Consumer#accept(Object)}
 * operation of {@code writerConsumer} is executed <b>asynchronously</b> on the given {@link Executor}.
 * </p>
 * <p>
 * Both methods are invoked only after {@link #get()} method of this {@link Supplier} is being called.
 * </p>
 * 
 * @author Dieter König
 */
public class CompletableReaderWriterPipe<T> extends AbstractPipe implements Supplier<CompletableFuture<T>> {

	private final int pipeSize;

	private final Executor readExecutor;

	private final Executor writeExecutor;

	private final Consumer<Writer> writerConsumer;

	private final Function<Reader, T> readerMapper;

	/**
	 * Configures this pipe so that it is ready to be used as {@link Supplier} of {@link CompletableFuture} to execute the piped processing.
	 * <p>
	 * If {@link #readExecutor} and {@link #writeExecutor} are the same than the used implementation should be able to spawn at least 2 threads otherwise the pipe will block
	 * infinitely. Don't use direct implementation for both {@link Executor}.
	 * </p>
	 * 
	 * @param pipeSize
	 *            The size of pipe buffer to use. If the provided value is negative or zero then this implementation falls back to default size usage.
	 * @param readExecutor
	 *            The {@link Executor} to use for the read operations on {@link Reader}.
	 * @param writeExecutor
	 *            The {@link Executor} to use for the write operations on {@link Writer}.
	 * @param writerConsumer
	 *            The {@link Consumer} implementing the write operation on {@link Writer}.
	 * @param readerMapper
	 *            {@link Function} which will be used by the pipe to process the {@link Reader} and produce a result.
	 */
	public CompletableReaderWriterPipe(final int pipeSize, final Executor readExecutor, final Executor writeExecutor, final Consumer<Writer> writerConsumer, final Function<Reader, T> readerMapper) {
		this.pipeSize = pipeSize;
		this.readExecutor = Objects.requireNonNull(readExecutor);
		this.writeExecutor = Objects.requireNonNull(writeExecutor);
		this.writerConsumer = Objects.requireNonNull(writerConsumer);
		this.readerMapper = Objects.requireNonNull(readerMapper);
	}

	@Override
	public CompletableFuture<T> get() {
		try {
			// build up a pipe
			final PipedWriter pipedWriter = new PipedWriter();
			final PipedReader pipedReader = pipeSize > 0 ? new PipedReader(pipedWriter, pipeSize) : new PipedReader(pipedWriter);

			// submit write task to given Executor
			final CompletableFuture<Void> writeFuture = CompletableFuture.runAsync(writerRunnable(pipedWriter, writerConsumer), writeExecutor);

			// submit read task to given Executor
			final CompletableFuture<T> readFuture = CompletableFuture.supplyAsync(() -> {
				// close reader in both cases (success, failure) prior to death of thread otherwise pipe will be broken or another operation will block infinitely
				try (final Reader reader = pipedReader) {
					final T result = readerMapper.apply(reader);
					// check if async write operation failed with an exception and if so throw it
					try {
						writeFuture.get();
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException("error during writing to Writer", e);
					}
					return result;
				} catch (IOException e) {
					throw new RuntimeException("error closing Reader", e);
				}
			}, readExecutor);

			return readFuture;
		} catch (IOException e) {
			// setting up a pipe ('new PipedReader...') failed
			return CompletableFuture.failedFuture(e);
		}
	}

}
