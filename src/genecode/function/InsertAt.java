package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;

/**
 * A function which inserts an element into the given array, yielding
 * a new array.
 */
public class InsertAt
    extends Function
{
    private static final long serialVersionUID = 654165418901317L;

    /**
     * Whether a type can be wrapped in {@link InsertAt}.
     *
     * @param type The type to check.
     *
     * @return Whether InsertAt can be applied to the type.
     */
    public static boolean isInsertable(final Class<?> type)
    {
        return type.isArray();
    }

    /**
     * CTOR.
     *
     * @param argType    The type of the array being insertAtd.
     * @param indexType  The type which is used to index into the array.
     */
    public InsertAt(final Class<?>                argType,
                    final Class<? extends Number> indexType)
    {
        super(
            Arrays.asList(argType, indexType, argType.getComponentType()),
            argType
        );

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
        if (args.length != 3) {
            return null;
        }

        final Object value0 = args[0];
        final Object value1 = args[1];
        final Object value2 = args[2];
        if (!value0.getClass().isArray() ||
            !(value1 instanceof Number)  ||
            value2 == null               ||
            !value0.getClass().getComponentType().isAssignableFrom(value2.getClass()))
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
        if (index < 0 || index > length) {
            return null;
        }
        
        // Create the new one and copy in the contents, pushing in the
        // element which we want to insert
        final Object result =
            Array.newInstance(value0.getClass().getComponentType(), length + 1);
        for (int i=0, j=0; i <= length; i++) {
            final Object element = (i == index) ? value2 : Array.get(value0, j++);
            Array.set(result, i, element);
        }
        return result;
    }
}
