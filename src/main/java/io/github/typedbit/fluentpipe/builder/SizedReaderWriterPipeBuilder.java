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

/**
 * Builder for a stream ({@link Reader} & {@link Writer}) based pipe.
 * 
 * @author Dieter König
 */
public interface SizedReaderWriterPipeBuilder {

	/**
	 * Configures this builder to use given {@link Consumer} as write operation on {@link Writer}.
	 * 
	 * @param writerConsumer
	 *            The {@link Consumer} implementing the write operation on {@link Writer}.
	 * @return {@link ConsumedWriterPipeBuilder}
	 */
	public ConsumedWriterPipeBuilder forWriter(final Consumer<Writer> writerConsumer);

}
