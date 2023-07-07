package net.gudenau.panama.internal;

import jdk.incubator.foreign.*;
import net.gudenau.panama.Binder;
import net.gudenau.panama.PanamaType;
import net.gudenau.panama.internal.codegen.GeneratedClassCache;
import net.gudenau.panama.internal.codegen.WrapperGenerator;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public record InternalBinder(
    SymbolLookup lookup,
    String name
) implements Binder {
    private static final CLinker LINKER = CLinker.systemCLinker();
    private static final Binder SYSTEM = new InternalBinder(LINKER::lookup, "stdlib");

    public static Binder of(Path path) {
        if(path == null) {
            return SYSTEM;
        }

        var classLoader = new ClassLoaderSymbolLookup(path);

        return new InternalBinder(classLoader, path.getFileName().toString());
    }

    @Override
    public MethodHandle downcall(String name, PanamaType result, PanamaType... args) {
        return doDowncall(name, -1, result, args);
    }

    @Override
    public MethodHandle downcall(String name, int varargs, PanamaType result, PanamaType... args) {
        if(varargs < 0 || varargs >= args.length) {
            throw new IllegalArgumentException("varargs must be at least 0 and less than the amount of arguments");
        }

        return doDowncall(name, varargs, result, args);
    }

    private static final GeneratedClassCache CLASS_CACHE = new GeneratedClassCache(
        (bytecode) -> Dragons.lookup(InternalBinder.class).defineClass(bytecode)
    );

    public MethodHandle doDowncall(String name, int varargs, PanamaType result, PanamaType... args) {
        var symbol = lookup.lookup(name)
            .orElseThrow(() -> new NoSuchElementException("Failed to find symbol " + name + " in " + name()));

        var internalResult = (InternalType) result;
        var internalArgs = Stream.of(args)
            .map(InternalType.class::cast)
            .toArray(InternalType[]::new);

        var argLayouts = Stream.of(internalArgs)
            .map(InternalType::layout)
            .toArray(MemoryLayout[]::new);

        FunctionDescriptor descriptor;
        if(varargs != -1) {
            var normalArgs = Arrays.copyOf(argLayouts, varargs);
            var varArgs = Arrays.copyOfRange(argLayouts, varargs, args.length);
            descriptor = (
                internalResult == null ?
                    FunctionDescriptor.ofVoid(normalArgs) :
                    FunctionDescriptor.of(internalResult.layout(), normalArgs)
                ).asVariadic(varArgs);
        } else {
            descriptor = internalResult == null ?
                FunctionDescriptor.ofVoid(argLayouts) :
                FunctionDescriptor.of(internalResult.layout(), argLayouts);
        }

        var baseHandle = LINKER.downcallHandle(symbol, descriptor);
        var plainHandle = internalResult != PanamaType.ADDRESS;
        if(plainHandle) {
            for (var arg : internalArgs) {
                if(arg == PanamaType.ADDRESS) {
                    plainHandle = false;
                    break;
                }
            }
        }

        if(plainHandle) {
            return baseHandle;
        }

        StringBuilder key = new StringBuilder(1 + internalArgs.length);
        key.append(internalResult == null ? 'v' : internalResult.shorthand());
        for (var arg : internalArgs) {
            key.append(arg.shorthand());
        }
        var wrapperType = CLASS_CACHE.getOrGenerate(key.toString(), () -> WrapperGenerator.generateWrapper(InternalBinder.class, key.toString(), MemoryAddress.class, Addressable.class, InternalSegment.class));

        try {
            var wrapper = Dragons.findStaticMethod(wrapperType, "wrap", MethodType.methodType(MethodHandle.class, MethodHandle.class));
            return (MethodHandle) wrapper.invokeExact(baseHandle);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to wrap function " + name, e);
        }
    }
}
