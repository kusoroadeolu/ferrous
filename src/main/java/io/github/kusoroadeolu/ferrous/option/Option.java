package io.github.kusoroadeolu.ferrous.option;

import io.github.kusoroadeolu.ferrous.throwing.ThrowingFunction;
import io.github.kusoroadeolu.ferrous.throwing.ThrowingSupplier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
     * Returns the wrapped value if Some, returns defaultValue if None
     */
    @NonNull
    T unwrapOr(@NonNull T defaultValue);
    
    /**
     * Returns the wrapped value if Some, calls supplier and returns its value if None
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


    
    // Conversion
    
    /**
     * Converts to null if None, or the wrapped value if Some.
     */
    @Nullable
    T toNullable();

}