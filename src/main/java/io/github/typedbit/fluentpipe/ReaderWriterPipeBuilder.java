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

import java.io.Reader;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.typedbit.fluentpipe.builder.ConsumedConsumedPipeBuilder;
import io.github.typedbit.fluentpipe.builder.ConsumedMappedPipeBuilder;
import io.github.typedbit.fluentpipe.builder.ConsumedWriterPipeBuilder;
import io.github.typedbit.fluentpipe.builder.InitialReaderWriterPipeBuilder;
import io.github.typedbit.fluentpipe.builder.InitialStreamPipeBuilder;
import io.github.typedbit.fluentpipe.builder.ReadExecutorDefinedPipeBuilder;
import io.github.typedbit.fluentpipe.builder.SizedReaderWriterPipeBuilder;

/**
 * Fluent pipe builder accepting configuration for the pipe split in multiple steps and providing an instance of it in last step.
 * 
 * @author Dieter König
 */
public class ReaderWriterPipeBuilder implements InitialReaderWriterPipeBuilder, SizedReaderWriterPipeBuilder, ConsumedWriterPipeBuilder {

	private static class MappedPipeBuilder<T> extends ReaderWriterPipeBuilder implements ConsumedMappedPipeBuilder<T>, ReadExecutorDefinedPipeBuilder<T> {

		private final Function<Reader, T> readerMapper;

		private MappedPipeBuilder(Function<Reader, T> readerMapper) {
			this.readerMapper = Objects.requireNonNull(readerMapper);
		}

		@Override
		public Supplier<CompletableFuture<T>> asyncWrite(Executor writeExecutor) {
			asyncWriteInternal(writeExecutor);
			return new CompletableReaderWriterPipe<T>(getPipeSize(), getReadExecutor(), getWriteExecutor(), getWriterConsumer(), readerMapper);
		}

		@Override
		public ReadExecutorDefinedPipeBuilder<T> asyncRead(Executor readExecutor) {
			asyncReadInternal(readExecutor);
			return this;
		}

	}

	private static class ConsumedPipeBuilder extends ReaderWriterPipeBuilder implements ConsumedConsumedPipeBuilder, Supplier<Callable<Void>> {

		private final Consumer<Reader> readerConsumer;

		private ConsumedPipeBuilder(Consumer<Reader> readerConsumer) {
			this.readerConsumer = Objects.requireNonNull(readerConsumer);
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
				return new ReadAsyncReaderWriterPipe(getPipeSize(), getReadExecutor(), getWriterConsumer(), readerConsumer);
			}
			if (getWriteExecutor() != null) {
				return new WriteAsyncReaderWriterPipe(getPipeSize(), getWriteExecutor(), getWriterConsumer(), readerConsumer);
			}
			throw new IllegalStateException("readExecutor and writeExecutor are not initialized");
		}

	}

	/**
	 * Returns a new instance of {@link InitialStreamPipeBuilder}.
	 * 
	 * @return {@link InitialStreamPipeBuilder}
	 */
	public static InitialReaderWriterPipeBuilder create() {
		return new ReaderWriterPipeBuilder();
	}

	private Executor readExecutor;

	private Executor writeExecutor;

	private int pipeSize;

	private Consumer<Writer> writerConsumer;

	private ReaderWriterPipeBuilder() {
		// hidden to ensure proper initialization via interfaces
	}

	public ConsumedConsumedPipeBuilder forReader(final Consumer<Reader> readerConsumer) {
		return (ConsumedPipeBuilder) new ConsumedPipeBuilder(readerConsumer).pipeSize(pipeSize).forWriter(writerConsumer);
	}

	private void checkOutput() {
		if (writerConsumer != null) {
			throw new IllegalStateException("outputStreamConsumer was already initialized");
		}
	}

	@SuppressWarnings("unchecked")
	public <T> ConsumedMappedPipeBuilder<T> mapReader(final Function<Reader, T> readerMapper) {
		return (MappedPipeBuilder<T>) new MappedPipeBuilder<T>(readerMapper).pipeSize(pipeSize).forWriter(writerConsumer);
	}

	public ConsumedWriterPipeBuilder forWriter(final Consumer<Writer> writerConsumer) {
		checkOutput();
		this.writerConsumer = Objects.requireNonNull(writerConsumer);
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
	public SizedReaderWriterPipeBuilder pipeSize(int pipeSize) {
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

	protected Consumer<Writer> getWriterConsumer() {
		return writerConsumer;
	}

}
