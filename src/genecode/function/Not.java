package genecode.function;

import java.util.Arrays;

/**
 * A function which performs a binary {@code NOT} opertion.
 */
public class Not
    extends Function
{
    private static final long serialVersionUID = 7867438753287235L;

    /**
     * CTOR.
     */
    public Not()
    {
        super(Arrays.asList(Boolean.class), Boolean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value = args[0];
        return (value instanceof Boolean) ? Boolean.valueOf(!(Boolean)value)
                                          : null;
    }
}
