package io.github.kusoroadeolu.ferrous.result;

import io.github.kusoroadeolu.ferrous.option.Option;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingFunction;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingRunnable;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<T, E> permits Ok, Err {

    // Static factory methods
    static <T, E> Result<T, E> ok(@NonNull T value) {
        return new Ok<>(value);
    }
    static <T, E> Result<T, E> err(@NonNull E error) {
        return new Err<>(error);
    }


    /** Returns true if this is Ok, false if Err */
    boolean isOk();

    /** Returns true if this is Err, false if Ok */
    boolean isErr();

    // Unwrap methods

    /**
     * Returns the wrapped value if Ok, throws ResultException if Err
     * @throws ResultException if called on Err
     */
    @NonNull
    T unwrap();

    /**
     * Returns the wrapped error if Err, throws ResultException if Ok
     * @throws ResultException if called on Ok
     */
    @NonNull
    E unwrapErr();

    /**
     * Returns the wrapped value if Ok, returns defaultValue if Err
     */
    @NonNull
    T unwrapOr(@NonNull T defaultValue);

    /**
     * Returns the wrapped value if Ok, calls supplier and returns its value if Err
     */
    @NonNull
    T unwrapOrElse(@NonNull Supplier<T> supplier);

    /**
     * Returns the wrapped value if Ok, throws ResultException with custom message if Err
     * @throws ResultException with the given message if called on Err
     */
    @NonNull
    T expect(@NonNull String message);

    /**
     * Returns the wrapped error if Err, throws ResultException with custom message if Ok
     * @throws ResultException with the given message if called on Ok
     */
    @NonNull
    E expectErr(@NonNull String message);

    // Transform methods

    /**
     * If Ok, applies fn to the wrapped value and returns Ok with the result.
     * If Err, returns Err unchanged.
     */
    @NonNull
    <U> Result<U, E> map(@NonNull Function<T, U> fn);

    /**
     * If Err, applies fn to the wrapped error and returns Err with the result.
     * If Ok, returns Ok unchanged.
     */
    @NonNull
    <F> Result<T, F> mapErr(@NonNull Function<E, F> fn);

    /**
     * If Ok, applies fn to the wrapped value (fn returns a Result) and returns that Result.
     * If Err, returns Err unchanged.
     * Used for chaining operations that can fail.
     */
    @NonNull
    <U> Result<U, E> flatMap(@NonNull Function<T, Result<U, E>> fn);

    // Combinators

    /**
     * If Ok, returns other. If Err, returns this Err.
     * Like logical AND - both must succeed.
     */
    @NonNull
    <U> Result<U, E> and(Result<U, E> other);

    /**
     * If Ok, applies fn to the wrapped value and returns the resulting Result.
     * If Err, returns Err unchanged.
     * Similar to flatMap but more explicit about chaining.
     */
    @NonNull
    <U> Result<U, E> andThen(@NonNull Function<T, Result<U, E>> fn);

    /**
     * If Ok, returns this Ok. If Err, returns other.
     * Like logical OR - returns first success or last failure.
     */
    @NonNull
    Result<T, E> or(@NonNull Result<T, E> other);

    /**
     * If Ok, returns this Ok. If Err, calls supplier and returns its Result.
     * Lazy version of or().
     */
    @NonNull
    Result<T, E> orElse(@NonNull Supplier<Result<T, E>> supplier);

    // Pattern matching

    /**
     * Applies okFn if Ok, errFn if Err, and returns the result.
     * Forces you to handle both cases explicitly.
     */
    @NonNull
    <R> R match(@NonNull Function<T, R> okFn, Function<E, R> errFn);

    @NonNull Option<T> ok();
    @NonNull Option<E> err();
}
