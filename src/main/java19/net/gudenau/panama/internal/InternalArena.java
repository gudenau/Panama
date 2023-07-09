package net.gudenau.panama.internal;

import net.gudenau.panama.PanamaArena;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.lang.foreign.MemorySession;

public final class InternalArena implements PanamaArena {
    private final MemorySession session;

    public InternalArena(boolean shared) {
        session = shared ? MemorySession.openShared() : MemorySession.openConfined();
    }

    @Override
    public PanamaSegment allocate(long size, long alignment) {
        return InternalSegment.of(session.allocate(size, alignment));
    }

    @Override
    public PanamaSegment allocateArray(PanamaType type, long count) {
        return InternalSegment.of(session.allocateArray(((InternalType) type).layout(), count));
    }

    @Override
    public void close() {
        session.close();
    }
}
