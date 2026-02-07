package io.github.kusoroadeolu.ferrous.option;

import io.github.kusoroadeolu.ferrous.result.Result;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingFunction;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A type representing an optional value: either {@link Some} or {@link None}.
 *
 * <p>{@code Option} is used for values that may or may not be present, providing
 * a type-safe alternative to null references. It forces explicit handling of the
 * absence case, eliminating null pointer exceptions.
 *
 * <p>An {@code Option} can be queried with {@link #isSome()} and {@link #isNone()}, and values
 * can be extracted with {@link #unwrap()}, {@link #unwrapOr(Object)}, or pattern matching.
 * Operations can be chained using {@link #map(Function)}, {@link #flatMap(Function)}, and
 * other combinators.
 *
 * <p>Example:
 * <pre>{@code
 * Option<User> findUser(String id) {
 *     return Option.ofNullable(database.get(id));
 * }
 *
 * // Handle the option
 * String name = findUser("123")
 *     .map(User::getName)
 *     .map(String::toUpperCase)
 *     .unwrapOr("UNKNOWN");
 * }</pre>
 *
 * @param <T> the type of the value
 * @see Some
 * @see None
 * @see Result
 */
public sealed interface Option<T> permits None, Some {

    /**
     * Creates an {@code Option} containing the given non-null value.
     *
     * <p>This is the primary way to wrap a known non-null value in an Option.
     *
     * @param <T> the type of the value
     * @param value the non-null value to wrap
     * @return a {@code Some} containing the value
     * @throws NullPointerException if value is null
     */
    static @NonNull <T> Option<T> some(@NonNull T value) {
        return new Some<>(value);
    }

    /**
     * Creates an empty {@code Option} with no value.
     *
     * <p>Use this when you know there's no value to return, similar to returning
     * {@code null} but in a type-safe way.
     *
     *
     * @param <T> the type parameter (can be inferred from context)
     * @return an empty {@code Option}
     */
    static @NonNull <T> Option<T> none() {
        return new None<>();
    }

    /**
     * Wraps a potentially null value in an {@code Option}.
     *
     * <p>This is the most common way to convert nullable values into the Option type.
     * Returns {@code Some} if the value is non-null, {@code None} if it's null.
     *
     * @param <T> the type of the value
     * @param value the potentially null value to wrap
     * @return {@code Some(value)} if value is non-null, {@code None} otherwise
     */
    static @NonNull <T> Option<T> ofNullable(@Nullable T value) {
        if (value == null) return new None<>();
        else return new Some<>(value);
    }

    /**
     * Attempts to get a value from a throwing supplier, catching any exceptions.
     * Any exception thrown by the supplier is caught and converted to {@code None}.
     * If the supplier returns null, the result is also {@code None}.
     *
     * @param <T> the type of value to get
     * @param supplier the supplier that might throw an exception
     * @return {@code Some(value)} if successful and non-null, {@code None} if exception thrown or null returned
     */
    static @NonNull <T> Option<T> of(@NonNull ThrowingSupplier<T> supplier) {
        try {
            return ofNullable(supplier.get());
        } catch (Exception e) {
            return new None<>();
        }
    }

    /**
     * Attempts to apply a throwing function to a value, catching any exceptions.
     *
     * <p>Similar to {@link #of(ThrowingSupplier)}, but takes an input value and a function.
     * Useful for transforming values with operations that might fail.
     *
     * @param <E> the input type
     * @param <T> the result type
     * @param action the function that might throw an exception
     * @param value the input value to transform
     * @return {@code Some(result)} if successful and non-null, {@code None} if exception thrown or null returned
     */
    static @NonNull <E, T> Option<T> of(@NonNull ThrowingFunction<E, T> action, @NonNull E value) {
        try {
            return ofNullable(action.apply(value));
        } catch (Exception e) {
            return new None<>();
        }
    }

    /**
     * Returns {@code true} if this {@code Option} contains a value.
     *
     * @return {@code true} for {@code Some}, {@code false} for {@code None}
     */
    boolean isSome();

    /**
     * Returns {@code true} if this {@code Option} is empty (contains no value).

     * @return {@code true} for {@code None}, {@code false} for {@code Some}
     */
    boolean isNone();

    /**
     * Returns the contained value, throwing an exception if empty.
     *
     * @return the wrapped value
     * @throws OptionException if this is {@code None}
     * @see #expect(String) for throwing with a custom error message
     * @see #unwrapOr(Object) for providing a default value
     */
    @NonNull
    T unwrap();

    /**
     * Returns the contained value if present, otherwise returns the provided fallback.
     *
     * @param fallback the value to return if this is {@code None}
     * @return the wrapped value if {@code Some}, otherwise the fallback value
     */
    @NonNull
    T unwrapOr(@NonNull T fallback);

    /**
     * Returns the contained value if present, otherwise computes a fallback value.
     *
     * <p>Use this when the fallback value is expensive to compute or has side effects.
     * The supplier is only called if this {@code Option} is {@code None}.
     *
     * @param supplier the supplier to call if this is {@code None}
     * @return the wrapped value if {@code Some}, otherwise the result of calling the supplier
     */
    @NonNull
    T unwrapOrElse(@NonNull Supplier<@NonNull T> supplier);

    /**
     * Returns the contained value if present, otherwise throws an exception with the given message.
     *
     * <p>Similar to {@link #unwrap()}, but allows you to provide a custom error message
     * for better debugging and error reporting.
     *
     * @param message the error message to use if this is {@code None}
     * @return the wrapped value
     * @throws OptionException with the provided message if this is {@code None}
     */
    @NonNull
    T expect(@NonNull String message);

    /**
     * Transforms the contained value by applying a function, if present.
     *
     * <p>This is the primary way to transform values inside an {@code Option} without
     * unwrapping and re-wrapping. If this is {@code None}, the function is not called
     * and {@code None} is returned.
     *
     * @param <U> the type of the result
     * @param fn the function to apply to the value
     * @return {@code Some} with the transformed value if this is {@code Some},
     *         otherwise {@code None}
     */
    @NonNull
    <U> Option<U> map(@NonNull Function<T, @NonNull U> fn);

    /**
     * Transforms the contained value with a function that returns an {@code Option}.
     *
     * <p>Use this when the transformation itself might not produce a value. This prevents
     * nested {@code Option<Option<T>>} types. Also known as "bind" or "chain" in other languages.
     *
     * @param <U> the type of value in the returned Option
     * @param fn the function that returns an {@code Option}
     * @return the result of applying the function if this is {@code Some},
     *         otherwise {@code None}
     */
    @NonNull
    <U> Option<U> flatMap(@NonNull Function<T, @NonNull Option<U>> fn);

    /**
     * Returns this {@code Option} if it contains a value that matches the predicate,
     * otherwise returns {@code None}.
     *
     * <p>Useful for conditional checking without unwrapping.
     *
     * @param predicate the predicate to test the value against
     * @return this {@code Option} if {@code Some} and the predicate returns {@code true},
     *         otherwise {@code None}
     */
    @NonNull
    Option<T> filter(@NonNull Predicate<T> predicate);

// ============================================================================
// Combinators
// ============================================================================

    /**
     * Returns the provided {@code Option} if this is {@code Some}, otherwise returns {@code None}.
     *
     * <p>Implements logical AND behavior - both Options must have values for the result
     * to have a value. The value from this Option is discarded.
     *
     * <pre>{@code
     * Option<String> first = Option.some("hello");
     * Option<Integer> second = Option.some(42);
     * Option<Integer> result = first.and(second); // Some(42)
     *
     * Option<String> none = Option.none();
     * Option<Integer> result2 = none.and(second); // None
     *
     * // Useful for sequencing checks
     * return hasPermission()
     *     .and(hasValidToken())
     *     .and(loadUserData());
     * }</pre>
     *
     * @param <U> the type of the other Option's value
     * @param other the Option to return if this is {@code Some}
     * @return {@code other} if this is {@code Some}, otherwise {@code None}
     * @see #andThen(Function) for a lazy version
     */
    @NonNull
    <U> Option<U> and(@NonNull Option<U> other);

    /**
     * Applies a function that returns an {@code Option} if this is {@code Some},
     * otherwise returns {@code None}.
     *
     * <p>This is essentially an alias for {@link #flatMap(Function)}, but emphasizes
     * the sequencing/chaining aspect. The name comes from Rust's Option API.
     *
     * <pre>{@code
     * Option<User> user = authenticate(credentials)
     *     .andThen(token -> validateToken(token))
     *     .andThen(userId -> loadUser(userId));
     * }</pre>
     *
     * @param <U> the type of value in the returned Option
     * @param fn the function to apply if this is {@code Some}
     * @return the result of applying the function if this is {@code Some},
     *         otherwise {@code None}
     */
    @NonNull
    <U> Option<U> andThen(@NonNull Function<T, Option<U>> fn);

    /**
     * Returns this {@code Option} if it contains a value, otherwise returns the provided alternative.
     *
     * <p>Implements logical OR behavior - returns the first Option that has a value.
     *
     * <pre>{@code
     * // Try cache, fall back to database
     * Option<User> user = cache.get(id).or(database.get(id));
     *
     * // Multiple fallbacks
     * Option<Config> config = loadFromFile("config.json")
     *     .or(loadFromFile("config.yml"))
     *     .or(loadDefaults());
     *
     * Option<String> some = Option.some("first");
     * Option<String> backup = Option.some("second");
     * some.or(backup); // Some("first") - backup not used
     *
     * Option<String> none = Option.none();
     * none.or(backup); // Some("second") - backup used
     * }</pre>
     *
     * @param other the alternative Option to use if this is {@code None}
     * @return this {@code Option} if {@code Some}, otherwise {@code other}
     * @see #orElse(Supplier) for a lazy version
     */
    @NonNull
    Option<T> or(@NonNull Option<T> other);

    /**
     * Returns this {@code Option} if it contains a value, otherwise computes an alternative.
     *
     * <p>Lazy version of {@link #or(Option)}. The supplier is only called if this Option is {@code None}.
     * Use this when the fallback is expensive to compute or has side effects.
     *
     * <pre>{@code
     * // Lazy computation - database only queried if cache misses
     * Option<User> user = cache.get(id).orElse(() ->
     *     database.query(id)
     * );
     *
     * // Chained lazy fallbacks
     * Option<Config> config = loadPrimaryConfig()
     *     .orElse(() -> loadBackupConfig())
     *     .orElse(() -> generateDefaultConfig());
     * }</pre>
     *
     * @param supplier the supplier to call if this is {@code None}
     * @return this {@code Option} if {@code Some}, otherwise the result of calling the supplier
     */
    @NonNull
    Option<T> orElse(@NonNull Supplier<Option<T>> supplier);

    /**
     * Converts this {@code Option} to a nullable value.
     *
     * <p>Use this when interfacing with APIs that expect nullable values.
     * This is the inverse of {@link #ofNullable(Object)}.
     *
     * <pre>{@code
     * Option<String> some = Option.some("hello");
     * String value = some.toNullable(); // "hello"
     *
     * Option<String> none = Option.none();
     * String nullValue = none.toNullable(); // null
     *
     * // Using with legacy API
     * map.put("key", option.toNullable());
     * }</pre>
     *
     * @return the contained value if {@code Some}, otherwise {@code null}
     */
    @Nullable
    T toNullable();

    /**
     * Returns true if this is Some and contains the given value.
     * For Some: returns true if the wrapped value equals the given value
     * For None: returns false
     *
     * @param value the value to check for
     * @return true if Some and contains the value, false otherwise
     */
    boolean contains(@NonNull T value);

    /**
     * Calls the provided consumer with the wrapped value if Some.
     * Returns this Option for chaining.
     * For Some: calls consumer with the wrapped value, returns this
     * For None: does nothing, returns this
     *
     * @param consumer the consumer to call if Some
     * @return this Option for method chaining
     */
    @NonNull
    Option<T> inspect(@NonNull Consumer<T> consumer);


    /**
     * Combines this Option with another, returning Some only if both are Some.
     * For Some(a) and Some(b): returns Some(Pair(a, b))
     * For any other combination: returns None
     *
     * @param <U> the type of the other Option's value
     * @param other the Option to zip with
     * @return Some containing both values if both are Some, None otherwise
     */
    @NonNull
    <U> Option<Pair<T, U>> zip(@NonNull Option<U> other);

    /**
     * Combines this Option with another using a combining function.
     * For Some(a) and Some(b): returns Some(fn(a, b))
     * For any other combination: returns None
     *
     * @param <U> the type of the other Option's value
     * @param <R> the result type
     * @param other the Option to zip with
     * @param fn the function to combine the values
     * @return Some with combined value if both are Some, None otherwise
     */
    @NonNull
    <U, R> Option<R> zipWith(@NonNull Option<U> other, @NonNull BiFunction<T, @NonNull U, @NonNull R> fn);

    /**
     * Converts this Option to a Result.
     * For Some: returns Ok with the wrapped value
     * For None: returns Err with the provided error
     *
     * @param <E> the error type
     * @param error the error to use if None
     * @return Ok if Some, Err with the provided error if None
     */
    @NonNull
    <E> Result<T, E> okOr(@NonNull E error);

    /**
     * Converts this Option to a Result, lazily computing the error.
     * For Some: returns Ok with the wrapped value
     * For None: calls supplier and returns Err with its value
     *
     * @param <E> the error type
     * @param supplier the supplier to call for the error if None
     * @return Ok if Some, Err with the supplied error if None
     */
    @NonNull
    <E> Result<T, E> okOrElse(@NonNull Supplier<@NonNull E> supplier);

    /**
     * Transposes an Option of a Result into a Result of an Option.
     * For Some(Ok(value)): returns Ok(Some(value))
     * For Some(Err(error)): returns Err(error)
     * For None: returns Ok(None)
     *
     * @param <U> the Ok type of the inner Result
     * @param <E> the Err type of the inner Result
     * @return the transposed Result
     */
    @NonNull
    <U, E> Result<Option<U>, E> transpose();

    /**
     * Returns this Option if it is Some, otherwise returns other if other is Some.
     * Returns None only if both are None.
     * For Some: returns this
     * For None and other is Some: returns other
     * For None and other is None: returns None
     *
     * @param other the alternative Option
     * @return this if Some, other if this is None and other is Some, None if both are None
     */
    @NonNull
    Option<T> xor(@NonNull Option<T> other);


    /**
     * Calls the provided consumer if this is Some.
     * For Some: calls consumer with the wrapped value
     * For None: does nothing
     *
     * @param consumer the consumer to call if Some
     */
    void ifSome(@NonNull Consumer<T> consumer);

    /**
     * Calls the provided runnable if this is None.
     * For Some: does nothing
     * For None: calls the runnable
     *
     * @param runnable the runnable to call if None
     */
    void ifNone(@NonNull Runnable runnable);

    /**
     * Converts this Option to a Stream.
     * For Some: returns a Stream containing the single value
     * For None: returns an empty Stream
     *
     * @return a Stream with 0 or 1 elements
     */
    @NonNull
    Stream<T> stream();

    /**
     * Converts this Option to an Optional.
     * For Some: returns Optional.of(value)
     * For None: returns Optional.empty()
     *
     * @return the equivalent Optional
     */
    @NonNull
    Optional<T> toOptional();

}