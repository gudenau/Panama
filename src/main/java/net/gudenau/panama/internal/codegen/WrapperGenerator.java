package net.gudenau.panama.internal.codegen;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class WrapperGenerator {
    private WrapperGenerator() {
        throw new AssertionError();
    }

    public static byte[] generateWrapper(Class<?> owner, String key, Class<?> foreignAddress, Class<?> panamaSegment) {
        return generateWrapper(owner, key, foreignAddress, foreignAddress, panamaSegment);
    }

    public static byte[] generateWrapper(Class<?> owner, String key, Class<?> foreignAddress, Class<?> foreignAddressable, Class<?> panamaSegment) {
        var Object = Type.getType(Object.class);
        var MethodHandle = Type.getType(java.lang.invoke.MethodHandle.class);
        var Generated = Type.getObjectType(Type.getInternalName(owner) + '$' + key);
        var Throwable = Type.getType(Throwable.class);
        var PanamaSegment = Type.getType(net.gudenau.panama.PanamaSegment.class);
        var MethodType = Type.getType(java.lang.invoke.MethodType.class);
        var Class = Type.getType(Class.class);
        var Class$Array = Type.getType('[' + Class.getDescriptor());
        var Dragons = Type.getType(net.gudenau.panama.internal.Dragons.class);
        var String = Type.getType(String.class);
        var InternalSegment = Type.getType(panamaSegment);
        var MemoryAddress = Type.getType(foreignAddress);
        var Addressable = Type.getType(foreignAddressable);

        var result = GenUtil.getShorthandType(key.charAt(0));
        var arguments = key.substring(1).chars()
            .mapToObj((shorthand) -> GenUtil.getShorthandType((char) shorthand))
            .toArray(Type[]::new);

        var generator = new ClassGenerator(Opcodes.ACC_FINAL, Generated, Object, null);
        generator.field(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "handle", MethodHandle);

        generator.constructor(Opcodes.ACC_PUBLIC, Type.getMethodType(Type.VOID_TYPE, MethodHandle), List.of(Throwable), (method) -> {
            var start = method.visitLabel();

            // super()
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, Object, "<init>", Type.VOID_TYPE);

            // this.handle = handle
            method.storeLocal(1, "handle", MethodHandle);

            // return;
            method.visitInsn(Opcodes.RETURN);

            var end = method.visitLabel();

            method.visitMaxs(2, 2);
            method.visitLocalVariable("this", Generated, start, end, 0);
            method.visitLocalVariable("handle", MethodHandle, start, end, 1);
        });

        // public static MethodHandle wrap(MethodHandle handle) throws Throwable {
        generator.method(Opcodes.ACC_STATIC, "wrap", Type.getMethodType(MethodHandle, MethodHandle), List.of(Throwable), (method) -> {
            var start = method.visitLabel();

            // new Generated(handle)
            method.instantiate(Generated, Type.getMethodType(Type.VOID_TYPE, MethodHandle), () -> {
                method.visitVarInsn(Opcodes.ALOAD, 0);
            });

            method.visitLdcInsn("invoke");

            // MethodType.methodType(publicResult, new Class[] { publicArgs })
            // -- or --
            // MethodType.methodType(publicResult, publicArgs[0])
            GenUtil.pushShorthandClassReference(method, key.charAt(0));
            if(arguments.length == 1) {
                GenUtil.pushShorthandClassReference(method, key.charAt(1));
                method.visitMethodInsn(Opcodes.INVOKESTATIC, MethodType, "methodType", MethodType, Class, Class);
            } else {
                method.loadInt(method, arguments.length);
                method.visitTypeInsn(Opcodes.ANEWARRAY, Class);
                var chars = key.substring(1).toCharArray();
                for(int i = 0, length = chars.length; i < length; i++) {
                    method.visitInsn(Opcodes.DUP);
                    method.loadInt(method, i);
                    GenUtil.pushShorthandClassReference(method, chars[i]);
                    method.visitInsn(Opcodes.AASTORE);
                }
                method.visitMethodInsn(Opcodes.INVOKESTATIC, MethodType, "methodType", MethodType, Class, Class$Array);
            }

            // return Dragons.bindVirtualMethod(generated, "invoke", methodType);
            method.visitMethodInsn(Opcodes.INVOKESTATIC, Dragons, "bindVirtualMethod", MethodHandle, Object, String, MethodType);
            method.visitInsn(Opcodes.ARETURN);

            var end = method.visitLabel();

            method.visitMaxs(arguments.length == 1 ? 4 : 7, 1);
            method.visitLocalVariable("handle", MethodHandle, start, end, 0);
        });

        generator.method(Opcodes.ACC_PUBLIC, "invoke", Type.getMethodType(result, arguments), List.of(Throwable), (method) -> {
            var start = method.visitLabel();

            // this.handle
            method.getField("handle", MethodHandle);

            final int argCount = arguments.length;
            // handle.invokeExact(...)
            int slot = 1;
            for (var argument : arguments) {
                method.visitVarInsn(argument.getOpcode(Opcodes.ILOAD), slot);
                if (argument.equals(PanamaSegment)) {
                    method.visitTypeInsn(Opcodes.CHECKCAST, InternalSegment);
                    method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, InternalSegment, "memoryAddress", Addressable);
                }
                slot += argument.getSize();
            }

            var res = result.getSort() == Type.OBJECT ? MemoryAddress : result;
            var args = new Type[argCount];
            for(int i = 0; i < argCount; i++) {
                var arg = arguments[i];
                args[i] = arg.getSort() == Type.OBJECT ? Addressable : arg;
            }
            method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, MethodHandle, "invokeExact", res, args);

            if(result.equals(PanamaSegment)) {
                method.visitMethodInsn(Opcodes.INVOKESTATIC, InternalSegment, "of", PanamaSegment, MemoryAddress);
            }
            method.visitInsn(result.getOpcode(Opcodes.IRETURN));

            var end = method.visitLabel();

            method.visitLocalVariable("this", Generated, start, end, 0);
            slot = 1;
            for(var argument : arguments) {
                method.visitLocalVariable("arg" + slot, argument, start, end, slot);
                slot += argument.getSize();
            }
            method.visitMaxs(slot, slot);
        });

        var code = generator.toByteArray();

        try {
            var path = Path.of("dump", Integer.toString(Runtime.version().feature()), Generated.getClassName() + ".class");
            var parent = path.getParent();
            if(!Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            try (var stream = Files.newOutputStream(path)) {
                stream.write(code);
            }
        }catch (IOException ignored) {}

        return code;
    }
}
