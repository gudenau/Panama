package net.gudenau.panama.internal;

import net.gudenau.panama.PanamaSegment;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentScope;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class InternalSegment implements PanamaSegment {
    public static final PanamaSegment NULL = new InternalSegment(0, 0);

    public static PanamaSegment of(MemorySegment segment) {
        return segment.equals(MemorySegment.NULL) ? NULL : new InternalSegment(segment.address(), segment.byteSize());
    }

    private final long address;
    private final long size;

    private InternalSegment(long address, long size) {
        this.address = address;
        this.size = size;
    }

    @Override
    public long address() {
        return address;
    }

    @Override
    public long byteSize() {
        return size;
    }

    @Override
    public PanamaSegment withSize(long size) {
        return new InternalSegment(address, size);
    }

    @Override
    public ByteBuffer buffer() {
        return segment().asByteBuffer().order(ByteOrder.nativeOrder());
    }

    public MemorySegment memoryAddress() {
        return MemorySegment.ofAddress(address, 0, SegmentScope.global());
    }

    public MemorySegment segment() {
        return MemorySegment.ofAddress(address, size, SegmentScope.global());
    }
}
