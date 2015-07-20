package com.rgeldmacher.leash;

import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author robertgeldmacher
 */
public final class Leash {

    public static final String SUFFIX = "Leash";

    private Leash() {

    }

    public static void restore(Activity source) {
        callLeash("restore", source);
    }

    public static void restore(Fragment source) {
        callLeash("restore", source);
    }

    public static void restore(FragmentActivity source) {
        callLeash("restore", source);
    }

    public static void restore(android.support.v4.app.Fragment source) {
        callLeash("restore", source);
    }

    public static void retain(Activity source) {
        callLeash("retain", source);
    }

    public static void retain(FragmentActivity source) {
        callLeash("retain", source);
    }

    public static void retain(Fragment source) {
        callLeash("retain", source);
    }

    public static void retain(android.support.v4.app.Fragment source) {
        callLeash("retain", source);
    }

    public static void clear(Activity source) {
        callLeash("clear", source);
    }

    public static void clear(FragmentActivity source) {
        callLeash("clear", source);
    }

    public static void clear(Fragment source) {
        callLeash("clear", source);
    }

    public static void clear(android.support.v4.app.Fragment source) {
        callLeash("clear", source);
    }

    private static void callLeash(String methodName, Object source) {
        try {
            String leashClassName = source.getClass().getName() + SUFFIX;
            Method method = Class.forName(leashClassName).getMethod(methodName, source.getClass());
            method.invoke(null, source);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to access Leash for " + source.getClass().getName(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to access Leash for " + source.getClass().getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access Leash for " + source.getClass().getName(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to access Leash for " + source.getClass().getName(), e);
        }
    }
}
