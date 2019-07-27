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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.typedbit.fluentpipe.builder.ConsumedConsumedPipeBuilder;
import io.github.typedbit.fluentpipe.builder.ConsumedMappedPipeBuilder;
import io.github.typedbit.fluentpipe.builder.ConsumedOutputStreamPipeBuilder;
import io.github.typedbit.fluentpipe.builder.InitialStreamPipeBuilder;
import io.github.typedbit.fluentpipe.builder.ReadExecutorDefinedPipeBuilder;
import io.github.typedbit.fluentpipe.builder.SizedStreamPipeBuilder;

/**
 * Fluent pipe builder accepting configuration for the pipe split in multiple steps and providing an instance of it in last step.
 * 
 * @author Dieter König
 */
public class StreamPipeBuilder implements InitialStreamPipeBuilder, SizedStreamPipeBuilder, ConsumedOutputStreamPipeBuilder {

	private static class MappedPipeBuilder<T> extends StreamPipeBuilder implements ConsumedMappedPipeBuilder<T>, ReadExecutorDefinedPipeBuilder<T> {

		private final Function<InputStream, T> inputStreamMapper;

		private MappedPipeBuilder(Function<InputStream, T> inputStreamMapper) {
			this.inputStreamMapper = Objects.requireNonNull(inputStreamMapper);
		}

		@Override
		public Supplier<CompletableFuture<T>> asyncWrite(Executor writeExecutor) {
			asyncWriteInternal(writeExecutor);
			return new CompletableStreamPipe<T>(getPipeSize(), getReadExecutor(), getWriteExecutor(), getOutputStreamConsumer(), inputStreamMapper);
		}

		@Override
		public ReadExecutorDefinedPipeBuilder<T> asyncRead(Executor readExecutor) {
			asyncReadInternal(readExecutor);
			return this;
		}

	}

	private static class ConsumedPipeBuilder extends StreamPipeBuilder implements ConsumedConsumedPipeBuilder, Supplier<Callable<Void>> {

		private final Consumer<InputStream> inputStreamConsumer;

		private ConsumedPipeBuilder(Consumer<InputStream> inputStreamConsumer) {
			this.inputStreamConsumer = Objects.requireNonNull(inputStreamConsumer);
		}

		public Supplier<Callable<Void>> asyncRead(final Executor readExecutor) {
			asyncReadInternal(readExecutor);
			return this;
		}

		public Supplier<Callable<Void>> asyncWrite(final Executor writeExecutor) {
			asyncWriteInternal(writeExecutor);
			return this;
		}

		@Override
		public Callable<Void> get() {
			if (getReadExecutor() != null) {
				return new ReadAsyncStreamPipe(getPipeSize(), getReadExecutor(), getOutputStreamConsumer(), inputStreamConsumer);
			}
			if (getWriteExecutor() != null) {
				return new WriteAsyncStreamPipe(getPipeSize(), getWriteExecutor(), getOutputStreamConsumer(), inputStreamConsumer);
			}
			throw new IllegalStateException("readExecutor and writeExecutor are not initialized");
		}

	}

	/**
	 * Returns a new instance of {@link InitialStreamPipeBuilder}.
	 * 
	 * @return {@link InitialStreamPipeBuilder}
	 */
	public static InitialStreamPipeBuilder create() {
		return new StreamPipeBuilder();
	}

	private Executor readExecutor;

	private Executor writeExecutor;

	private int pipeSize;

	private Consumer<OutputStream> outputStreamConsumer;

	private StreamPipeBuilder() {
		// hidden to ensure proper initialization via interfaces
	}

	public ConsumedConsumedPipeBuilder forInput(final Consumer<InputStream> inputStreamConsumer) {
		return (ConsumedPipeBuilder) new ConsumedPipeBuilder(inputStreamConsumer).pipeSize(pipeSize).forOutput(outputStreamConsumer);
	}

	private void checkOutput() {
		if (outputStreamConsumer != null) {
			throw new IllegalStateException("outputStreamConsumer was already initialized");
		}
	}

	@SuppressWarnings("unchecked")
	public <T> ConsumedMappedPipeBuilder<T> mapInput(final Function<InputStream, T> inputStreamMapper) {
		return (MappedPipeBuilder<T>) new MappedPipeBuilder<T>(inputStreamMapper).pipeSize(pipeSize).forOutput(outputStreamConsumer);
	}

	public ConsumedOutputStreamPipeBuilder forOutput(final Consumer<OutputStream> outputStreamConsumer) {
		checkOutput();
		this.outputStreamConsumer = Objects.requireNonNull(outputStreamConsumer);
		return this;
	}

	protected final void asyncReadInternal(final Executor readExecutor) {
		if (this.readExecutor != null) {
			throw new IllegalStateException("readExecutor was already initialized");
		}
		this.readExecutor = Objects.requireNonNull(readExecutor);
	}

	protected final void asyncWriteInternal(final Executor writeExecutor) {
		if (this.writeExecutor != null) {
			throw new IllegalStateException("writeExecutor was already initialized");
		}
		this.writeExecutor = Objects.requireNonNull(writeExecutor);
	}

	@Override
	public SizedStreamPipeBuilder pipeSize(int pipeSize) {
		this.pipeSize = pipeSize;
		return this;
	}

	protected Executor getReadExecutor() {
		return readExecutor;
	}

	protected Executor getWriteExecutor() {
		return writeExecutor;
	}

	protected int getPipeSize() {
		return pipeSize;
	}

	protected Consumer<OutputStream> getOutputStreamConsumer() {
		return outputStreamConsumer;
	}

}
