package io.github.kusoroadeolu.ferrous.option;

import org.jspecify.annotations.NonNull;

public record Pair<A, B>(@NonNull A a, @NonNull B b){}
