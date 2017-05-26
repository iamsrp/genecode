package genecode.function;

import java.util.Arrays;

/**
 * A function which calls the toLowerCase on a string.
 */
public class ToLower
    extends Function
{
    private static final long serialVersionUID = 8961879546431L;

    /**
     * CTOR.
     */
    public ToLower()
    {
        super(Arrays.asList(String.class), String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value = args[0];
        if (value.getClass() != String.class) {
            return null;
        }
        else {
            return ((String)value).toLowerCase();
        }
    }
}
