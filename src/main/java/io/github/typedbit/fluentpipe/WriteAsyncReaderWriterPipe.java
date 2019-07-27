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

import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Implementation of Pipe connecting two {@link Consumer}. One writing to a {@link Writer} and another one reading from a {@link Reader} which in fact will read the bytes written
 * by first {@link Consumer}.
 * <p>
 * This implementation executes {@link Consumer#accept(Object)} of {@code writerConsumer} <b>asynchronously</b> on the given {@link Executor}. {@link Consumer#accept(Object)}
 * operation of {@code readerConsumer} is executed directly through this {@link Callable} which can be executed on a thread of your choice.
 * </p>
 * <ul>
 * <li>Both {@link Consumer} are invoked only after {@link #call()} method of this {@link Callable} is being called.</li>
 * <li>Both {@link Consumer} must read/write until EOF is encountered.</li>
 * </ul>
 * 
 * @author Dieter König
 */
public class WriteAsyncReaderWriterPipe extends AbstractPipe implements Callable<Void> {

	private final int pipeSize;

	private final Executor executor;

	private final Consumer<Writer> writerConsumer;

	private final Consumer<Reader> readerConsumer;

	/**
	 * Configures this pipe so that it is ready to be used as {@link Callable} to execute the piped processing.
	 * 
	 * @param pipeSize
	 *            The size of pipe buffer to use. If the provided value is negative or zero then this implementation falls back to default size usage.
	 * @param executor
	 *            The {@link Executor} to use for the write operations on {@link Writer}.
	 * @param writerConsumer
	 *            The {@link Consumer} implementing the write operation on {@link Writer}.
	 * @param readerConsumer
	 *            The {@link Consumer} implementing the read operation on {@link Reader}.
	 */
	public WriteAsyncReaderWriterPipe(final int pipeSize, final Executor executor, final Consumer<Writer> writerConsumer, final Consumer<Reader> readerConsumer) {
		this.pipeSize = pipeSize;
		this.executor = Objects.requireNonNull(executor);
		this.writerConsumer = Objects.requireNonNull(writerConsumer);
		this.readerConsumer = Objects.requireNonNull(readerConsumer);
	}

	@Override
	public Void call() throws Exception {
		// build up a pipe
		try (final PipedReader pipedReader = pipeSize > 0 ? new PipedReader(pipeSize) : new PipedReader()) {
			try (final PipedWriter pipedWriter = new PipedWriter(pipedReader)) {

				// submit write task to given Executor
				final CompletableFuture<Void> future = CompletableFuture.runAsync(writerRunnable(pipedWriter, writerConsumer), executor);

				// close reader in both cases (success, failure) prior to death of thread otherwise pipe will be broken or another operation will block infinitely
				try (final Reader reader = pipedReader) {
					// execute reading in a thread where call() has been called
					readerConsumer.accept(reader);
				}
				// check if async write operation failed with an exception and if so throw it to mark pipe operation as failure
				return future.get();

			}

		}
	}

}
