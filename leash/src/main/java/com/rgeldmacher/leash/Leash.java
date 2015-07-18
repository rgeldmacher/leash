package com.rgeldmacher.leash;

/**
 * @author robertgeldmacher
 */
public final class Leash {

    public static final String SUFFIX = "Leash";

    private Leash() {

    }

    public static void restore(Object leashSource) {
        Class<?> targetClass = leashSource.getClass();
        LeashBinding leash = getLeashBinding(targetClass.getName());
        leash.restore(leashSource);
    }

    public static void retain(Object leashSource) {
        Class<?> targetClass = leashSource.getClass();
        LeashBinding leash = getLeashBinding(targetClass.getName());
        leash.retain(leashSource);
    }

    public static void clear(Object leashSource) {
        Class<?> targetClass = leashSource.getClass();
        LeashBinding leash = getLeashBinding(targetClass.getName());
        leash.clear(leashSource);
    }

    private static LeashBinding getLeashBinding(String className) {
        try {
            Class<?> leashClass = Class.forName(className + SUFFIX);
            return (LeashBinding) leashClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to find leash for " + className, e);
        }
    }

    /**
     * Used internally by Leash. DO NOT USE!
     */
    public interface LeashBinding<T> {

        void restore(T source);

        void retain(T source);

        void clear(T source);
    }
}
