# plusminus-context

Implementation of a cross-cutting context.

Plusminus Context is a small set of Spring-friendly libraries for passing
cross-cutting values (current HTTP request, handler, transaction-bound objects,
etc.) through your application without threading them through method
signatures. The core abstraction is `Context<T>` — a typed, read-only holder. A value is bound only for the
duration of a block of code, like `java.lang.ScopedValue`. When nothing is
bound, the context gives its own value.

## Modules

| Module | Description |
| --- | --- |
| `plusminus-context` | Core abstractions: `Context<T>`, `Context.where(...).run(...)` to bind values, `Context.snapshot()` and a `ContextPropagationTaskDecorator` to pass values to other threads.  |
| `plusminus-scope` | `ScopeRunner` and `AroundScope` for wrapping a unit of work, publishing Spring application events (`ScopeStartedEvent`, `ScopeCompletedEvent`, `ScopeFailedEvent`, `ScopeFinalizedEvent` and their invocation counterparts). |
| `plusminus-http` | Servlet integration: `HttpFilter` runs each request as a scope and binds `HttpServletRequest`/`HttpServletResponse` contexts; `HttpInterceptor` exposes handler contexts and publishes invocation events. Beans are provided by `HttpContextAutoconfig`. |
| `plusminus-transactional` | `TransactionalContext.of(supplier)` creates a context whose values live for the duration of the current transaction, stored as a `TransactionSynchronizationManager` resource. |

## Working with contexts

A context is read with `get()`. A value bound with `where` comes first;
otherwise the context gives its own value. A value is bound for a block of code
with `Context.where(...)` and is visible anywhere inside that block:

```java
Context<User> user = Context.required();

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

Blocks can be nested. An inner block sees the bound values of the outer one and
can replace them; the outer values are back when the inner block ends.

### Kinds of contexts

| Created with | Own value | Cached in the block |
| --- | --- | --- |
| `Context.required()` | none, so it must be bound with `where` | — |
| `Context.of(supplier)` | the supplier result | yes |
| `Context.constant(value)` | the given value | no |
| `Context.dynamic(supplier)` | the supplier result, computed on every read | no |
| `context.map(mapper)` | the mapper result, computed on every read | no |
| a lambda or a custom class | the result of its `supply()` | yes |
| `TransactionalContext.of(supplier)` | the supplier result, once per transaction | no |

A value bound with `where` always replaces the own value, for every kind of
context. Outside a block every context except `Context.required()` works too, but
nothing is cached: `Context.of(supplier)` calls the supplier on every read. So a
supplier that must run only once (it opens a connection, creates an id, calls a
service) must be bound with `where(context)` at the entry point.

`Context.run(...)` and `Context.call(...)` open a block without binding anything,
which is enough for cached contexts:

```java
Context<Tenant> tenant = Context.of(() -> tenantService.current());
Context<String> tenantName = tenant.map(Tenant::getName);

Context.run(() -> {
    tenant.get();     // calls the supplier
    tenant.get();     // returns the same object
    tenantName.get(); // calls the mapper on every read
});
```

### Bound values and cached values

- A value bound with `where` is the same everywhere inside the block: in inner
  blocks and in tasks wrapped with a snapshot.
- A cached value belongs to one block in one thread. An inner block or another
  thread calls the supplier again and may get a different value. This keeps
  values correct when an inner block binds another value that the supplier reads.

So a value that must stay the same, like a request id, is bound at the entry
point. `where(context)` binds the current value of the context:

```java
Context<UUID> requestId = Context.of(UUID::randomUUID);

Context.where(requestId).run(() -> {
    Context.run(() -> requestId.get());               // the same value
    executor.submit(Context.snapshot().wrap(task));   // the task sees the same value
});
```

### Missing values and `null`

`null` is a normal value: `where(context, null)` binds it, and `null` from a
supplier is cached like any other value. `optional()` returns the value as an
`Optional`. It never throws for `Context.required()` and for contexts mapped
from it; a custom context whose `supply()` throws still throws from
`optional()`.

| Case | Result |
| --- | --- |
| `Context.required()` is read and no block is open | `IllegalStateException("No active scope")` |
| `Context.required()` is read in a block and nothing is bound | `NoSuchElementException("No value in context")` |
| `Context.required().optional()` and nothing is bound | `Optional.empty()` |
| `context.map(mapper).optional()` and the source context is empty | `Optional.empty()` |
| A transactional context is read outside a transaction and nothing is bound | `IllegalStateException("No active transaction")` |

### Other threads

Values are not visible in other threads. Take a snapshot and wrap the task:

```java
executor.submit(Context.snapshot().wrap(task));
```

The snapshot holds only the values bound with `where`; cached values are
computed again in the other thread. A worker thread usually has no open block,
so the task sees exactly the snapshot. When the task runs in a thread that
already has an open block (for example in the caller's thread with
`CallerRunsPolicy`), it also sees the bound values of that block; the snapshot
values win when both have the same context.

In a Spring application
`ContextPropagationTaskDecorator` does this for `@Async` methods and task
executors, and it is registered automatically. It is the primary
`TaskDecorator`, and it applies your own `TaskDecorator` beans (in their order)
inside the propagated context. Turn it off with
`plusminus.context.propagation.enabled=false`.

> **Warning:** with `plusminus-http`, the snapshot also holds the current
> `HttpServletRequest` and `HttpServletResponse`. A task that runs after the
> request has ended must not use them: the servlet container reuses these
> objects for other requests. Copy what the task needs (a header, the user)
> into its own value before you start it.

### Custom contexts

`Context<T>` is a functional interface with one method to implement, `supply()`,
which returns the context's own value. Callers use `get()`, which first looks
for a value in the block, then calls `supply()` and caches the result:

```java
Context<Locale> locale = LocaleContextHolder::getLocale;

locale.get(); // the current locale
Context.where(locale, Locale.FRENCH).run(() -> locale.get()); // Locale.FRENCH
```

A value that must not be cached, because it can change inside the block, is
created with `Context.dynamic(supplier)`. That is the only way to make an
uncached context: a custom class or lambda is always cached.

A scope finds a value only by the same context instance, so create each context
once: as a `static final` field or a Spring bean.

### Transactional contexts

A transactional context lazily provides one value per active transaction:

```java
Context<UUID> transactionId = TransactionalContext.of(UUID::randomUUID);
UUID current = transactionId.get(); // throws IllegalStateException("No active transaction") outside a transaction
```

An inner transaction started with `REQUIRES_NEW` gets its own value, and the
outer value is back when it ends. A transactional context does not need a block
and is never cached in it. Like any context, a value bound with `where` comes
first, even outside a transaction; only reading its own value needs a
transaction.

### HTTP contexts

With `plusminus-http` each request runs inside a block, and these beans can be
injected anywhere:

| Bean | Value |
| --- | --- |
| `Context<HttpServletRequest>` | the current request |
| `Context<HttpServletResponse>` | the current response |
| `Context<Object>` | the handler of the request, once it is chosen |
| `Context<HandlerMethod>` | the handler, when it is a controller method |

```java
@Autowired
private Context<HttpServletRequest> request;

String agent = request.get().getHeader("User-Agent");
```

For an async request (a controller that returns `Callable` or
`DeferredResult`), the scope and invocation events cover the whole request:
`ScopeCompletedEvent`/`ScopeFailedEvent` and `InvocationCompletedEvent`/
`InvocationFailedEvent` are published when the async result is done, not when
the first dispatch returns. `AroundScope` beans wrap only the first dispatch.

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
