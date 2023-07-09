package net.gudenau.panama;

import net.gudenau.panama.internal.InternalPanama;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface PanamaArena extends AutoCloseable {
    static PanamaArena openConfined() {
        return InternalPanama.openArena(false);
    }

    static PanamaArena openShared() {
        return InternalPanama.openArena(true);
    }

    PanamaSegment allocate(long size, long alignment);

    default PanamaSegment allocate(long size) {
        return allocate(size, 1);
    }

    default PanamaSegment allocate(PanamaType type) {
        return allocate(type.byteSize(), type.byteAlignment());
    }

    default PanamaSegment allocateString(String string) {
        return allocateString(string, StandardCharsets.UTF_8);
    }

    default PanamaSegment allocateString(String string, Charset charset) {
        var bytes = string.getBytes(charset);
        var length = bytes.length;
        var segment = allocate(length + 1);
        segment.buffer().put(0, bytes).put(length, (byte) 0);
        return segment;
    }

    PanamaSegment allocateArray(PanamaType type, long count);

    @Override void close();
}
