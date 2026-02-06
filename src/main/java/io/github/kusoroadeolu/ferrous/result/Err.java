package io.github.kusoroadeolu.ferrous.result;

import io.github.kusoroadeolu.ferrous.option.Option;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;
import java.util.function.Supplier;

record Err<T, E>(@NonNull E error) implements Result<T, E> {
    @Override
    public boolean isOk() {
        return false;
    }

    @Override
    public boolean isErr() {
        return true;
    }

    @Override
    public T unwrap() {
        throw new ResultException("'unWrap' called on type 'err'");
    }

    @Override
    public E unwrapErr() {
        return error;
    }

    @Override
    public T unwrapOr(T defaultValue) {
        return defaultValue;
    }

    @Override
    public T unwrapOrElse(Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public T expect(String message) {
        throw new ResultException(message);
    }

    @Override
    public E expectErr(String message) {
        return error;
    }

    @Override
    public <U> Result<U, E> map(Function<T, U> fn) {
        return new Err<>(error);
    }

    @Override
    public <F> Result<T, F> mapErr(Function<E, F> fn) {
        return new Err<>(fn.apply(error));
    }

    @Override
    public <U> Result<U, E> flatMap(Function<T, Result<U, E>> fn) {
        return new Err<>(error);
    }

    @Override
    public <U> Result<U, E> and(Result<U, E> other) {
        return other;
    }

    @Override
    public <U> Result<U, E> andThen(Function<T, Result<U, E>> fn) {
        return new Err<>(error);
    }

    @Override
    public Result<T, E> or(Result<T, E> other) {
        return other;
    }

    @Override
    public Result<T, E> orElse(Supplier<Result<T, E>> supplier) {
        return supplier.get();
    }

    @Override
    public @NonNull Option<T> ok() {
        return Option.none();
    }

    @Override
    public @NonNull Option<E> err() {
        return Option.some(error);
    }
}