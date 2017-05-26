package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;

/**
 * A function which reverses the contents of an array.
 */
public class Reverse
    extends Function
{
    private static final long serialVersionUID = 68233468356735L;

    /**
     * CTOR.
     *
     * @param argType    The type of the array being reversed.
     */
    public Reverse(final Class<?> argType)
    {
        super(Arrays.asList(argType), argType);

        if (!argType.isArray()) {
            throw new IllegalArgumentException(
                "Argument of function is not an array type: " + argType
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        if (args.length != 1) {
            return null;
        }

        final Object array = args[0];
        if (!array.getClass().isArray()) {
            return null;
        }

        // If it's smaller than two elements we can just return the
        // original
        final int length = Array.getLength(array);
        if (length < 2) {
            return array;
        }

        // Create the new one and copy in the contents, reversed
        final Object result =
            Array.newInstance(array.getClass().getComponentType(), length);
        for (int i=0; i < length; i++) {
            Array.set(result, length - 1 - i, Array.get(array, i));
        }
        return result;
    }
}
