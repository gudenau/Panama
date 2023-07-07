package net.gudenau.panama;

import net.gudenau.panama.internal.InternalPanama;

import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.Objects;

public interface Binder {
    static Binder of(Path path) {
        Objects.requireNonNull(path, "path can not be null");

        return InternalPanama.binder(path);
    }

    static Binder system() {
        return InternalPanama.binder(null);
    }

    MethodHandle downcall(String name, PanamaType result, PanamaType... args);

    MethodHandle downcall(String name, int vararg, PanamaType result, PanamaType... args);
}
