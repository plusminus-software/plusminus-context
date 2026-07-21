# plusminus-context

Implementation of a cross-cutting context.

Plusminus Context is a small set of Spring-friendly libraries for passing
cross-cutting values (current HTTP request, handler, transaction-bound objects,
etc.) through your application without threading them through method
signatures. The core abstraction is `Context<T>` — a typed holder backed by a
`ThreadLocal`, with read-only, writable and clearable variants.

## Modules

| Module | Description |
| --- | --- |
| `plusminus-context` | Core abstractions: `Context<T>`, `WritableContext<T>`, `ClearableContext<T>` and a `ContextPropagationTaskDecorator` that propagates context values to tasks executed on other threads. |
| `plusminus-scope` | `ScopeRunner` and `AroundScope` for wrapping a unit of work, publishing Spring application events (`ScopeStartedEvent`, `ScopeCompletedEvent`, `ScopeFailedEvent`, `ScopeFinalizedEvent` and their invocation counterparts). |
| `plusminus-http` | Servlet integration: `HttpFilter` runs each request as a scope and fills `HttpServletRequest`/`HttpServletResponse` contexts; `HttpInterceptor` fills handler contexts and publishes invocation events. Beans are provided by `HttpContextAutoconfig`. |
| `plusminus-transactional` | `TransactionalContext<T>` whose values live for the duration of the current transaction, driven by a delegating `ContextTransactionManager`. |

## Working with contexts

Create a context from a value or a supplier and read it anywhere:

```java
WritableContext<String> context = WritableContext.of();
context.set("some value");
String value = context.get();
Optional<String> optional = context.optional();
```

A transactional context lazily provides one value per active transaction:

```java
TransactionalContext<UUID> transactionId = TransactionalContext.of(UUID::randomUUID);
UUID current = transactionId.get(); // fails outside an active transaction
```

## Getting started

Add the module you need, e.g. the core one:

```xml
<dependency>
    <groupId>software.plusminus</groupId>
    <artifactId>plusminus-context</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Use `plusminus-scope`, `plusminus-http` or `plusminus-transactional` as the
`artifactId` for the other modules.

## Building

Requires JDK 8.

```bash
./mvnw clean install
```

The build enforces code quality with Checkstyle, PMD, SpotBugs and JaCoCo
coverage checks.

## License

[Apache License, Version 2.0](LICENSE)
