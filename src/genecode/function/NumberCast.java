package genecode.function;

import java.util.Arrays;

/**
 * Cast one number value to another.
 */
public class NumberCast
    extends Function
{
    private static final long serialVersionUID = 1209738968965L;

    /**
     * CTOR.
     *
     * @param from  Casting from this type.
     * @param to    Casting to this type.
     */
    public NumberCast(final Class<? extends Number> from,
                      final Class<? extends Number> to)
    {
        super(Arrays.asList(from), to);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getReturnType().getSimpleName() + "Cast";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value = args[0];
        if (!(value instanceof Number)) {
            return null;
        }

        if (getReturnType().equals(Byte.class)) {
            return Byte.valueOf(((Number)value).byteValue());
        }
        else if (getReturnType().equals(Short.class)) {
            return Short.valueOf(((Number)value).shortValue());
        }
        else if (getReturnType().equals(Integer.class)) {
            return Integer.valueOf(((Number)value).intValue());
        }
        else if (getReturnType().equals(Long.class)) {
            return Long.valueOf(((Number)value).longValue());
        }
        else if (getReturnType().equals(Float.class)) {
            return Float.valueOf(((Number)value).floatValue());
        }
        else if (getReturnType().equals(Double.class)) {
            return Double.valueOf(((Number)value).doubleValue());
        }
        else {
            return null;
        }
    }
}
