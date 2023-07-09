# Panama
A library for easing the usage of the foreign function and foreign memory APIs introduced in Java
[16](https://openjdk.org/jeps/389)/[14](https://openjdk.org/jeps/370) (respectfully). These APIs have been in a state of
flux so that every major Java version changes how the API works and breaks binary and source compatibility. The goal of
the Panama library is to retain source and binary compatibility between Java releases.

---

### Requirements
Java 16+ and `--enable-preview` passed to the JVM on versions 19+ and OW2's ASM library (just `asm`, none of the other
artifacts are required).

---

### Limitations

Java 16:
- Shared arenas are not supported

---

### Missing features
- Upcalls (I.E callbacks)
- Memory sessions
- A bunch of helpers
- Structures
- Unions

---

### Example
A basic "hello world" using Panama:

```Java
package com.example.panama;

import net.gudenau.panama.Binder;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.nio.charset.StandardCharsets;

public final class HelloPanama {
    public static void main(String[] args) throws Throwable {
        // The system binder contains the STD lib functions like printf.
        var binder = Binder.system();

        // Get handles to malloc, free and printf.
        var malloc = binder.downcall("malloc", PanamaType.ADDRESS, PanamaType.LONG);
        var free = binder.downcall("free", null, PanamaType.ADDRESS);
        // printf is special, it's a variadic function. The `1` passed here says the arguments starting at the first
        // index are variadic.
        var printf = binder.downcall("printf", 1, PanamaType.INTEGER, PanamaType.ADDRESS, PanamaType.INTEGER);

        // Get the bytes for our format string, there are better ways of doing this they just are not implemented yet
        var message = "Hello panama! %d\n\u0000".getBytes(StandardCharsets.UTF_8);
        
        // Allocate the memory via malloc (and assume it succeeds)
        var segment = ((PanamaSegment) malloc.invokeExact((long) message.length)).withSize(message.length);
        
        // Fill our allocated buffer with our format string
        segment.buffer().put(0, message);
        
        // Invoke printf
        var ignored = (int) printf.invokeExact(segment, Runtime.version().feature());
        
        // Free our buffer
        free.invokeExact(segment);
    }
}
```

A more complex and more "complete" example:

```Java
package com.example.panama;

import net.gudenau.panama.Binder;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

final class StdLib {
    private static final MethodHandle malloc;

    static PanamaSegment malloc(long size) {
        PanamaSegment segment;
        try {
            segment = (PanamaSegment) malloc.invokeExact(size);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke malloc", e);
        }

        if (segment.equals(PanamaSegment.NULL)) {
            throw new OutOfMemoryError("Failed to allocate " + size + " bytes via malloc");
        }

        return segment;
    }

    private static final MethodHandle free;
    static void free(PanamaSegment segment) {
        // PanamaSegment has a null value, a real null is illegal and free is a nop when passed NULL
        Objects.requireNonNull(segment, "segment can not be null");

        try {
            free.invokeExact(segment);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke free", e);
        }
    }
    
    // There are better ways of doing variadic and String stuff, but it is not implemented yet.
    private static final MethodHandle printf;
    static int printf(PanamaSegment format, int arg) {
        Objects.requireNonNull(segment, "segment can not be null");
        
        try {
            return (int) printf.invokeExact(format, arg);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke printf", e);
        }
    }
}
```

```Java
package com.example.panama;

import java.nio.charset.StandardCharsets;

public final class HelloPanama {
    public static void main(String[] args) {
        var string = "Hello panama! %d\n\u0000".getBytes(StandardCharsets.UTF_8);
        var buffer = StdLib.malloc(string.length);
        try {
            buffer.buffer().put(0, string);
            StdLib.printf(buffer, Runtime.version().feature());
        } finally {
            StdLib.free(buffer);
        }
    }
}
```

---

### How does this work?
Essentially this just uses [multi-release jar files](https://openjdk.org/jeps/238) and an abstraction layer on top.
Internally there are a few hacks based off of Unsafe to gently poke the JVM and some runtime generated classes to
allow the incompatible `MethodHandle` surfaces to be converted. The runtime speed penalty for this should be fairly low,
but it may still be noticeable depending on your use case. These are only used for pointer types, primitive-only
functions don't incur this overhead. 

A decompiled and annotated runtime generated class for a method that takes a pointer and doesn't return anything:
```Java
final class InternalBinder$vr {
    // The JDK provided method handle
    private final MethodHandle handle;

    public InternalBinder$vr(MethodHandle handle) {
        this.handle = handle;
    }

    // Creates a new instance of this wrapper and returns a method handle to it.
    static MethodHandle wrap(MethodHandle handle) throws Throwable {
        return Dragons.bindVirtualMethod(new InternalBinder$vr(handle), "invoke", MethodType.methodType(Void.TYPE, PanamaSegment.class));
    }

    // This does the actual marshalling, hopefully it's simple enough that the JIT can work its magic
    public void invoke(PanamaSegment arg1) throws Throwable {
        this.handle.invokeExact(((InternalSegment)arg1).memoryAddress());
    }
}
```
