package io.github.kusoroadeolu.ferrous.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

record None<T>() implements Option<T> {
    @Override
    public boolean isSome() {
        return false;
    }

    @Override
    public boolean isNone() {
        return true;
    }

    @Override
    public T unwrap() {
        throw new OptionException("unwrap called on 'none' type");
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
        throw new OptionException(message);
    }

    @Override
    public <U> Option<U> map(Function<T, U> fn) {
        return new None<>();
    }

    @Override
    public <U> Option<U> flatMap(Function<T, Option<U>> fn) {
        return new None<>();
    }

    @Override
    public Option<T> filter(Predicate<T> predicate) {
        return new None<>();
    }

    @Override
    public <U> Option<U> and(Option<U> other) {
        return other;
    }

    @Override
    public <U> Option<U> andThen(Function<T, Option<U>> fn) {
        return new None<>();
    }

    @Override
    public Option<T> or(Option<T> other) {
        return other;
    }

    @Override
    public Option<T> orElse(Supplier<Option<T>> supplier) {
        return supplier.get();
    }


    @Override
    public T toNullable() {
        return null;
    }
}
