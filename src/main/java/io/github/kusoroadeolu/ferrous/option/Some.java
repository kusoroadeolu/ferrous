package io.github.kusoroadeolu.ferrous.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

record Some<T>(T value) implements Option<T> {

    @Override
    public boolean isSome() {
        return true;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public T unwrap() {
        return value;
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
    public <U> Option<U> map(Function<T, U> fn) {
        return new Some<>(fn.apply(value));
    }

    @Override
    public <U> Option<U> flatMap(Function<T, Option<U>> fn) {
        return fn.apply(value);
    }

    @Override
    public Option<T> filter(Predicate<T> predicate) {
        return predicate.test(value) ? new Some<>(value) : new None<>();
    }

    @Override
    public <U> Option<U> and(Option<U> other) {
        return other;
    }

    @Override
    public <U> Option<U> andThen(Function<T, Option<U>> fn) {
        return fn.apply(value);
    }

    @Override
    public Option<T> or(Option<T> other) {
        return new Some<>(value);
    }

    @Override
    public Option<T> orElse(Supplier<Option<T>> supplier) {
        return new Some<>(value);
    }

    @Override
    public <R> R ifSomeOrElse(Function<T, R> someFn, Supplier<R> noneFn) {
        return someFn.apply(value);
    }

    @Override
    public void ifSomeOrElse(Consumer<T> someFn, Runnable noneFn) {
        someFn.accept(value);
    }

    @Override
    public T toNullable() {
        return value;
    }
}
