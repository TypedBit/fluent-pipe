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
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder for a stream ({@link InputStream} and {@link OutputStream}) based pipe.
 * 
 * @author Dieter König
 */
public interface ConsumedOutputStreamPipeBuilder {

	/**
	 * Configures this builder to use the given {@link Consumer} for pipe construction.
	 * 
	 * @param inputStreamConsumer
	 *            {@link Consumer} which will be used by the pipe to process the {@link InputStream}
	 * @return configured builder instance
	 */
	public ConsumedConsumedPipeBuilder forInput(final Consumer<InputStream> inputStreamConsumer);

	/**
	 * Configures this builder to use the given {@link Function} for pipe construction.
	 * 
	 * @param inputStreamMapper
	 *            {@link Function} which will be used by the pipe to process the {@link InputStream} and produce a result.
	 * @param <T>
	 *            Type of the desired result object
	 * @return configured builder instance
	 */
	public <T> ConsumedMappedPipeBuilder<T> mapInput(final Function<InputStream, T> inputStreamMapper);

}
