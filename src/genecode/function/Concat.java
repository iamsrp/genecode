package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;

/**
 * A function which concatenates the contents of two arrays.
 */
public class Concat
    extends Function
{
    private static final long serialVersionUID = 68233468356735L;

    /**
     * CTOR.
     *
     * @param argType The type of the array being concatenated.
     */
    public Concat(final Class<?> argType)
    {
        super(Arrays.asList(argType, argType), argType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        if (args.length != 2) {
            return null;
        }

        final Object array0 = args[0];
        final Object array1 = args[1];
        if (!array0.getClass().isArray() || !array1.getClass().isArray()) {
            return null;
        }

        // Type check
        if (!array0.getClass().equals(getReturnType()) ||
            !array1.getClass().equals(getReturnType()))
        {
            return null;
        }

        // The result is the size of both
        final int length0 = Array.getLength(array0);
        final int length1 = Array.getLength(array1);

        // Simple cases
        if (length0 == 0) {
            return array1;
        }
        if (length1 == 0) {
            return array0;
        }

        // Create the new one and copy in the contents for each array
        final Object result =
            Array.newInstance(array0.getClass().getComponentType(),
                              length0 + length1);
        for (int i=0; i < length0; i++) {
            Array.set(result, i, Array.get(array0, i));
        }
        for (int i=0; i < length1; i++) {
            Array.set(result, length0 + i, Array.get(array1, i));
        }
        return result;
    }
}
