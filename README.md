# Ferrous

A lightweight Java library bringing Rust-style `Option` and `Result` types for safer, more expressive code.

## Installation

### Maven
```xml
<dependency>
    <groupId>io.github.kusoroadeolu</groupId>
    <artifactId>ferrous</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'io.github.kusoroadeolu:ferrous:1.0.0'
```

## Why Ferrous?

Ferrous eliminates null pointer exceptions and makes error handling explicit in your type signatures. Instead of returning `null` or throwing exceptions, you express optionality and failure as part of your API.

```java
// Before: Unclear what can go wrong
User findUser(String id) throws DatabaseException {
    User user = db.query(id);
    if (user == null) throw new DatabaseException("Not found");
    return user;
}

// After: Types tell the whole story
Result<User, DatabaseError> findUser(String id) {
    return queryDatabase(id)
        .flatMap(this::validateUser)
        .flatMap(this::enrichWithProfile);
}
```

## Quick Start

### Option<T> - Handling values that might not exist

```java
import io.github.kusoroadeolu.ferrous.option.Option;

// Creating Options
Option<String> some = Option.some("hello");
Option<String> none = Option.none();
Option<String> maybe = Option.ofNullable(possiblyNull);

// Using Options
String result = Option.ofNullable(map.get("key"))
    .map(String::toUpperCase)
    .filter(s -> s.length() > 5)
    .unwrapOr("DEFAULT");

// Chaining with fallbacks
Option<Config> config = loadFromFile()
    .or(loadFromEnv())
    .or(loadDefaults());

// Pattern matching
option.ifSome(value -> System.out.println("Got: " + value));
option.ifNone(() -> System.out.println("Nothing here"));
```

### Result<T, E> - Handling operations that can fail

```java
import io.github.kusoroadeolu.ferrous.result.Result;

// Creating Results
Result<String, Error> success = Result.ok("value");
Result<String, Error> failure = Result.err(new Error("oops"));

// Wrapping code that throws
Result<Data, Exception> result = Result.catching(() -> {
    return riskyOperation();
});

// Chaining operations (short-circuits on first error)
Result<Response, ApiError> response = validateRequest(request)
    .andThen(req -> authenticate(req))
    .andThen(user -> fetchData(user))
    .map(data -> new Response(data));

// Handle both cases
response.ifOkOrElse(
    data -> System.out.println("Success: " + data),
    error -> System.err.println("Failed: " + error)
);
```

## Core Patterns
### Railway-Oriented Programming

Chain operations that might fail. The chain stops at the first error:

```java
Result<Order, OrderError> processOrder(OrderRequest req) {
    return validateOrder(req)              // Result<Order, OrderError>
        .andThen(this::checkInventory)     // Result<Order, OrderError>
        .andThen(this::processPayment)     // Result<Order, OrderError>
        .andThen(this::shipOrder)          // Result<Order, OrderError>
        .inspect(order -> logSuccess(order));
}
```

### Converting Between Option and Result

```java
// Option → Result
Option<User> userOpt = findUser(id);
Result<User, String> userResult = userOpt.okOr("User not found");

// Result → Option
Result<User, Error> userRes = loadUser(id);
Option<User> userOpt = userRes.ok();  // Discards the error
```

### Combining Results

```java
// Zip two Results together
Result<String, Error> first = Result.ok("Hello");
Result<String, Error> second = Result.ok("World");

Result<Pair<String, String>, Error> combined = first.zip(second);
// Ok(Pair("Hello", "World"))

// Or combine with a function
Result<String, Error> greeting = first.zipWith(second, 
    (a, b) -> a + " " + b
);
// Ok("Hello World")
```

## API Highlights

### Option Methods
- `map`, `flatMap`, `filter` - Transform and filter values
- `unwrap`, `unwrapOr`, `unwrapOrElse` - Extract values
- `and`, `andThen`, `or`, `orElse` - Combine Options
- `zip`, `zipWith` - Combine multiple Options
- `ifSome`, `ifNone` - Side effects
- `toNullable`, `toOptional` - Interop with Java

### Result Methods
- `map`, `mapErr`, `flatMap` - Transform values and errors
- `unwrap`, `unwrapOr`, `unwrapOrElse` - Extract values
- `and`, `andThen`, `or`, `orElse` - Combine Results
- `ok`, `err` - Convert to Options
- `catching` - Wrap throwing code
- `ifOk`, `ifErr`, `ifOkOrElse` - Side effects
- `inspect`, `inspectErr` - Debug without unwrapping

## When to Use What

### Use Option when:
- A value might not exist (cache lookup, map access, optional config)
- You want to avoid null checks
- Absence is not an error, just a valid state

### Use Result when:
- An operation can fail with meaningful error information
- You want errors in your type signature
- You need to chain operations that can each fail differently
- You want to replace try-catch with functional composition

## Examples

### Configuration Loading with Fallbacks
```java
Option<Config> getConfig() {
    return Option.of(() -> loadFromFile("config.json"))
        .or(Option.of(() -> loadFromFile("config.yaml")))
        .orElse(() -> Option.some(defaultConfig()));
}
```

### API Call with Error Handling
```java
Result<User, ApiError> fetchUser(String id) {
    return Result.catching(
        () -> httpClient.get("/users/" + id),
        ex -> new ApiError("Network failure: " + ex.getMessage())
    )
    .flatMap(response -> parseJson(response.body()))
    .flatMap(this::validateUser);
}
```

### Validation Pipeline
```java
Result<User, ValidationError> createUser(UserInput input) {
    return validateEmail(input.email())
        .andThen(email -> validatePassword(input.password()))
        .andThen(pass -> validateAge(input.age()))
        .map(age -> new User(input.email(), input.password(), age));
}
```

## Comparison with Java Optional

| Feature | Java Optional | Ferrous Option |
|---------|--------------|----------------|
| Combinators | Limited | Full set (and, or, xor, zip, etc.) |
| Error handling | Convert to exceptions | Convert to Result |
| Interop | Built-in | `toOptional()`, `toNullable()` |
| Naming | `orElseGet` | `unwrapOrElse` (consistent with Result) |
| Philosophy | Discourage null | Rust-style functional composition |

Ferrous is more feature-complete and designed to work seamlessly with `Result` for comprehensive error handling.

## Requirements

- Java 21+
- Uses JSpecify annotations for nullability

## License

MIT

## Links

- [GitHub](https://github.com/kusoroadeolu/ferrous)
- [Latest Javadoc](https://kusoroadeolu.github.io/ferrous/)

---
