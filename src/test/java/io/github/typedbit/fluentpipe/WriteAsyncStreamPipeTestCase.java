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
import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link WriteAsyncStreamPipe} class.
 * 
 * @author Dieter König
 */
public class WriteAsyncStreamPipeTestCase {

	@Test
	public void testByBuilder() throws Exception {
		StreamPipeBuilder

				.create()

				.defaultPipeSize()

				.forOutput((o) -> {
					try (final InputStream input = WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml")) {
						final byte[] buffer = new byte[8192];
						int len;
						while ((len = input.read(buffer)) != -1) {
							o.write(buffer, 0, len);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})

				.forInput((i) -> {
					try (final InputStream input = WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml")) {
						final byte[] expecteds = new byte[8192];
						final byte[] actuals = new byte[8192];
						int expectedLength;
						while ((expectedLength = input.read(expecteds)) != -1) {
							int actualLength = i.read(actuals);
							Assertions.assertEquals(expectedLength, actualLength);
							Assertions.assertArrayEquals(expecteds, actuals);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})

				.asyncWrite()

				.get()

				.call();

	}

	@Test
	public void testByConstructor() throws Exception {
		new WriteAsyncStreamPipe(0, ForkJoinPool.commonPool(), (o) -> {
			try (final InputStream input = WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml")) {
				byte[] buffer = new byte[8192];
				int len;
				while ((len = input.read(buffer)) != -1) {
					o.write(buffer, 0, len);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, (i) -> {
			try (final InputStream input = WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml")) {
				byte[] expecteds = new byte[8192];
				byte[] actuals = new byte[8192];
				int expectedLength;
				while ((expectedLength = input.read(expecteds)) != -1) {
					int actualLength = i.read(actuals);
					Assertions.assertEquals(expectedLength, actualLength);
					Assertions.assertArrayEquals(expecteds, actuals);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		})

				.call();

	}

}
