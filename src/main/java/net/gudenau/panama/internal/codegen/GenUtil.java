package net.gudenau.panama.internal.codegen;

import net.gudenau.panama.PanamaSegment;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

public final class GenUtil {
    private GenUtil() {
        throw new AssertionError();
    }

    public static String[] internalNames(List<Type> types) {
        if(types == null || types.isEmpty()) {
            return null;
        }

        return types.stream()
            .map(Type::getInternalName)
            .toArray(String[]::new);
    }

    public static Type getShorthandType(char shorthand) {
        return switch(shorthand) {
            case 'b' -> Type.BYTE_TYPE;
            case 's' -> Type.SHORT_TYPE;
            case 'i' -> Type.INT_TYPE;
            case 'l' -> Type.LONG_TYPE;
            case 'c' -> Type.CHAR_TYPE;
            case 'f' -> Type.FLOAT_TYPE;
            case 'd' -> Type.DOUBLE_TYPE;
            case 'v' -> Type.VOID_TYPE;
            case 'r' -> Type.getType(PanamaSegment.class);
            default -> throw new IllegalArgumentException("Unknown shorthand " + shorthand);
        };
    }

    public static void pushClassReference(MethodVisitor visitor, Type type) {
        switch (type.getSort()) {
            case Type.BYTE -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
            case Type.SHORT -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
            case Type.INT -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            case Type.LONG -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
            case Type.BOOLEAN -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
            case Type.CHAR -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
            case Type.FLOAT -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
            case Type.DOUBLE -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
            case Type.VOID -> visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
            default -> visitor.visitLdcInsn(type);
        }
    }

    public static void pushShorthandClassReference(MethodVisitor method, char shorthand) {
        switch(shorthand) {
            case 'b' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
            case 's' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
            case 'i' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            case 'l' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
            case 'z' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
            case 'c' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
            case 'f' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
            case 'd' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
            case 'v' -> method.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Void", "TYPE", "Ljava/lang/Class;");
            case 'r' -> method.visitLdcInsn(Type.getType(PanamaSegment.class));
            default -> throw new IllegalArgumentException("Unknown shorthand " + shorthand);
        };
    }

    public static byte[] createDummy(String destination) {
        var Dummy = Type.getObjectType('L' + destination + ".Dummy;");
        var Object = Type.getType(Object.class);

        return new ClassGenerator(Opcodes.ACC_FINAL | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, Dummy, Object, null).toByteArray();
    }
}
