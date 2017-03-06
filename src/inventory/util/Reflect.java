package inventory.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflect {

    private static Logger LOG = LogManager.getLogger(Reflect.class);

    public static Class[] buildParamTypeList(Object...args) {
        // Build the argument type list...
        Class[] parmTypes = new Class[args.length];
        for ( int i = 0; i < args.length; i++ ) {
            parmTypes[i] = args[i].getClass();
        }

        return parmTypes;
    }

    /**
     * Checks if an Object has a method that has the given args.
     * @param o
     * @param method
     * @return
     */
    public static boolean hasMethod(Object o, String method, Object...args) {
        if ( o == null ) {
            return false;
        }

        Class[] parmTypes = buildParamTypeList(args);

        try {
            Method m = o.getClass().getMethod(method, parmTypes);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Given an Object, tries to find a suitable Method matching the
     * real types of `...args` and invokes it, if available.
     *
     * @param o Object to execute method on
     * @param method Method name to look up on `o`
     * @param args Arguments to use to find / invoke `method`
     * @return Object return value from `method`
     */
    public static Object oneShot(Object o, String method, Object...args) throws
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Check nullity on `o`
        if ( o == null ) {
            throw new NullPointerException("Object param `o` can not be null!");
        }

        Class[] parmTypes = buildParamTypeList(args);

        // No need to check m == null, getMethod will throw NoSuchMethodException
        Object ret = null;
        Method m = o.getClass().getMethod(method, parmTypes);
        try {
            ret = m.invoke(o, args);
        } catch (IllegalAccessException iaex) {
            m.setAccessible(true);
            try {
                ret = m.invoke(o, args);
            } catch (Exception ex) {
                throw ex;
            }
            m.setAccessible(false);
        }

        return ret;
    }

    /**
     * Same as `oneShot`, but disregards any exceptions.
     *
     * @param o
     * @param method
     * @param args
     * @return
     */
    public static Object unsafeOneShot(Object o, String method, Object...args) {
        try {
            return oneShot(o, method, args);
        } catch (Exception ex) {
            LOG.warn("Ignoring thrown exception from oneShot:");
            LOG.catching(ex);
            return null;
        }
    }

}
