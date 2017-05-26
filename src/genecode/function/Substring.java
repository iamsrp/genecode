package genecode.function;

import java.util.Arrays;

/**
 * A function which takes the substring of a string.
 */
public class Substring
    extends Function
{
    private static final long serialVersionUID = 86778335783567L;

    /**
     * CTOR.
     */
    public Substring()
    {
        super(Arrays.asList(String.class, Long.class, Long.class), String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value0 = args[0];
        final Object value1 = args[1];
        final Object value2 = args[2];
        if (value0.getClass() != String.class ||
            !(value1 instanceof Number)       ||
            !(value2 instanceof Number))
        {
            return null;
        }

        // How big and what's the index?
        final String string = (String)value0;
        final int length = string.length();
        int from = ((Number)value1).intValue();
        int to   = ((Number)value2).intValue();

        // Going from the end?
        if (from < 0) {
            from = length + from;
        }
        if (to < 0) {
            to = length + to;
        }

        // Bounds restriction
        from = Math.min(length, Math.max(0, from));
        to   = Math.min(length, Math.max(0, to  ));

        // Now do it
        if (to <= from) {
            return "";
        }
        else {
            return string.substring(from, to);
        }
    }
}
