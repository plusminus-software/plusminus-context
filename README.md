# plusminus-context

Implementation of a cross-cutting context.

Plusminus Context is a small set of Spring-friendly libraries for passing
cross-cutting values (current HTTP request, handler, transaction-bound objects,
etc.) through your application without threading them through method
signatures. The core abstraction is `Context<T>` — a typed, read-only holder. A value is bound only for the
duration of a block of code, like `java.lang.ScopedValue`, and `get()` never
returns `null`.

## Modules

| Module | Description |
| --- | --- |
| `plusminus-context` | Core abstractions: `Context<T>`, `Context.where(...).run(...)` to bind values, `Context.snapshot()` and a `ContextPropagationTaskDecorator` to pass values to other threads.  |
| `plusminus-scope` | `ScopeRunner` and `AroundScope` for wrapping a unit of work, publishing Spring application events (`ScopeStartedEvent`, `ScopeCompletedEvent`, `ScopeFailedEvent`, `ScopeFinalizedEvent` and their invocation counterparts). |
| `plusminus-http` | Servlet integration: `HttpFilter` runs each request as a scope and binds `HttpServletRequest`/`HttpServletResponse` contexts; `HttpInterceptor` exposes handler contexts and publishes invocation events. Beans are provided by `HttpContextAutoconfig`. |
| `plusminus-transactional` | `TransactionalContext<T>` whose values live for the duration of the current transaction, stored as a `TransactionSynchronizationManager` resource. |

## Working with contexts

A context is read with `get()`. A value is bound for a block of code with
`Context.where(...)` and is visible anywhere inside that block:

```java
Context<User> user = Context.of();

Context.where(user, currentUser).run(() -> {
    User value = user.get();
});

user.get(); // throws IllegalStateException("No active scope")
```

Several values can be bound at once, and the block can return a result:

```java
String name = Context.where(user, currentUser)
        .where(tenant, currentTenant)
        .call(() -> user.get().getName());
```

Blocks can be nested. An inner block sees the values of the outer one and can
replace them; the outer values are back when the inner block ends.

### Kinds of contexts

| Created with | Value | Works outside a block |
| --- | --- | --- |
| `Context.of()` | only the one bound with `where` | no |
| `Context.of(supplier)` | the supplier result, computed once per block | no |
| `Context.of(value)` | the given value | yes |
| `context.map(mapper)` | the mapper result, computed on every read | if the source context does |

A value bound with `where` always replaces the value of the context itself.
`Context.run(...)` and `Context.call(...)` open a block without binding anything,
which is enough for contexts created with a supplier.

```java
Context<Tenant> tenant = Context.of(() -> tenantService.current());
Context<String> tenantName = tenant.map(Tenant::getName);

Context.run(() -> {
    tenant.get();     // calls the supplier
    tenant.get();     // returns the same object
    tenantName.get(); // calls the mapper on every read
});
```

### Missing values

`get()` never returns `null`, for any kind of context:

| Case | Exception |
| --- | --- |
| No block is open | `IllegalStateException("No active scope")` |
| Nothing is bound, or the supplier or mapper returns `null` | `NoSuchElementException("No value in context")` |
| `Context.of(null)` or `where(context, null)` | `NullPointerException` |

### Other threads

Values are not visible in other threads. Take a snapshot and wrap the task:

```java
executor.submit(Context.snapshot().wrap(task));
```

The snapshot holds the values bound with `where`. `Context.snapshot(true)` also
copies the values already computed by suppliers. In a Spring application
`ContextPropagationTaskDecorator` does this for `@Async` methods and task
executors, and it is registered automatically.

### Custom contexts

`Context<T>` is a functional interface with one method to implement, `find()`.
It returns the context's own value, or `null` when there is none. Callers always
use `get()`, which first looks for a value bound with `where`, then calls
`find()`, and throws when both give nothing:

```java
Context<Locale> locale = LocaleContextHolder::getLocale;

locale.get(); // the current locale
Context.where(locale, Locale.FRENCH).run(() -> locale.get()); // Locale.FRENCH
```

### Transactional contexts

A transactional context lazily provides one value per active transaction:

```java
TransactionalContext<UUID> transactionId = TransactionalContext.of(UUID::randomUUID);
UUID current = transactionId.get(); // throws IllegalStateException("No active transaction") outside a transaction
```

An inner transaction started with `REQUIRES_NEW` gets its own value, and the
outer value is back when it ends. A transactional context does not need a block,
and a value bound with `where` replaces the transaction value inside the block.

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
