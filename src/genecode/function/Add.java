package genecode.function;

import java.util.Arrays;

/**
 * A function which adds two values.
 */
public class Add
    extends Function
{
    private static final long serialVersionUID = 86436578436734763L;

    /**
     * CTOR.
     *
     * @param type The subclass of {@link Number} which we take and
     *             give back. Only boxed versions of primitive types
     *             are supported.
     */
    public Add(final Class<? extends Number> type)
    {
        super(Arrays.asList(type, type), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value0 = args[0];
        final Object value1 = args[1];
        if (!(value0 instanceof Number) || !(value1 instanceof Number)) {
            return null;
        }

        if (getReturnType().equals(Byte.class)) {
            return Byte.valueOf((byte)(((Number)value0).byteValue() +
                                       ((Number)value1).byteValue()));
        }
        else if (getReturnType().equals(Short.class)) {
            return Short.valueOf((short)(((Number)value0).shortValue() +
                                         ((Number)value1).shortValue()));
        }
        else if (getReturnType().equals(Integer.class)) {
            return Integer.valueOf(((Number)value0).intValue() +
                                   ((Number)value1).intValue());
        }
        else if (getReturnType().equals(Long.class)) {
            return Long.valueOf(((Number)value0).longValue() +
                                ((Number)value1).longValue());
        }
        else if (getReturnType().equals(Float.class)) {
            return Float.valueOf(((Number)value0).floatValue() +
                                 ((Number)value1).floatValue());
        }
        else if (getReturnType().equals(Double.class)) {
            return Double.valueOf(((Number)value0).doubleValue() +
                                  ((Number)value1).doubleValue());
        }
        else {
            return null;
        }
    }
}
