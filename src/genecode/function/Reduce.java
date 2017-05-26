package genecode.function;

import genecode.ArrayUtil;

import java.lang.reflect.Array;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;

/**
 * A function which function on the elements of an array to produce a
 * single resultant value. The function must take two arguments and
 * return one, all of the same type.
 *
 * <p>For example, if the function is {@link Add} then it will sum all
 * the elements in the array.
 */
public class Reduce
    extends Function
{
    private static final long serialVersionUID = 986823478567865L;

    /**
     * Our function.
     */
    private Function myFunction;

    /**
     * Space for the arguments to call the function with.
     */
    private Object[] myValues;

    /**
     * Whether a given function can be used to map.
     *
     * @param function The function to check.
     *
     * @return Whether the function can be wrapped in {@link Reduce}.
     */
    public static boolean isApplicable(final Function function)
    {
        // Must take two args and return one, all of the same type
        if (function.getArgTypes().size() != 2) {
            return false;
        }
        for (int i=0; i < 2; i++) {
            if (!function.getArgTypes().get(i).equals(function.getReturnType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get our arguments from the given function.
     */
    private static List<Class<?>> getArgTypes(final Function function)
    {
        // Should take two args
        final List<Class<?>> types = function.getArgTypes();
        if (types.size() != 2) {
            throw new IllegalArgumentException(
                "Function should take two arguments: " + function.describe()
            );
        }

        // Check the types are sane
        for (int i=0; i < types.size(); i++) {
            final Class<?> type = types.get(i);
            if (!type.equals(function.getReturnType())) {
                throw new IllegalArgumentException(
                    "Argument #" + i + " " +
                    "of function differs from the return type: " +
                    function.describe()
                );
            }
        }

        // And give it back
        return Collections.singletonList(
            ArrayUtil.arrayType(function.getReturnType())
        );
    }

    /**
     * CTOR.
     *
     * @param function The function to wrap.
     */
    public Reduce(final Function function)
    {
        super(getArgTypes(function), function.getReturnType());
        myFunction = function;
        myValues   = new Object[2];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return super.toString() + "[" + myFunction + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reduce clone()
    {
        final Reduce reduce = (Reduce)super.clone();
        reduce.myFunction = myFunction.clone();
        reduce.myValues   = new Object[myValues.length];
        return reduce;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        if (args == null || args.length != 1) {
            return null;
        }

        // Extract the arguments and ensure that they look like what
        // we want
        final Object arg = args[0];

        // Ensure the array argument looks right
        if (arg == null ||
            !arg.getClass().isArray() ||
            !getReturnType().isAssignableFrom(arg.getClass().getComponentType()))
        {
            return null;
        }

        // Safe to use, and update our notion of max size
        final int length = Array.getLength(arg);

        // Anything? If just one element then return it
        if (length == 0) {
            return null;
        }
        if (length == 1) {
            return Array.get(arg, 0);
        }

        // Start us off
        myValues[0] = Array.get(arg, 0);
        myValues[1] = Array.get(arg, 1);
        if (myValues[0] == null || myValues[1] == null) {
            return null;
        }
        Object result = myFunction.call(myValues);

        // And accumulate
        for (int i = 2; i < length; i++) {
            myValues[0] = result;
            myValues[1] = Array.get(arg, i);
            if (myValues[0] == null || myValues[1] == null) {
                return null;
            }
            result = myFunction.call(myValues);
        }

        // Now just give it back
        return result;
    }
}
