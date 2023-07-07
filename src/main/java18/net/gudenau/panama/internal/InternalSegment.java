package net.gudenau.panama.internal;

import jdk.incubator.foreign.Addressable;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;
import net.gudenau.panama.PanamaSegment;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class InternalSegment implements PanamaSegment {
    public static final PanamaSegment NULL = new InternalSegment(0, 0);

    public static PanamaSegment of(MemoryAddress address) {
        return address.equals(MemoryAddress.NULL) ? NULL : new InternalSegment(address.toRawLongValue(), 0);
    }

    public static PanamaSegment of(MemorySegment segment) {
        return segment.address().equals(MemoryAddress.NULL) ? NULL : new InternalSegment(segment.address().toRawLongValue(), segment.byteSize());
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

    public Addressable memoryAddress() {
        return MemoryAddress.ofLong(address);
    }

    public MemorySegment segment() {
        return MemorySegment.ofAddress(MemoryAddress.ofLong(address), size, ResourceScope.globalScope());
    }
}
