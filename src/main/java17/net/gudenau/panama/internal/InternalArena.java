package net.gudenau.panama.internal;

import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import net.gudenau.panama.PanamaArena;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

public final class InternalArena implements PanamaArena {
    private final ResourceScope scope;
    private final SegmentAllocator allocator;

    public InternalArena(boolean shared) {
        scope = shared ? ResourceScope.newSharedScope() : ResourceScope.newConfinedScope();
        allocator = SegmentAllocator.ofScope(scope);
    }

    @Override
    public PanamaSegment allocate(long size, long alignment) {
        return InternalSegment.of(allocator.allocate(size, alignment));
    }

    @Override
    public PanamaSegment allocateArray(PanamaType type, long count) {
        return InternalSegment.of(allocator.allocateArray(((InternalType) type).layout(), count));
    }

    @Override
    public void close() {
        scope.close();
    }
}
