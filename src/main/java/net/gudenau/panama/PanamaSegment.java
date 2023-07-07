package net.gudenau.panama;

import net.gudenau.panama.internal.InternalPanama;

import java.nio.ByteBuffer;

public interface PanamaSegment {
    PanamaSegment NULL = InternalPanama.createNullSegment();

    long address();
    long byteSize();

    PanamaSegment withSize(long size);

    ByteBuffer buffer();
}
