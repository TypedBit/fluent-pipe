[![Build Status](https://travis-ci.com/TypedBit/fluent-pipe.svg?branch=master)](https://travis-ci.com/TypedBit/fluent-pipe)
# fluent-pipe
Production grade ease of use fail-safe modularized fluent style framework providing a pipe, i.e. in case of stream variant a `PipedInputStream` connected with a `PipedOutputStream` in right manner.

## What is it for?
Pipe is a very low memory consuming possibility to transform data which is read from somewhere providing you an `InputStream` respectively `Reader` again after you transformation is applied. Similar holds for data which is written to somewhere. A typical example is an application of a **XSLT** to a **XML**. Also things like `deepCopy` where you maybe want to serialize some object and deserialize that serialized representation back to an object can be easily implemented using a pipe. 

## Why should you use it?
`PipedInputStream` and `PipedOutputStream` respectively `PipedReader` and `PipedWriter` can only be used with 2 different threads. That multi-threaded nature requires the programmer to take care of different aspects during writing the code using them.

Otherwise you may end up with things like *broken pipe* or *infinite blocking*.

Here [fluent-pipe](https://github.com/TypedBit/fluent-pipe) comes in and cares about the right way of building a pipe. This let you focus on your business logic.

## Requirements
* Java 9
* Maven
* Apache License Version 2.0

## Features
* uses Java Module System providing a module **without** additional dependencies
* fluent style fail-safe builder (each choice of configuration methods only once available)
* obtain pipe as default java interfaces:
    * `java.util.concurrent.Callable`
    * `java.util.concurrent.CompletableFuture`
* obtain `java.util.function.Supplier` for your configured pipe and reuse logic defined only once
* `java.util.concurrent.Callable` is also reusable
* construct pipe instances by just one constructor call if you wish
* Support of `InputSteam`/`OutputStream` API
* Support of `Reader`/`Writer` API

## Usage
> Write to the given OutputStream in some way. You don't need to worry about closing the OutputStream.

> Read from the given InputStream in some way. You don't need to worry about closing the InputStream.

> Your read & write logic MUST be reusable if you want to reuse it over the `Supplier` interface or if you want to reuse the same `Callable` instance.

Here are some usage examples:
##### Define and execute immediately, await blocking

```java
StreamPipeBuilder.create().defaultPipeSize()
	.forOutput((o) -> {/* this example does nothing here with OutputStream */})
	.forInput((i) -> {/* this example does nothing here with InputStream */})
	.asyncWrite().get().call();
```
##### Define and execute later, await blocking

```java
final Callable<Void> fluentPipe = StreamPipeBuilder.create().defaultPipeSize()
	.forOutput((o) -> {/* this example does nothing here with OutputStream */})
	.forInput((i) -> {/* this example does nothing here with InputStream */})
	.asyncWrite().get();
	
// do some stuff, later...
fluentPipe.call();	
```

##### Define and reuse to execute same logic multiple times (if it is reusable)

```java
final Supplier<Callable<Void>> fluentPipeSupplier =
	StreamPipeBuilder.create().defaultPipeSize()
	.forOutput((o) -> {/* this example does nothing here with OutputStream */})
	.forInput((i) -> {/* this example does nothing here with InputStream */})
	.asyncWrite();
	
// do some stuff, later...
fluentPipeSupplier.get().call();
// and later again
fluentPipeSupplier.get().call();
```

##### Define and execute both operations asynchronous (read produces result), execute immediately, await result immediately blocking (CompletableFuture-way also possible)

```java
MyObject result = StreamPipeBuilder.create().defaultPipeSize()
	.forOutput((o) -> {/* this example does nothing here with OutputStream */})
	.mapInput((i) -> {
		// this example does nothing here with InputStream
		// and produces null as result object
		return null;
	})
	.asyncRead().asyncWrite().get().get();
```

##### Define and execute both operations asynchronous (read produces result), execute immediately, read result later

```java
CompletableFuture<MyObject> fluentPipe = StreamPipeBuilder.create().defaultPipeSize()
	.forOutput((o) -> {/* this example does nothing here with OutputStream */})
	.mapInput((i) -> {
		// this example does nothing here with InputStream
		// and produces null as result object
		return null;
	})
	.asyncRead().asyncWrite().get();
	
// do some stuff, later...
MyObject result = fluentPipe.get();
// or the CompletableFuture-way
fluentPipe.thenApply((pipeResult)->{return null;}).thenAccept(...)...;
```