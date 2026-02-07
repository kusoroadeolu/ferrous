package io.github.kusoroadeolu.ferrous.result;

import io.github.kusoroadeolu.ferrous.option.Option;
import io.github.kusoroadeolu.ferrous.option.Pair;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingFunction;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingSupplier;
import org.jspecify.annotations.NonNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A type representing either success ({@link Ok}) or failure ({@link Err}).
 *
 * <p>{@code Result} is used for operations that can fail in a predictable way, providing
 * type-safe error handling without exceptions. It forces explicit handling of both success
 * and error cases, making error paths visible in the type system.
 *
 * <p>A {@code Result} can be queried with {@link #isOk()} and {@link #isErr()}, and values
 * can be extracted with {@link #unwrap()}, {@link #unwrapOr(Object)}, or pattern matching.
 * Operations can be chained using {@link #map(Function)}, {@link #flatMap(Function)}, and
 * other combinators.
 *
 * <p>Example:
 * <pre>{@code
 * Result<User, DatabaseError> findUser(String id) {
 *     return validateUserId(id)
 *         .flatMap(validId -> queryDatabase(validId))
 *         .flatMap(row -> parseUser(row));
 * }
 *
 * // Handle the result
 * String username = findUser("123")
 *     .map(User::getName)
 *     .unwrapOr("Unknown");
 * }</pre>
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error value
 * @see Ok
 * @see Err
 * @see Option
 */
public sealed interface Result<T, E> permits Ok, Err {

    /**
     * Creates a {@code Result} representing a successful operation.
     *
     * <p>Example:
     * <pre>{@code
     * Result<String, Error> success = Result.ok("hello");
     * }</pre>
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error value
     * @param value the success value
     * @throws NullPointerException if value is null
     * @return an {@code Ok} containing the value
     */
    static <T, E> Result<T, E> ok(@NonNull T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a {@code Result} representing a failed operation.
     *
     * @param <T> the type of the success value
     * @param <E> the type of the error value
     * @param error the error value
     * @return an {@code Err} containing the error
     * @throws NullPointerException if error is null
     */
    static <T, E> Result<T, E> err(@NonNull E error) {
        return new Err<>(error);
    }

    /**
     * Wraps a throwing supplier in a Result, catching any exceptions.
     * If supplier succeeds: returns Ok(value)
     * If supplier throws: returns Err(exception)
     *
     * @param <T> the success type
     * @param supplier the throwing supplier to execute
     * @return Ok if successful, Err containing the exception if it throws
     */
    @NonNull
    static <T> Result<T, Exception> catching(@NonNull ThrowingSupplier<T> supplier){
        try {
            return new Ok<>(supplier.get());
        }catch (Exception e){
            return new Err<>(e);
        }
    }

    /**
     * Wraps a throwing supplier in a Result, mapping exceptions with the provided function.
     * If supplier succeeds: returns Ok(value)
     * If supplier throws: returns Err(errorMapper(exception))
     *
     * @param <T> the success type
     * @param <E> the error type
     * @param supplier the throwing supplier to execute
     * @param errorMapper function to map exceptions to error type
     * @return Ok if successful, Err with mapped exception if it throws
     */
    @NonNull
    static <T, E> Result<T, E> catching(
            @NonNull ThrowingSupplier<T> supplier,
            @NonNull Function<Exception, E> errorMapper
    ){
        try {
            return new Ok<>(supplier.get());
        }catch (Exception e){
            return new Err<>(errorMapper.apply(e));
        }
    }

    /**
     * Wraps a throwing function in a Result, catching any exceptions.
     * If function succeeds: returns Ok(value)
     * If function throws: returns Err(exception)
     *
     * @param <T> the input type
     * @param <R> the success type
     * @param fn the throwing function to execute
     * @param input the input value
     * @return Ok if successful, Err containing the exception if it throws
     */
    @NonNull
    static <T, R> Result<R, Exception> catching(
            @NonNull ThrowingFunction<T, R> fn,
            @NonNull T input
    ){
        try {
            return new Ok<>(fn.apply(input));
        }catch (Exception e){
            return new Err<>(e);
        }
    }

    /**
     * Wraps a throwing function in a Result, mapping exceptions with the provided function.
     * If function succeeds: returns Ok(value)
     * If function throws: returns Err(errorMapper(exception))
     *
     * @param <T> the input type
     * @param <R> the success type
     * @param <E> the error type
     * @param fn the throwing function to execute
     * @param input the input value
     * @param errorMapper function to map exceptions to error type
     * @return Ok if successful, Err with mapped exception if it throws
     */
    @NonNull
    static <T, R, E> Result<R, E> catching(
            @NonNull ThrowingFunction<T, @NonNull R> fn,
            @NonNull T input,
            @NonNull Function<Exception, @NonNull E> errorMapper
    ){
        try {
            return new Ok<>(fn.apply(input));
        }catch (Exception e){
            return new Err<>(errorMapper.apply(e));
        }
    }


    /**
     * Returns {@code true} if this {@code Result} represents a successful operation.
     *
     * @return {@code true} for {@code Ok}, {@code false} for {@code Err}
     * @see #isErr()
     */
    boolean isOk();

    /**
     * Returns {@code true} if this {@code Result} represents a failed operation.
     *
     * @return {@code true} for {@code Err}, {@code false} for {@code Ok}
     * @see #isOk()
     */
    boolean isErr();


    /**
     * Returns the success value, throwing an exception if this is an error.
     *
     *
     * @return the wrapped value
     * @throws ResultException if this is {@code Err}
     * @see #expect(String) for throwing with a custom error message
     * @see #unwrapOr(Object) for providing a default value
     */
    @NonNull
    T unwrap();

    /**
     * Returns the error value, throwing an exception if this is a success.
     *
     * <p>This is the inverse of {@link #unwrap()}.
     *
     * @return the wrapped error
     * @throws ResultException if this is {@code Ok}
     * @see #expectErr(String) for throwing with a custom error message
     */
    @NonNull
    E unwrapErr();

    /**
     * Returns the success value if present, otherwise returns the provided default value.
     *
     * @param defaultValue the value to return if this is {@code Err}
     * @return the wrapped value if {@code Ok}, otherwise the default value
     */
    @NonNull
    T unwrapOr(@NonNull T defaultValue);

    /**
     * Returns the success value if present, otherwise computes a fallback value.
     *
     * @param supplier the supplier to call if this is {@code Err}
     * @return the wrapped value if {@code Ok}, otherwise the result of calling the supplier
     */
    @NonNull
    T unwrapOrElse(@NonNull Supplier<@NonNull T> supplier);

    /**
     * Returns the success value if present, otherwise throws an exception with the given message.
     *
     * <p>Similar to {@link #unwrap()}, but allows you to provide a custom error message
     * for better debugging and error reporting. The error value is discarded.

     * @param message the error message to use if this is {@code Err}
     * @return the wrapped value
     * @throws ResultException with the provided message if this is {@code Err}
     * @see #unwrap() for a version without custom message
     */
    @NonNull
    T expect(@NonNull String message);

    /**
     * Returns the error value if present, otherwise throws an exception with the given message.
     *
     * <p>This is the inverse of {@link #expect(String)}. Use this when you're certain
     * the operation failed and need to extract the error with a custom message.
     *
     * @param message the error message to use if this is {@code Ok}
     * @return the wrapped error
     * @throws ResultException with the provided message if this is {@code Ok}
     * @see #unwrapErr() for a version without custom message
     */
    @NonNull
    E expectErr(@NonNull String message);

    /**
     * Transforms the success value by applying a function, if present.
     *
     * <p>This is the primary way to transform values inside a {@code Result} without
     * unwrapping and re-wrapping. If this is {@code Err}, the function is not called
     * and the error is propagated unchanged.
     *
     * @param <U> the type of the transformed value
     * @param fn the function to apply to the success value
     * @return {@code Ok} with the transformed value if this is {@code Ok},
     *         otherwise {@code Err} unchanged
     * @see #flatMap(Function) for transformations that return Results
     */
    @NonNull
    <U> Result<U, E> map(@NonNull Function<T, @NonNull U> fn);

    /**
     * Transforms the error value by applying a function, if present.

     *
     * @param <F> the type of the new error
     * @param fn the function to apply to the error value
     * @return {@code Ok} unchanged if this is {@code Ok},
     *         otherwise {@code Err} with the transformed error
     * @see #map(Function) for transforming success values
     */
    @NonNull
    <F> Result<T, F> mapErr(@NonNull Function<E, @NonNull F> fn);

    /**
     * Transforms the success value with a function that returns a {@code Result}.

     *
     * @param <U> the type of value in the returned Result
     * @param fn the function that returns a {@code Result}
     * @return the result of applying the function if this is {@code Ok},
     *         otherwise {@code Err} unchanged
     * @see #map(Function) for transformations that can't fail
     * @see #andThen(Function) for an alias with emphasis on sequencing
     */
    @NonNull
    <U> Result<U, E> flatMap(@NonNull Function<T, @NonNull Result<U, E>> fn);

    /**
     * Returns the provided {@code Result} if this is {@code Ok}, otherwise returns this {@code Err}.
     *
     * <p>Implements logical AND behavior, both Results must succeed for the final result
     * to succeed.
     *
     * @param <U> the type of the other Result's success value
     * @param other the Result to return if this is {@code Ok}
     * @return {@code other} if this is {@code Ok}, otherwise this {@code Err}
     * @see #andThen(Function) for a lazy, function-based version
     */
    @NonNull
    <U> Result<U, E> and(Result<U, E> other);

    /**
     * Applies a function that returns a {@code Result} if this is {@code Ok},
     * otherwise returns this {@code Err}.
     *
     * <p>This is essentially an alias for {@link #flatMap(Function)}, but emphasizes
     * the sequencing/chaining aspect rather than the flattening behavior. The name
     * comes from Rust's Result API.
     *
     * @param <U> the type of value in the returned Result
     * @param fn the function to apply if this is {@code Ok}
     * @return the result of applying the function if this is {@code Ok},
     *         otherwise this {@code Err}
     * @see #flatMap(Function) for the same behavior with different emphasis
     */
    @NonNull
    <U> Result<U, E> andThen(@NonNull Function<T, @NonNull Result<U, E>> fn);

    /**
     * Returns this {@code Result} if it's {@code Ok}, otherwise returns the provided alternative.
     *
     * <p>Implements logical OR behavior - returns the first successful Result. Use this
     * for fallback strategies where you want to try alternative approaches if one fails.
     *
     * @param other the alternative Result to use if this is {@code Err}
     * @return this {@code Result} if {@code Ok}, otherwise {@code other}
     * @see #orElse(Supplier) for a lazy version
     */
    @NonNull
    Result<T, E> or(@NonNull Result<T, E> other);

    /**
     * Returns this {@code Result} if it's {@code Ok}, otherwise computes an alternative.
     *
     * <p>Lazy version of {@link #or(Result)}. The supplier is only called if this Result
     * is {@code Err}. Use this when the fallback is expensive to compute or has side effects.
     *
     * @param supplier the supplier to call if this is {@code Err}
     * @return this {@code Result} if {@code Ok}, otherwise the result of calling the supplier
     */
    @NonNull
    Result<T, E> orElse(@NonNull Supplier<@NonNull Result<T, E>> supplier);

// ============================================================================
// Conversion methods
// ============================================================================

    /**
     * Converts this {@code Result} to an {@code Option} of the success value.
     *
     * <p>Discards the error information and returns {@code Some} if successful,
     * {@code None} if failed. Use this when you only care about whether the operation
     * succeeded, not why it failed.
     *
     * @return {@code Some} containing the success value if {@code Ok}, otherwise {@code None}
     * @see #err() for converting to an Option of the error
     */
    @NonNull
    Option<T> ok();

    /**
     * Converts this {@code Result} to an {@code Option} of the error value.
     *
     * <p>Discards the success information and returns {@code Some} if failed,
     * {@code None} if successful. Use this when you want to collect or process
     * errors while ignoring successes.
     *
     * @return {@code Some} containing the error value if {@code Err}, otherwise {@code None}
     * @see #ok() for converting to an Option of the success value
     */
    @NonNull
    Option<E> err();


    /**
     * Returns true if this is Ok and contains the given value.
     * For Ok: returns true if the wrapped value equals the given value
     * For Err: returns false
     *
     * @param value the value to check for
     * @return true if Ok and contains the value, false otherwise
     */
    boolean contains(@NonNull T value);

    /**
     * Returns true if this is Err and contains the given error.
     * For Ok: returns false
     * For Err: returns true if the wrapped error equals the given error
     *
     * @param error the error to check for
     * @return true if Err and contains the error, false otherwise
     */
    boolean containsErr(@NonNull E error);

    /**
     * Calls the provided consumer with the wrapped value if Ok.
     * Returns this Result for chaining.
     * For Ok: calls consumer with the wrapped value, returns this
     * For Err: does nothing, returns this
     *
     * @param consumer the consumer to call if Ok
     * @return this Result for method chaining
     */
    @NonNull
    Result<T, E> inspect(@NonNull Consumer<T> consumer);

    /**
     * Calls the provided consumer with the wrapped error if Err.
     * Returns this Result for chaining.
     * For Ok: does nothing, returns this
     * For Err: calls consumer with the wrapped error, returns this
     *
     * @param consumer the consumer to call if Err
     * @return this Result for method chaining
     */
    @NonNull
    Result<T, E> inspectErr(@NonNull Consumer<E> consumer);

    /**
     * Combines this Result with another, returning Ok only if both are Ok.
     * For Ok(a) and Ok(b): returns Ok(Pair(a, b))
     * For any Err: returns the first Err encountered
     *
     * @param <U> the type of the other Result's Ok value
     * @param other the Result to zip with
     * @return Ok containing both values if both are Ok, otherwise the first Err
     */
    @NonNull
    <U> Result<Pair<T, U>, E> zip(@NonNull Result<U, E> other);

    /**
     * Combines this Result with another using a combining function.
     * For Ok(a) and Ok(b): returns Ok(fn(a, b))
     * For any Err: returns the first Err encountered
     *
     * @param <U> the type of the other Result's Ok value
     * @param <R> the result type
     * @param other the Result to zip with
     * @param fn the function to combine the values
     * @return Ok with combined value if both are Ok, otherwise the first Err
     */
    @NonNull
    <U, R> Result<R, E> zipWith(@NonNull Result<U, E> other, @NonNull BiFunction<T, @NonNull U, @NonNull R> fn);

    /**
     * Transposes a Result of an Option into an Option of a Result.
     * For Ok(Some(value)): returns Some(Ok(value))
     * For Ok(None): returns None
     * For Err(error): returns Some(Err(error))
     *
     * @param <U> the type of the inner Option's value
     * @return the transposed Option
     */
    @NonNull
    <U> Option<Result<U, E>> transpose();


    /**
     * Calls the provided consumer if this is Ok.
     * For Ok: calls consumer with the wrapped value
     * For Err: does nothing
     *
     * @param consumer the consumer to call if Ok
     */
    void ifOk(@NonNull Consumer<T> consumer);

    /**
     * Calls the provided consumer if this is Err.
     * For Ok: does nothing
     * For Err: calls consumer with the wrapped error
     *
     * @param consumer the consumer to call if Err
     */
    void ifErr(@NonNull Consumer<E> consumer);

    /**
     * Calls onOk if this is Ok, or onErr if this is Err.
     * For Ok: calls onOk with the wrapped value
     * For Err: calls onErr with the wrapped error
     *
     * @param onOk the consumer to call if Ok
     * @param onErr the consumer to call if Err
     */
    void ifOkOrElse(@NonNull Consumer<T> onOk, @NonNull Consumer<E> onErr);

    /**
     * Converts this Result to a Stream.
     * For Ok: returns a Stream containing the single value
     * For Err: returns an empty Stream
     *
     * @return a Stream with 0 or 1 elements
     */
    @NonNull
    Stream<T> stream();


}
