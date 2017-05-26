package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;

/**
 * A function which gets the {@code n}th element of an array.
 *
 * <p>If the index is negative then it is assumed to be an offset from
 * the end, Python style. I.e. -1 is the last element in the array.
 */
public class GetAt
    extends Function
{
    private static final long serialVersionUID = 6734656754368753L;

    /**
     * CTOR.
     *
     * @param argType    The type of the array being indexed.
     * @param indexType  The type which is used to index into the array.
     */
    public GetAt(final Class<?>                argType,
                 final Class<? extends Number> indexType)
    {
        super(Arrays.asList(argType, indexType), argType.getComponentType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value0 = args[0];
        final Object value1 = args[1];
        if (!value0.getClass().isArray() ||
            !(value1 instanceof Number)  ||
            !getReturnType().isAssignableFrom(value0.getClass().getComponentType()))
        {
            return null;
        }

        // How big and what's the index?
        final int length = Array.getLength(value0);
        int index = ((Number)value1).intValue();

        // Going from the end?
        if (index < 0) {
            index = length + index;
        }

        // Now we can look
        if (index < 0 || index >= length) {
            return null;
        }
        else {
            return Array.get(value0, index);
        }
    }
}
