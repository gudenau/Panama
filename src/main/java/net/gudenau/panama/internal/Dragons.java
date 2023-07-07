package net.gudenau.panama.internal;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Objects;

public final class Dragons {
    private Dragons() {
        throw new AssertionError();
    }

    private static final Unsafe THE_ONE = findUnsafe();
    private static final long AccessibleObject$override = findOverrideCookie();
    private static final MethodHandles.Lookup DRAGON_LOOKUP = createLookup();
    private static final long MethodHandles$Lookup$allowedModes = findAllowedModes();

    private static Unsafe findUnsafe() {
        var exceptions = new ArrayList<Throwable>();
        for(var field : Unsafe.class.getDeclaredFields()) {
            if(field.getType() != Unsafe.class || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                var unsafe = field.get(null);
                if(unsafe instanceof Unsafe) {
                    return (Unsafe) unsafe;
                }
            } catch(Throwable e) {
                exceptions.add(e);
            }
        }

        var exception = new AssertionError("Failed to get Unsafe handle");
        exceptions.forEach(exception::addSuppressed);
        throw exception;
    }

    private static long findOverrideCookie() {
        var object = allocateInstance(AccessibleObject.class);
        for(long cookie = 4; cookie < 64; cookie++) {
            object.setAccessible(false);
            if(getBoolean(object, cookie)) {
                continue;
            }

            object.setAccessible(true);
            if(getBoolean(object, cookie)) {
                return cookie;
            }
        }

        throw new AssertionError("Failed to find cookie for AccessibleObject#override");
    }

    private static MethodHandles.Lookup createLookup() {
        try {
            var constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
            setAccessible(constructor, true);
            return constructor.newInstance(Object.class, null, -1);
        } catch (Throwable e) {
            throw new AssertionError("Failed to create dragon lookup", e);
        }
    }

    private static long findAllowedModes() {
        for(long cookie = 4; cookie < 64; cookie += Integer.BYTES) {
            if(getInt(DRAGON_LOOKUP, cookie) == -1) {
                return cookie;
            }
        }

        throw new AssertionError("Failed to find cookie for MethodHandles.Lookup#allowedModes");
    }

    public static void setAccessible(AccessibleObject object, boolean accessible) {
        putBoolean(object, AccessibleObject$override, accessible);
    }

    public static boolean getBoolean(Object instance, long cookie) {
        return THE_ONE.getBoolean(instance, cookie);
    }

    public static void putBoolean(Object instance, long cookie, boolean value) {
        THE_ONE.putBoolean(instance, cookie, value);
    }

    public static int getInt(Object instance, long cookie) {
        return THE_ONE.getInt(instance, cookie);
    }

    public static void putInt(Object instance, long cookie, int value) {
        THE_ONE.putInt(instance, cookie, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> type) {
        try {
            return (T) THE_ONE.allocateInstance(type);
        } catch (InstantiationException e) {
            throw new AssertionError("Failed", e);
        }
    }

    public static MethodHandles.Lookup lookup(Class<?> target) {
        var lookup = DRAGON_LOOKUP.in(target);
        // Let's really play with fire
        putInt(lookup, MethodHandles$Lookup$allowedModes, -1);
        return lookup;
    }

    public static MethodHandle findVirtualGetter(Class<?> owner, String name, Class<?> type) throws NoSuchFieldException {
        try {
            return DRAGON_LOOKUP.findGetter(owner, name, type);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Dragon lookup was unable to access " + owner.getName() + "#" + name, e);
        }
    }

    public static MethodHandle findStaticSetter(Class<?> owner, String name, Class<?> type) throws NoSuchFieldException {
        try {
            return DRAGON_LOOKUP.findStaticSetter(owner, name, type);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Dragon lookup was unable to access " + owner.getName() + "#" + name, e);
        }
    }

    public static MethodHandle findVirtualMethod(Class<?> owner, String name, MethodType type) throws NoSuchMethodException {
        try {
            return DRAGON_LOOKUP.findVirtual(owner, name, type);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Dragon lookup was unable to access " + owner.getName() + "#" + name + type.descriptorString(), e);
        }
    }

    public static MethodHandle bindVirtualMethod(Object instance, String name, MethodType type) throws NoSuchMethodException {
        Objects.requireNonNull(instance, "instance can't be null");
        try {
            return DRAGON_LOOKUP.bind(instance, name, type);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Dragon lookup was unable to access " + instance.getClass().getName() + "#" + name + type.descriptorString(), e);
        }
    }

    public static MethodHandle findStaticMethod(Class<?> owner, String name, MethodType type) throws NoSuchMethodException {
        try {
            return DRAGON_LOOKUP.findStatic(owner, name, type);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Dragon lookup was unable to access " + owner.getName() + "#" + name + type.descriptorString(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> findClass(Module module, String name) throws ClassNotFoundException {
        var loader = module.getClassLoader();
        if(loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return (Class<T>) loader.loadClass(name);
    }
}
