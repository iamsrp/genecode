package genecode;

import java.lang.reflect.Array;

import java.util.HashMap;
import java.util.Map;

/**
 * Handy array functions.
 */
public class ArrayUtil
{
    /**
     * Cache of array types
     */
    private static final ThreadLocal<Map<Class<?>,Class<?>>> ourTypeCache =
        new ThreadLocal<Map<Class<?>,Class<?>>>() {
            @Override protected Map<Class<?>,Class<?>> initialValue() {
                return new HashMap<>();
            }
        };

    /**
     * Get the type of an array holding the given type.
     *
     * @param klass The element type of the array.
     *
     * @return The type of the array.
     */
    public static Class<?> arrayType(final Class<?> klass)
    {
        return ourTypeCache.get().computeIfAbsent(
            klass,
            type -> Array.newInstance(type, 0).getClass()
        );
    }
}
