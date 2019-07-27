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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Implementation of Pipe connecting two {@link Consumer}. One writing to an {@link OutputStream} and another one reading from an {@link InputStream} which in fact will read the
 * bytes written by first {@link Consumer}.
 * <p>
 * This implementation executes {@link Consumer#accept(Object)} of {@code outputStreamConsumer} <b>asynchronously</b> on the given {@link Executor}. {@link Consumer#accept(Object)}
 * operation of {@code inputStreamConsumer} is executed directly through this {@link Callable} which can be executed on a thread of your choice.
 * </p>
 * <ul>
 * <li>Both {@link Consumer} are invoked only after {@link #call()} method of this {@link Callable} is being called.</li>
 * <li>Both {@link Consumer} must read/write the stream until EOF is encountered.</li>
 * </ul>
 * 
 * @author Dieter König
 */
public class WriteAsyncStreamPipe extends AbstractPipe implements Callable<Void> {

	private final int pipeSize;

	private final Executor executor;

	private final Consumer<OutputStream> outputStreamConsumer;

	private final Consumer<InputStream> inputStreamConsumer;

	/**
	 * Configures this pipe so that it is ready to be used as {@link Callable} to execute the piped processing.
	 * 
	 * @param pipeSize
	 *            The size of pipe buffer to use. If the provided value is negative or zero then this implementation falls back to default size usage.
	 * @param executor
	 *            The {@link Executor} to use for the write operations on {@link OutputStream}.
	 * @param outputStreamConsumer
	 *            The {@link Consumer} implementing the write operation on {@link OutputStream}.
	 * @param inputStreamConsumer
	 *            The {@link Consumer} implementing the read operation on {@link InputStream}.
	 */
	public WriteAsyncStreamPipe(final int pipeSize, final Executor executor, final Consumer<OutputStream> outputStreamConsumer, final Consumer<InputStream> inputStreamConsumer) {
		this.pipeSize = pipeSize;
		this.executor = Objects.requireNonNull(executor);
		this.outputStreamConsumer = Objects.requireNonNull(outputStreamConsumer);
		this.inputStreamConsumer = Objects.requireNonNull(inputStreamConsumer);
	}

	@Override
	public Void call() throws Exception {
		// build up a pipe
		try (final PipedInputStream pipedInput = pipeSize > 0 ? new PipedInputStream(pipeSize) : new PipedInputStream()) {
			try (final PipedOutputStream pipedOutput = new PipedOutputStream(pipedInput)) {

				// submit write task to given Executor
				final CompletableFuture<Void> future = CompletableFuture.runAsync(outputRunnable(pipedOutput, outputStreamConsumer), executor);

				// close stream in both cases (success, failure) prior to death of thread otherwise pipe will be broken or another operation will block infinitely
				try (final InputStream input = pipedInput) {
					// execute reading in a thread where call() has been called
					inputStreamConsumer.accept(input);
				}
				// check if async write operation failed with an exception and if so throw it to mark pipe operation as failure
				return future.get();

			}

		}
	}

}
