package net.gudenau.panama.internal;

import net.gudenau.panama.PanamaArena;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.lang.foreign.Arena;

public final class InternalArena implements PanamaArena {
    private final Arena arena;

    public InternalArena(boolean shared) {
        arena = shared ? Arena.openShared() : Arena.openConfined();
    }

    @Override
    public PanamaSegment allocate(long size, long alignment) {
        return InternalSegment.of(arena.allocate(size, alignment));
    }

    @Override
    public PanamaSegment allocateArray(PanamaType type, long count) {
        return InternalSegment.of(arena.allocateArray(((InternalType) type).layout(), count));
    }

    @Override
    public void close() {
        arena.close();
    }
}
