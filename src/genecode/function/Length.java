package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;

/**
 * A function which gets the size of a collection.
 */
public class Length
    extends Function
{
    private static final long serialVersionUID = 2376235762708708L;

    /**
     * CTOR.
     *
     * @param argType The array or String type we take.
     */
    public Length(final Class<?> argType)
    {
        super(Arrays.asList(argType), Long.class);
        if (!argType.isArray() && argType != String.class) {
            throw new IllegalArgumentException(
                argType + " is not an array type or a String"
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object safeCall(final Object[] args)
    {
        final Object value = args[0];
        if (value == null) {
            return null;
        }
        else if (value.getClass() == String.class) {
            return Long.valueOf(((String)value).length());
        }
        else {
            return !value.getClass().isArray()
                ? null
                : Long.valueOf(Array.getLength(value));
        }
    }
}
