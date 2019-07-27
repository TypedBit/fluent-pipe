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

import java.io.Reader;
import java.io.Writer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder for a stream ({@link Reader} & {@link Writer}) based pipe.
 * 
 * @author Dieter König
 */
public interface ConsumedWriterPipeBuilder {

	/**
	 * Configures this builder to use the given {@link Consumer} for pipe construction.
	 * 
	 * @param readerConsumer
	 *            {@link Consumer} which will be used by the pipe to process the {@link Reader}
	 * @return configured builder instance
	 */
	public ConsumedConsumedPipeBuilder forReader(final Consumer<Reader> readerConsumer);

	/**
	 * Configures this builder to use the given {@link Function} for pipe construction.
	 * 
	 * @param readerMapper
	 *            {@link Function} which will be used by the pipe to process the {@link Reader} and produce a result.
	 * @return configured builder instance
	 */
	public <T> ConsumedMappedPipeBuilder<T> mapReader(final Function<Reader, T> readerMapper);

}
