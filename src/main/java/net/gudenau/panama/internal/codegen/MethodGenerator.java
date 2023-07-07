package net.gudenau.panama.internal.codegen;

import org.objectweb.asm.*;

public final class MethodGenerator extends MethodVisitor {
    private final boolean virtual;
    private final Type owner;

    MethodGenerator(MethodVisitor visitor, int access, Type owner) {
        super(Opcodes.ASM9, visitor);

        virtual = (access & Opcodes.ACC_STATIC) == 0;
        this.owner = owner;
    }

    public Label visitLabel() {
        var label = new Label();
        visitLabel(label);
        return label;
    }

    @Deprecated
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    public void visitMethodInsn(int opcode, Type owner, String name, Type returnType, Type... args) {
        super.visitMethodInsn(opcode, owner.getInternalName(), name, Type.getMethodDescriptor(returnType, args), false);
    }

    @Deprecated
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    public void visitFieldInsn(int opcode, Type owner, String name, Type description) {
        super.visitFieldInsn(opcode, owner.getInternalName(), name, description.getDescriptor());
    }

    @Deprecated
    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    public void visitLocalVariable(String name, Type type, Label start, Label end, int index) {
        super.visitLocalVariable(name, type.getDescriptor(), null, start, end, index);
    }

    public void storeLocal(int local, String name, Type type) {
        if(virtual) {
            visitVarInsn(Opcodes.ALOAD, 0);
        }
        visitVarInsn(Opcodes.ALOAD, local);
        visitFieldInsn(virtual ? Opcodes.PUTFIELD : Opcodes.PUTSTATIC, owner, name, type);
    }

    @Deprecated
    @Override
    public void visitTypeInsn(int opcode, String type) {
        super.visitTypeInsn(opcode, type);
    }

    public void visitTypeInsn(int opcode, Type type) {
        super.visitTypeInsn(opcode, type.getInternalName());
    }

    public void loadInt(MethodVisitor method, int value) {
        if(value >= -1 && value <= 5) {
            method.visitInsn(Opcodes.ICONST_0 + value);
        } else if(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            method.visitIntInsn(Opcodes.BIPUSH, value);
        } else if(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            method.visitIntInsn(Opcodes.SIPUSH, value);
        } else {
            method.visitLdcInsn(value);
        }
    }

    public void instantiate(Type type, Type description, Runnable action) {
        visitTypeInsn(Opcodes.NEW, type);
        visitInsn(Opcodes.DUP);
        action.run();
        visitMethodInsn(Opcodes.INVOKESPECIAL, type, "<init>", Type.VOID_TYPE, description.getArgumentTypes());
    }

    public void getField(String name, Type type) {
        if(virtual) {
            visitVarInsn(Opcodes.ALOAD, 0);
            visitFieldInsn(Opcodes.GETFIELD, owner, name, type);
        } else {
            visitFieldInsn(Opcodes.GETSTATIC, owner, name, type);
        }
    }
}
