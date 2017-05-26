package genecode.function;

import java.util.Arrays;

/**
 * A function which turns anything into a string.
 */
public class ToString
    extends Function
{
    private static final long serialVersionUID = 8778657856134347L;

    /**
     * CTOR.
     *
     * @param type What to convert to a string.
     */
    public ToString(final Class<?> type)
    {
        super(Arrays.asList(type), String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value = args[0];
        return (value == null) ? null : String.valueOf(value);
    }
}
