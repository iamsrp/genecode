package genecode.function;

import java.util.Arrays;

/**
 * A function which creates the concatenation of two Strings.
 */
public class StringConcat
    extends Function
{
    private static final long serialVersionUID = 9887123687874123L;

    /**
     * CTOR.
     */
    public StringConcat()
    {
        super(Arrays.asList(String.class, String.class), String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value0 = args[0];
        final Object value1 = args[1];
        if (!(value0 instanceof String) || !(value1 instanceof String)) {
            return null;
        }

        final String string0 = (String)value0;
        final String string1 = (String)value1;

        if (string0.isEmpty()) {
            return string1;
        }
        else if (string1.isEmpty()) {
            return string0;
        }
        else {
            return string0 + string1;
        }
    }
}
