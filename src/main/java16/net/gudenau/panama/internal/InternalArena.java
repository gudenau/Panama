package net.gudenau.panama.internal;

import jdk.incubator.foreign.NativeScope;
import net.gudenau.panama.PanamaArena;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.nio.charset.Charset;

public final class InternalArena implements PanamaArena {
    private final NativeScope scope;

    public InternalArena(boolean shared) {
        if(shared) {
            throw new IllegalArgumentException("Shared arenas are not supported on Java 16");
        }

        scope = NativeScope.unboundedScope();
    }

    @Override
    public PanamaSegment allocate(long size, long alignment) {
        return InternalSegment.of(scope.allocate(size, alignment));
    }

    @Override
    public PanamaSegment allocateArray(PanamaType type, long count) {
        return InternalSegment.of(scope.allocateArray(((InternalType) type).layout(), count));
    }

    @Override
    public void close() {
        scope.close();
    }
}
