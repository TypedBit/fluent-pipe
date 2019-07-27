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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.function.Consumer;

/**
 * Abstract class providing common implementations for pipes acting upon IO streams or readers and writers.
 * 
 * @author Dieter König
 */
abstract class AbstractPipe {

	protected final Runnable inputRunnable(final InputStream pipedInput, final Consumer<InputStream> inputStreamConsumer) {
		return () -> {
			// close stream in both cases (success, failure) prior to death of thread otherwise pipe will be broken or another operation will block infinitely
			try (final InputStream input = pipedInput) {
				inputStreamConsumer.accept(input);
			} catch (IOException e) {
				throw new RuntimeException("error closing InputStream", e);
			}
		};
	}

	protected final Runnable outputRunnable(final OutputStream pipedOutput, final Consumer<OutputStream> outputStreamConsumer) {
		return () -> {
			// close stream in both cases (success, failure) prior to death of thread otherwise pipe will be broken or another operation will block infinitely
			try (final OutputStream output = pipedOutput) {
				outputStreamConsumer.accept(output);
			} catch (IOException e) {
				throw new RuntimeException("error closing OutputStream", e);
			}
		};
	}

	protected final Runnable readerRunnable(final Reader pipedReader, final Consumer<Reader> readerConsumer) {
		return () -> {
			// close reader in both cases (success, failure) prior to death of thread otherwise pipe will be broken or another operation will block infinitely
			try (final Reader reader = pipedReader) {
				readerConsumer.accept(reader);
			} catch (IOException e) {
				throw new RuntimeException("error closing Reader", e);
			}
		};
	}

	protected final Runnable writerRunnable(final Writer pipedWriter, final Consumer<Writer> writerConsumer) {
		return () -> {
			// close writer in both cases (success, failure) prior to death of thread otherwise pipe will be broken or another operation will block infinitely
			try (final Writer writer = pipedWriter) {
				writerConsumer.accept(writer);
			} catch (IOException e) {
				throw new RuntimeException("error closing Writer", e);
			}
		};
	}

}
