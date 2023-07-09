package net.gudenau.panama.internal;

import net.gudenau.panama.Binder;
import net.gudenau.panama.PanamaArena;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.nio.charset.StandardCharsets;

public class Test {
    public static void main(String[] args) throws Throwable {
        try(var arena = PanamaArena.openConfined()) {
            var binder = Binder.system();
            var printf = binder.downcall("printf", 1, PanamaType.INTEGER, PanamaType.ADDRESS, PanamaType.INTEGER);
            var segment = arena.allocateString("Hello panama! %d");
            var ignored = (int) printf.invokeExact(segment, Runtime.version().feature());
        }
    }
}
