package genecode.function;

import java.util.Arrays;

/**
 * A function which computes the exponent of a value.
 */
public class Exp
    extends Function
{
    private static final long serialVersionUID = 1876247865349807L;

    /**
     * CTOR.
     *
     * @param type The subclass of {@link Number} which we take and
     *             give back. Only boxed versions of floating point
     *             types are supported.
     */
    public Exp(final Class<? extends Number> type)
    {
        super(Arrays.asList(type), type);
        if (type != Double.class && type != Float.class) {
            throw new IllegalArgumentException(
                "Only floating point values are supported, not " + type
            );
        }
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

        if (getReturnType().equals(Float.class)) {
            return Float.valueOf((float)Math.exp(((Number)value).doubleValue()));
        }
        else if (getReturnType().equals(Double.class)) {
            return Double.valueOf(Math.exp(((Number)value).doubleValue()));
        }
        else {
            return null;
        }
    }
}
