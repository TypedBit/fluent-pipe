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
import java.io.InputStreamReader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CompletableReaderWriterPipe}
 * 
 * @author Dieter König
 */
public class CompletableReaderWriterPipeTestCase {

	@Test
	public void testByBuilder() throws Exception {
		ReaderWriterPipeBuilder

				.create()

				.defaultPipeSize()

				.forWriter((w) -> {
					try (final InputStreamReader reader = new InputStreamReader(WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml"))) {
						final char[] buffer = new char[8192];
						int len;
						while ((len = reader.read(buffer)) != -1) {
							w.write(buffer, 0, len);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})

				.mapReader((r) -> {
					try (final InputStreamReader reader = new InputStreamReader(WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml"))) {
						final char[] expecteds = new char[8192];
						final char[] actuals = new char[8192];
						int expectedLength;
						while ((expectedLength = reader.read(expecteds)) != -1) {
							int actualLength = r.read(actuals);
							Assertions.assertEquals(expectedLength, actualLength);
							Assertions.assertArrayEquals(expecteds, actuals);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return null;
				})

				.asyncRead()

				.asyncWrite()

				.get()

				.get();
	}

	@Test
	public void testByBuilderAwaitRead() throws Exception {
		ReaderWriterPipeBuilder

				.create()

				.defaultPipeSize()

				.forWriter((w) -> {
					try (final InputStreamReader reader = new InputStreamReader(WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml"))) {
						final char[] buffer = new char[16];
						int len;
						while ((len = reader.read(buffer)) != -1) {
							w.write(buffer, 0, len);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})

				.mapReader((r) -> {
					try (final InputStreamReader reader = new InputStreamReader(WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml"))) {
						final char[] expecteds = new char[16];
						final char[] actuals = new char[16];
						int expectedLength;
						while ((expectedLength = reader.read(expecteds)) != -1) {
							int actualLength = r.read(actuals);
							Assertions.assertEquals(expectedLength, actualLength);
							Assertions.assertArrayEquals(expecteds, actuals);
							Thread.sleep(100);
						}
					} catch (IOException | InterruptedException e) {
						throw new RuntimeException(e);
					}
					return null;
				})

				.asyncRead()

				.asyncWrite()

				.get()

				.get();
	}

	@Test
	public void testByBuilderAwaitWrite() throws Exception {
		ReaderWriterPipeBuilder

				.create()

				.defaultPipeSize()

				.forWriter((w) -> {
					try (final InputStreamReader reader = new InputStreamReader(WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml"))) {
						final char[] buffer = new char[16];
						int len;
						while ((len = reader.read(buffer)) != -1) {
							w.write(buffer, 0, len);
							Thread.sleep(100);
						}
					} catch (IOException | InterruptedException e) {
						throw new RuntimeException(e);
					}
				})

				.mapReader((r) -> {
					try (final InputStreamReader reader = new InputStreamReader(WriteAsyncStreamPipeTestCase.class.getResourceAsStream("test1.xml"))) {
						final char[] expecteds = new char[16];
						final char[] actuals = new char[16];
						int expectedLength;
						while ((expectedLength = reader.read(expecteds)) != -1) {
							int actualLength = r.read(actuals);
							Assertions.assertEquals(expectedLength, actualLength);
							Assertions.assertArrayEquals(expecteds, actuals);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return null;
				})

				.asyncRead()

				.asyncWrite()

				.get()

				.get();
	}

}
