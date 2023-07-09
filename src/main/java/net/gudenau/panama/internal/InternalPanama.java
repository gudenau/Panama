package net.gudenau.panama.internal;

import net.gudenau.panama.Binder;
import net.gudenau.panama.PanamaArena;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.nio.file.Path;

public final class InternalPanama {
    public static Binder binder(Path path) {
        throw new AssertionError();
    }

    public static PanamaType type(Class<?> type) {
        throw new AssertionError();
    }

    public static PanamaSegment createNullSegment() {
        throw new AssertionError();
    }

    public static PanamaArena openArena(boolean shared) {
        throw new AssertionError();
    }
}
