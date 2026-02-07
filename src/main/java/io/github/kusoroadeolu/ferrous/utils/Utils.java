package io.github.kusoroadeolu.ferrous.utils;

import java.util.Objects;

public class Utils {
    private Utils(){}

    public static void throwIfNull(Object val){
        if (Objects.isNull(val)) throw new NullPointerException();
    }
}
