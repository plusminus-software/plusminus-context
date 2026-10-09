# plusminus-context

Implementation of a cross-cutting context.

Plusminus Context is a small set of Spring-friendly libraries for passing
cross-cutting values (current HTTP request, handler, transaction-bound objects,
etc.) through your application without threading them through method
signatures. The core abstraction is `Context<T>` — a typed, read-only holder. A value is bound only for the
duration of a block of code, like `java.lang.ScopedValue`.

## Modules

| Module | Description |
| --- | --- |
| `plusminus-context` | Core abstractions: `Context<T>`, `Context.where(...).run(...)` to bind values, `Context.snapshot()` and a `ContextPropagationTaskDecorator` to pass values to other threads.  |
| `plusminus-scope` | `ScopeRunner` and `AroundScope` for wrapping a unit of work, publishing Spring application events (`ScopeStartedEvent`, `ScopeCompletedEvent`, `ScopeFailedEvent`, `ScopeFinalizedEvent` and their invocation counterparts). |
| `plusminus-http` | Servlet integration: `HttpFilter` runs each request as a scope and binds `HttpServletRequest`/`HttpServletResponse` contexts; `HttpInterceptor` exposes handler contexts and publishes invocation events. Beans are provided by `HttpContextAutoconfig`. |
| `plusminus-transactional` | `TransactionalContext<T>` whose values live for the duration of the current transaction, stored as a `TransactionSynchronizationManager` resource. |

## Working with contexts

Create a context, bind a value for a block of code and read it anywhere inside:

```java
Context<User> user = Context.of();

Context.where(user, () -> currentUser).run(() -> {
    User value = user.get();
});

user.optional(); // empty outside of the block
user.get();      // throws NoSuchElementException outside of the block
```

`Context.local()` creates a context that is not copied to other threads.
To run a task in another thread with the current values:

```java
executor.submit(Context.snapshot().wrap(task));
```

A transactional context lazily provides one value per active transaction:

```java
TransactionalContext<UUID> transactionId = TransactionalContext.of(UUID::randomUUID);
UUID current = transactionId.get(); // throws outside an active transaction
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
