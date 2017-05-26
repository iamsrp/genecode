package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;
import java.util.List;

/**
 * A function which create an array containing the integer numbers in
 * the range {@code [arg0..arg1)}.
 */
public class Range
    extends Function
{
    private static final long serialVersionUID = 23989658706532L;

    /**
     * CTOR.
     *
     * @param argType  The type of the element being rangeed.
     */
    public Range(final Class<? extends Number> argType)
    {
        super(Arrays.asList(argType, argType),
              Array.newInstance(argType, 0).getClass());
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
        if (!(value0 instanceof Number) || !(value1 instanceof Number)) {
            return null;
        }

        // From what to what
        final long start = ((Number)value0).longValue();
        final long end   = ((Number)value1).longValue();
        final long count = Math.abs(end - start);

        // Bounds check (arrays can't be bigger than max-int, and
        // worry about overflow)
        if (count > Integer.MAX_VALUE) {
            return null;
        }
        if (Math.abs((double)end - (double)start) > Integer.MAX_VALUE) {
            return null;
        }

        // Create the result and put in the arguments as values
        final Object result =
            Array.newInstance(getReturnType().getComponentType(), (int)count);
        for (int i = 0; i < count; i++) {
            // Determine the value to stuff in
            final long   value = start + i * ((end > start) ? 1 : -1);
            final Object object;
            if (getReturnType().equals(Byte.class)) {
                object = Byte.valueOf((byte)value);
            }
            else if (getReturnType().equals(Short.class)) {
                object = Short.valueOf((short)value);
            }
            else if (getReturnType().equals(Integer.class)) {
                object = Integer.valueOf((int)value);
            }
            else if (getReturnType().equals(Long.class)) {
                object = Long.valueOf(value);
            }
            else if (getReturnType().equals(Float.class)) {
                object = Float.valueOf(value);
            }
            else if (getReturnType().equals(Double.class)) {
                object = Double.valueOf(value);
            }
            else {
                return null;
            }

            // And set it
            Array.set(result, i, value);
        }
        return result;
    }
}
