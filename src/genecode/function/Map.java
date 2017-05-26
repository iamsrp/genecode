package genecode.function;

import genecode.ArrayUtil;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A function which calls another function on one, or more, arrays to
 * produce a third array.
 */
public class Map
    extends Function
{
    private static final long serialVersionUID = 23879428974257L;

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
     * @return Whether the function may be wrapped by {@link Map}.
     */
    public static boolean isApplicable(final Function function)
    {
        // No args means can't be used
        if (function.getArgTypes().size() == 0) {
            return false;
        }

        // Otherwise, yes
        return true;
    }

    /**
     * Get our arguments from the given function.
     */
    private static List<Class<?>> getArgTypes(final Function function)
    {
        final List<Class<?>> types  = function.getArgTypes();
        final List<Class<?>> result = new ArrayList<>(types.size());
        for (int i=0; i < types.size(); i++) {
            result.add(ArrayUtil.arrayType(types.get(i)));
        }
        return result;
    }

    /**
     * Get our return type from the given function.
     */
    private static Class<?> getReturnType(final Function function)
    {
        return ArrayUtil.arrayType(function.getReturnType());
    }

    /**
     * CTOR.
     *
     * @param function The function to wrap.
     */
    public Map(final Function function)
    {
        super(getArgTypes(function), getReturnType(function));
        myFunction = function;
        myValues   = new Object[function.getArgTypes().size()];

        if (function.getArgTypes().size() == 0) {
            throw new IllegalArgumentException(
                "Can't be applied to a function taking no arguments: " +
                function.describe()
            );
        }
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
    public Map clone()
    {
        final Map map = (Map)super.clone();
        map.myFunction = myFunction.clone();
        map.myValues   = new Object[myValues.length];
        return map;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        if (args == null ||
            args.length == 0 ||
            args.length != myValues.length)
        {
            return null;
        }

        // Extract the arguments and ensure that they look like what
        // we want
        int length = Integer.MAX_VALUE;
        for (int i=0; i < args.length; i++) {
            // The argument and its expected element type
            final Object   arg  = args[i];
            final Class<?> type = myFunction.getArgTypes().get(i);

            // Ensure the array argument looks right
            if (arg == null ||
                !arg.getClass().isArray() ||
                !type.isAssignableFrom(arg.getClass().getComponentType()))
            {
                return null;
            }

            // Safe to use, and update our notion of max size
            length = Math.min(Array.getLength(arg), length);
        }

        // We can construct space for the result now
        final Object result =
            Array.newInstance(myFunction.getReturnType(), length);

        // Now call map
        for (int i=0; i < length; i++) {
            // Extract the values from each array
            for (int j=0; j < myValues.length; j++) {
                final Object value = Array.get(args[j], i);
                if (value == null) {
                    return null;
                }
                myValues[j] = value;
            }

            // Call the function
            final Object value = myFunction.call(myValues);
            if (value == null) {
                return null;
            }

            // And store its result in 'result' array
            Array.set(result, i, value);
        }

        // And give it back
        return result;
    }
}
