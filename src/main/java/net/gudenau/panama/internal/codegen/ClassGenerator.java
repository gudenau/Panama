package net.gudenau.panama.internal.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Consumer;

import static net.gudenau.panama.internal.codegen.GenUtil.internalNames;

public final class ClassGenerator extends ClassVisitor {
    private static final int CLASS_VERSION = switch (Runtime.version().feature()) {
        case 16 -> Opcodes.V16;
        case 17 -> Opcodes.V17;
        case 18 -> Opcodes.V18;
        case 19 -> Opcodes.V19;
        default -> Opcodes.V20;
    };

    private final Type name;

    public ClassGenerator(int access, Type name, Type parent, List<Type> interfaces) {
        super(Opcodes.ASM9, new ClassWriter(0));

        this.name = name;

        visit(CLASS_VERSION, access, name.getInternalName(), null, parent.getInternalName(), internalNames(interfaces));
    }

    public void field(int access, String name, Type type) {
        visitField(access, name, type.getDescriptor(), null, null).visitEnd();
    }

    public void constructor(int access, Type type, List<Type> exceptions, Consumer<MethodGenerator> generator) {
        method(access, "<init>", type, exceptions, generator);
    }

    public void method(int access, String name, Type type, List<Type> exceptions, Consumer<MethodGenerator> generator) {
        var visitor = new MethodGenerator(visitMethod(access, name, type.getDescriptor(), null, internalNames(exceptions)), access, this.name);
        visitor.visitCode();
        try {
            generator.accept(visitor);
        } finally {
            visitor.visitEnd();
        }
    }

    public byte[] toByteArray() {
        return ((ClassWriter) cv).toByteArray();
    }
}
