package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;
import java.util.List;

/**
 * A function which selects elements from one array based on an array
 * of booleans.
 */
public class Select
    extends Function
{
    private static final long serialVersionUID = 25608782343876876L;

    /**
     * CTOR.
     *
     * @param argType  The type of the array being selected.
     */
    public Select(final Class<?> argType)
    {
        super(Arrays.asList(argType, new Boolean[0].getClass()), argType);

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
        // The inputs
        final Object value0 = args[0];
        final Object value1 = args[1];
        if (value0 == null || value1 == null) {
            return null;
        }
        if (!value0.getClass().isArray() ||
            !value1.getClass().isArray() ||
            !value0.getClass().equals(getReturnType()) ||
            !value1.getClass().getComponentType().equals(Boolean.class))
        {
            return null;
        }

        // How many
        final int length = Math.min(Array.getLength(value0),
                                    Array.getLength(value1));

        // How big to make the result
        int numTrue = 0;
        for (int i=0; i < length; i++) {
            if (Array.get(value1, i) == Boolean.TRUE) {
                numTrue++;
            }
        }

        // Create the result and put in the arguments as values where
        // the selector is true
        final Object result =
            Array.newInstance(getReturnType().getComponentType(), numTrue);
        for (int i=0, j=0; i < length; i++) {
            if (Array.get(value1, i) == Boolean.TRUE) {
                Array.set(result, j++, Array.get(value0, i));
            }
        }
        return result;
    }
}
