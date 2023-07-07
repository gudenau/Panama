package net.gudenau.panama;

import net.gudenau.panama.internal.InternalPanama;

public interface PanamaType {
    PanamaType BYTE = InternalPanama.type(byte.class);
    PanamaType SHORT = InternalPanama.type(short.class);
    PanamaType INTEGER = InternalPanama.type(int.class);
    PanamaType LONG = InternalPanama.type(long.class);
    PanamaType CHAR = InternalPanama.type(char.class);
    PanamaType FLOAT = InternalPanama.type(float.class);
    PanamaType DOUBLE = InternalPanama.type(double.class);
    PanamaType ADDRESS = InternalPanama.type(PanamaSegment.class);

    long byteSize();
    long byteAlignment();
}
