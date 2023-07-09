package net.gudenau.panama.internal;

import net.gudenau.panama.Binder;
import net.gudenau.panama.PanamaArena;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.nio.file.Path;

public final class InternalPanama {
    static {
        try {
            // Oddly enough this is not in the foreign module
            var Utils = Dragons.findClass(Object.class.getModule(), "jdk.internal.foreign.Utils");
            var setter = Dragons.findStaticSetter(Utils, "foreignRestrictedAccess", String.class);
            setter.invokeExact("permit");
        } catch (Throwable e) {
            throw new AssertionError("Failed to setup InternalPanama", e);
        }
    }

    public static Binder binder(Path path) {
        return InternalBinder.of(path);
    }

    public static PanamaType type(Class<?> type) {
        return InternalType.of(type);
    }

    public static PanamaSegment getNullSegment() {
        return InternalSegment.NULL;
    }

    public static PanamaArena openArena(boolean shared) {
        return new InternalArena(shared);
    }
}
