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
import java.io.PipedInputStream;

/**
 * Builder for a stream ({@link InputStream} and {@link OutputStream}) based pipe.
 * 
 * @author Dieter König
 */
public interface InitialStreamPipeBuilder {

	/**
	 * Configures this builder to use {@link PipedInputStream} with its default pipeSize as defined by the jvm.
	 * 
	 * @return {@link SizedStreamPipeBuilder}
	 */
	default public SizedStreamPipeBuilder defaultPipeSize() {
		return pipeSize(0);
	}

	/**
	 * Configures this builder to use given pipeSize to instantiate a {@link PipedInputStream}. If the provided value is negative or zero then this implementation falls back to default
	 * size usage.
	 * 
	 * @param pipeSize
	 *            The size of pipe buffer to use.
	 * @return {@link SizedStreamPipeBuilder}
	 */
	public SizedStreamPipeBuilder pipeSize(int pipeSize);

}
