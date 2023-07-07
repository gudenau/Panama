package net.gudenau.panama.internal.codegen;

import net.gudenau.panama.internal.SharedLock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class GeneratedClassCache {
    private final Definer definer;

    public GeneratedClassCache(Definer definer) {
        this.definer = definer;
    }

    private final Map<String, Class<?>> WRAPPERS = new HashMap<>();
    private final SharedLock WRAPPERS$LOCK = new SharedLock();

    public Class<?> getOrGenerate(String key, Supplier<byte[]> factory) {
        Class<?> type = WRAPPERS$LOCK.read(() -> WRAPPERS.get(key));
        if(type != null) {
            return type;
        }

        var bytecode = factory.get();

        return WRAPPERS$LOCK.write(() ->
            WRAPPERS.computeIfAbsent(key, (k) -> {
                try {
                    return definer.invoke(bytecode);
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to define generated class: " + k, e);
                }
            })
        );
    }

    @FunctionalInterface
    public interface Definer {
        Class<?> invoke(byte[] bytecode) throws Throwable;
    }
}
