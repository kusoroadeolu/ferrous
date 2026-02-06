package io.github.kusoroadeolu.ferrous.result;

import io.github.kusoroadeolu.ferrous.option.Option;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;
import java.util.function.Supplier;

record Ok<T, E>(@NonNull T value) implements Result<T, E> {
    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public boolean isErr() {
        return false;
    }

    @Override
    public T unwrap() {
        return value;
    }

    @Override
    public E unwrapErr() {
        throw new ResultException("'unwrapErr' called on type 'ok'");
    }

    @Override
    public T unwrapOr(T defaultValue) {
        return value;
    }

    @Override
    public T unwrapOrElse(Supplier<T> supplier) {
        return value;
    }

    @Override
    public T expect(String message) {
        return value;
    }

    @Override
    public E expectErr(String message) {
        throw new ResultException(message);
    }

    @Override
    public <U> Result<U, E> map(@NonNull Function<T, U> fn) {
        return new Ok<>(fn.apply(value));
    }

    @Override
    public <F> Result<T, F> mapErr(Function<E, F> fn) {
        return new Ok<>(value);
    }

    @Override
    public <U> Result<U, E> flatMap(Function<T, Result<U, E>> fn) {
        return fn.apply(value);
    }

    @Override
    public <U> Result<U, E> and(Result<U, E> other) {
        return other;
    }

    @Override
    public <U> Result<U, E> andThen(Function<T, Result<U, E>> fn) {
        return this.flatMap(fn);
    }

    @Override
    public Result<T, E> or(Result<T, E> other) {
        return this;
    }

    @Override
    public Result<T, E> orElse(Supplier<Result<T, E>> supplier) {
        return this;
    }


    @Override
    public @NonNull Option<T> ok() {
        return Option.some(value);
    }

    @Override
    public @NonNull Option<E> err() {
        return Option.none();
    }
}
