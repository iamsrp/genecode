package genecode.function;

import java.util.Arrays;

/**
 * A function which calls the toUpperCase on a string.
 */
public class ToUpper
    extends Function
{
    private static final long serialVersionUID = 98238761876431L;

    /**
     * CTOR.
     */
    public ToUpper()
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
            return ((String)value).toUpperCase();
        }
    }
}
