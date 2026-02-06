package io.github.kusoroadeolu.ferrous.option;

import io.github.kusoroadeolu.ferrous.result.Result;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingFunction;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public sealed interface Option<T> permits None, Some {

    // Static factory methods
    static @NonNull <T> Option<T> some(@NonNull T value) {
        return new Some<>(value);
    }
    static @NonNull <T> Option<T> none() {
        return new None<>();
    }
    
    /**
     * Wraps a potentially null value. Returns Some if non-null, None if null.
     */
    static @NonNull <T> Option<T> ofNullable(@Nullable T value) {
        if (value == null) return new None<>();
        else return new Some<>(value);
    }

    static @NonNull <T> Option<T> of(@NonNull ThrowingSupplier<T> supplier) {
        try {
            return ofNullable(supplier.get());
        }catch (Exception e){
            return new None<>();
        }
    }

    static @NonNull <E, T> Option<T> of(@NonNull ThrowingFunction<E, T> action, @NonNull E value) {
        try {
            return ofNullable(action.apply(value));
        }catch (Exception e){
            return new None<>();
        }
    }
    // Query methods
    
    /** Returns true if Some, false if None */
    boolean isSome();
    
    /** Returns true if None, false if Some */
    boolean isNone();

    // Unwrap methods
    
    /**
     * Returns the wrapped value if Some, throws OptionException if None
     * @throws OptionException if called on None
     */
    @NonNull
    T unwrap();
    
    /**
     * @param fallback The fallback value
     * @return the wrapped value if Some, returns fallback if None
     */
    @NonNull
    T unwrapOr(@NonNull T fallback);
    
    /**
     * Returns the wrapped value if Some, calls supplier and returns its value if None
     * @param
     */
    @NonNull
    T unwrapOrElse(@NonNull Supplier<T> supplier);
    
    /**
     * Returns the wrapped value if Some, throws OptionException with custom message if None
     * @throws OptionException with the given message if called on None
     */
    @NonNull
    T expect(@NonNull String message);

    // Transform methods
    
    /**
     * If Some, applies fn to the wrapped value and returns Some with the result.
     * If None, returns None.
     */
    @NonNull
    <U> Option<U> map(@NonNull Function<T, U> fn);
    
    /**
     * If Some, applies fn to the wrapped value (fn returns an Option) and returns that Option.
     * If None, returns None.
     * Used for chaining operations that might not produce a value.
     */
    @NonNull
    <U> Option<U> flatMap(@NonNull Function<T, Option<U>> fn);
    
    /**
     * If Some and predicate returns true for the value, returns Some.
     * Otherwise, returns None.
     */
    @NonNull
    Option<T> filter(@NonNull Predicate<T> predicate);

    // Combinators
    
    /**
     * If Some, returns other. If None, returns None.
     * Like logical AND - both must have values.
     */
    @NonNull
    <U> Option<U> and(@NonNull Option<U> other);
    
    /**
     * If Some, applies fn to the wrapped value and returns the resulting Option.
     * If None, returns None.
     * Similar to flatMap.
     */
    @NonNull
    <U> Option<U> andThen(@NonNull Function<T, Option<U>> fn);
    
    /**
     * If Some, returns Some. If None, returns other.
     * Like logical OR - returns first value or second option.
     */
    @NonNull
    Option<T> or(@NonNull Option<T> other);
    
    /**
     * If Some, returns Some. If None, calls supplier and returns its Option.
     * Lazy version of or().
     */
    @NonNull
    Option<T> orElse(@NonNull Supplier<Option<T>> supplier);


    /**
     * Converts to null if None, or the wrapped value if Some.
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
    <U, R> Option<R> zipWith(@NonNull Option<U> other, @NonNull BiFunction<T, U, R> fn);

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
    <E> Result<T, E> okOrElse(@NonNull Supplier<E> supplier);

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