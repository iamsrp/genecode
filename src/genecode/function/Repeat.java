package genecode.function;

import genecode.ArrayUtil;

import java.lang.reflect.Array;

import java.util.Arrays;
import java.util.List;

/**
 * A function which create an array containing the first argument,
 * repeated a number of times.
 *
 * <p>The size of the resultant array is specifically capped so as to
 * guard against maxing out the available heap space.
 */
public class Repeat
    extends Function
{
    private static final long serialVersionUID = 862968256702560L;

    /**
     * The maximum number of values which were may repeat.
     */
    private final int myMaxLength;

    /**
     * CTOR.
     *
     * @param argType    The type of the element being repeated.
     * @param maxLength  The maximum number of allowed repeats. Attempts to 
     *                   allocate more than this will result in failure.
     */
    public Repeat(final Class<?> argType, final int maxLength)
    {
        super(Arrays.asList(argType, Long.class),
              ArrayUtil.arrayType(argType));
        myMaxLength = maxLength;
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
        if (value0 == null || !(value1 instanceof Number)) {
            return null;
        }

        // How many? And range check it.
        final int length = ((Number)value1).intValue();
        if (length < 0 || length >= myMaxLength) {
            return null;
        }

        // Create the result and put in the arguments as values
        final Object result =
            Array.newInstance(getReturnType().getComponentType(), length);
        for (int i=0; i < length; i++) {
            Array.set(result, i, value0);
        }
        return result;
    }
}
