package net.gudenau.panama.internal;

import net.gudenau.panama.Binder;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.nio.charset.StandardCharsets;

public class Test {
    public static void main(String[] args) throws Throwable {
        var binder = Binder.system();
        var malloc = binder.downcall("malloc", PanamaType.ADDRESS, PanamaType.LONG);
        var free = binder.downcall("free", null, PanamaType.ADDRESS);
        var printf = binder.downcall("printf", 1, PanamaType.INTEGER, PanamaType.ADDRESS, PanamaType.INTEGER);
        var message = "Hello panama! %d\u0000".getBytes(StandardCharsets.UTF_8);
        var segment = ((PanamaSegment) malloc.invokeExact((long) message.length)).withSize(message.length);
        segment.buffer().put(0, message);
        var ignored = (int) printf.invokeExact(segment, Runtime.version().feature());
        free.invokeExact(segment);
    }
}
