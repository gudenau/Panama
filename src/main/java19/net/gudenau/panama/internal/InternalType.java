package net.gudenau.panama.internal;

import net.gudenau.panama.PanamaSegment;
import net.gudenau.panama.PanamaType;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
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
            new InternalType(byte.class, ValueLayout.JAVA_BYTE, 'b'),
            new InternalType(short.class, ValueLayout.JAVA_SHORT, 's'),
            new InternalType(int.class, ValueLayout.JAVA_INT, 'i'),
            new InternalType(long.class, ValueLayout.JAVA_LONG, 'l'),
            new InternalType(char.class, ValueLayout.JAVA_CHAR, 'c'),
            new InternalType(float.class, ValueLayout.JAVA_FLOAT, 'f'),
            new InternalType(double.class, ValueLayout.JAVA_DOUBLE, 'd'),
            new InternalType(PanamaSegment.class, ValueLayout.ADDRESS, 'r')
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

    @Override
    public long byteSize() {
        return layout.byteSize();
    }

    @Override
    public long byteAlignment() {
        return layout.byteAlignment();
    }
}
