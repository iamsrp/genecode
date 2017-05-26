package genecode.function;

import java.util.Arrays;

/**
 * A function which negates a value.
 */
public class Neg
    extends Function
{
    private static final long serialVersionUID = 417864237362487687L;

    /**
     * CTOR.
     *
     * @param type The subclass of {@link Number} which we take and
     *             give back. Only boxed versions of primitive types
     *             are supported.
     */
    public Neg(final Class<? extends Number> type)
    {
        super(Arrays.asList(type), type);
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
            return Byte.valueOf((byte)(-((Number)value).byteValue()));
        }
        else if (getReturnType().equals(Short.class)) {
            return Short.valueOf((short)(-((Number)value).shortValue()));
        }
        else if (getReturnType().equals(Integer.class)) {
            return Integer.valueOf(-((Number)value).intValue());
        }
        else if (getReturnType().equals(Long.class)) {
            return Long.valueOf(-((Number)value).longValue());
        }
        else if (getReturnType().equals(Float.class)) {
            return Float.valueOf(-((Number)value).floatValue());
        }
        else if (getReturnType().equals(Double.class)) {
            return Double.valueOf(-((Number)value).doubleValue());
        }
        else {
            return null;
        }
    }
}
