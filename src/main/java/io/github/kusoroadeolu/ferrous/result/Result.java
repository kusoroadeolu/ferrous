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

public sealed interface Result<T, E> permits Ok, Err {

    // Static factory methods
    static <T, E> Result<T, E> ok(@NonNull T value) {
        return new Ok<>(value);
    }
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
            @NonNull ThrowingFunction<T, R> fn,
            @NonNull T input,
            @NonNull Function<Exception, E> errorMapper
    ){
        try {
            return new Ok<>(fn.apply(input));
        }catch (Exception e){
            return new Err<>(errorMapper.apply(e));
        }
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


    @NonNull Option<T> ok();
    @NonNull Option<E> err();

    // Additional essential methods for Result<T, E> interface

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
     * Flattens a nested Result where both have the same error type.
     * For Ok(Ok(value)): returns Ok(value)
     * For Ok(Err(error)): returns Err(error)
     * For Err(error): returns Err(error)
     *
     * @param <U> the type of the inner Result's Ok value
     * @return the flattened Result
     */
    @NonNull
    <U> Result<U, E> flatten();

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
    <U, R> Result<R, E> zipWith(@NonNull Result<U, E> other, @NonNull BiFunction<T, U, R> fn);

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
