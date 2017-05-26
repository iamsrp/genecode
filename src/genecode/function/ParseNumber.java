package genecode.function;

import java.util.Arrays;

/**
 * A function which parses a string as a number.
 */
public class ParseNumber
    extends Function
{
    private static final long serialVersionUID = 90823548976347L;

    /**
     * CTOR.
     *
     * @param type The subclass of {@link Number} which we take and
     *             give back. Only boxed versions of primitive types
     *             are supported.
     */
    public ParseNumber(final Class<? extends Number> type)
    {
        super(Arrays.asList(String.class), type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "parse" + getReturnType().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value = args[0];
        if (!(value instanceof String)) {
            return null;
        }

        try {
            if (getReturnType().equals(Byte.class)) {
                return Byte.parseByte((String)value);
            }
            else if (getReturnType().equals(Short.class)) {
                return Short.parseShort((String)value);
            }
            else if (getReturnType().equals(Integer.class)) {
                return Integer.parseInt((String)value);
            }
            else if (getReturnType().equals(Long.class)) {
                return Long.parseLong((String)value);
            }
            else if (getReturnType().equals(Float.class)) {
                return Float.parseFloat((String)value);
            }
            else if (getReturnType().equals(Double.class)) {
                return Double.parseDouble((String)value);
            }
            else {
                return null;
            }
        }
        catch (NumberFormatException e) {
            return null;
        }
    }
}
