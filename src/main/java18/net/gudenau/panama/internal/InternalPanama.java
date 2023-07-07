package net.gudenau.panama.internal;

import net.gudenau.panama.Binder;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.lang.invoke.MethodType;
import java.nio.file.Path;

public final class InternalPanama {
    static {
        try {
            var module = InternalPanama.class.getModule();
            var handle = Dragons.bindVirtualMethod(module, "implAddEnableNativeAccess", MethodType.methodType(Module.class));
            handle.invoke();
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

    public static PanamaSegment createNullSegment() {
        return InternalSegment.NULL;
    }
}
