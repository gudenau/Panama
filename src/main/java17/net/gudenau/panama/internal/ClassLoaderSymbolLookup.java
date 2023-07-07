package net.gudenau.panama.internal;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.SymbolLookup;
import net.gudenau.panama.internal.codegen.GenUtil;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class ClassLoaderSymbolLookup extends ClassLoader implements SymbolLookup {
    private static final Class<?> NativeLibraries;
    private static final Class<?> NativeLibrary;
    private static final MethodHandle ClassLoader$libraries$getter;
    private static final MethodHandle ClassLoader$findNative;
    private static final MethodHandle NativeLibraries$loadLibrary;
    static {
        try {
            NativeLibraries = Dragons.findClass(Object.class.getModule(), "jdk.internal.loader.NativeLibraries");
            NativeLibrary = Dragons.findClass(Object.class.getModule(), "jdk.internal.loader.NativeLibrary");
            ClassLoader$libraries$getter = Dragons.findVirtualGetter(ClassLoader.class, "libraries", NativeLibraries);
            ClassLoader$findNative = Dragons.findStaticMethod(ClassLoader.class, "findNative", MethodType.methodType(long.class, ClassLoader.class, String.class));
            NativeLibraries$loadLibrary = Dragons.findVirtualMethod(NativeLibraries, "loadLibrary", MethodType.methodType(NativeLibrary, Class.class, File.class));
        } catch (Throwable e) {
            throw new AssertionError("Failed to setup InternalBuilder", e);
        }
    }

    private final MethodHandle findNative;

    public ClassLoaderSymbolLookup(Path path) {
        Class<?> dummy;
        MethodHandle loadLibrary;
        try {
            var libraries = ClassLoader$libraries$getter.invoke(this);
            loadLibrary = NativeLibraries$loadLibrary.bindTo(libraries);
            findNative = ClassLoader$findNative.bindTo(this);

            dummy = Dragons.lookup(getClass()).defineClass(GenUtil.createDummy(InternalBinder.class.getPackageName()));
        } catch (Throwable e) {
            throw new AssertionError("Failed to setup the ClassLoader for a Binder", e);
        }

        Object result;
        try {
            result = loadLibrary.invoke(dummy, path.toFile());
        } catch (Throwable e) {
            throw new LinkageError("Failed to load library: " + path, e);
        }
        if(result == null) {
            throw new UnsatisfiedLinkError("Failed to load library: " + path);
        }
    }

    private long findNative(String name) {
        try {
            return (long) findNative.invokeExact((ClassLoader) this, name);
        } catch (Throwable e) {
            throw new AssertionError("Failed to invoke findNative", e);
        }
    }

    @Override
    public Optional<MemoryAddress> lookup(String name) {
        Objects.requireNonNull(name);
        var address = MemoryAddress.ofLong(findNative(name));
        return address == MemoryAddress.NULL ? Optional.empty() : Optional.of(address);
    }
}
