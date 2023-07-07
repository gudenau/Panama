package net.gudenau.panama.internal;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record InternalType(
    Class<?> type,
    MemoryLayout layout,
    char shorthand
) implements PanamaType {
    private static final Map<Class<?>, PanamaType> TYPES = Stream.of(
            new InternalType(byte.class, CLinker.C_CHAR, 'b'),
            new InternalType(short.class, CLinker.C_SHORT, 's'),
            new InternalType(int.class, CLinker.C_INT, 'i'),
            new InternalType(long.class, CLinker.C_LONG_LONG, 'l'),
            new InternalType(char.class, CLinker.C_SHORT, 'c'),
            new InternalType(float.class, CLinker.C_FLOAT, 'f'),
            new InternalType(double.class, CLinker.C_DOUBLE, 'd'),
            new InternalType(PanamaSegment.class, CLinker.C_POINTER, 'r')
        ).collect(Collectors.toUnmodifiableMap(
            InternalType::type,
            Function.identity()
        ));

    public static PanamaType of(Class<?> type) {
        var layout = TYPES.get(type);
        if(layout == null) {
            throw new IllegalArgumentException("Unknown type: " + type.getName());
        }
        return layout;
    }

    public Class<?> panamaType() {
        return type == PanamaSegment.class ? MemoryAddress.class : type;
    }

    @Override
    public long byteSize() {
        return layout.byteSize();
    }

    @Override
    public long byteAlignment() {
        return layout.byteAlignment();
    }
}
