package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;

/**
 * A function which removes an element from the given array, yielding
 * a new array.
 */
public class RemoveAt
    extends Function
{
    private static final long serialVersionUID = 767834876897653987L;

    /**
     * Whether a type can be wrapped in {@link RemoveAt}.
     *
     * @param type The type to check.
     *
     * @return Whether RemoveAt can be applied to the type.
     */
    public static boolean isRemovable(final Class<?> type)
    {
        return type.isArray();
    }

    /**
     * CTOR.
     *
     * @param argType    The type of the array being removeAtd.
     * @param indexType  The type which is used to index into the array.
     */
    public RemoveAt(final Class<?>                argType,
                    final Class<? extends Number> indexType)
    {
        super(Arrays.asList(argType, indexType), argType);

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
        if (args.length != 2) {
            return null;
        }

        final Object value0 = args[0];
        final Object value1 = args[1];
        if (!value0.getClass().isArray() ||
            !(value1 instanceof Number))
        {
            return null;
        }

        // If it's smaller than two elements we can just return the
        // original
        final int length = Array.getLength(value0);
        int index = ((Number)value1).intValue();

        // Going from the end?
        if (index < 0) {
            index = length + index;
        }

        // Bounds check
        if (index < 0 || index >= length) {
            return null;
        }
        
        // Create the new one and copy in the contents, skipping over
        // the element which we want to remove
        final Object result =
            Array.newInstance(value0.getClass().getComponentType(), length - 1);
        for (int i=0, j=0; i < length; i++) {
            if (i == index) {
                continue;
            }
            Array.set(result, j++, Array.get(value0, i));
        }
        return result;
    }
}
