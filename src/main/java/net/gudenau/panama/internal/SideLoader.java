package net.gudenau.panama.internal;

public final class SideLoader extends ClassLoader {
    public Class<?> sideLoad(String name, byte[] code) {
        return defineClass(name, code, 0, code.length);
    }
}
