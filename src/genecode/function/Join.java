package genecode.function;

import genecode.ArrayUtil;

import java.util.Arrays;

/**
 * A function which joins an array of strings by a single string (like
 * Python's {@code join()} function.
 */
public class Join
    extends Function
{
    private static final long serialVersionUID = 387967862870624L;

    /**
     * CTOR.
     */
    public Join()
    {
        super(Arrays.asList(ArrayUtil.arrayType(String.class), String.class),
              String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object safeCall(final Object[] args)
    {
        final Object value0 = args[0];
        final Object value1 = args[1];
        if (value0 == null ||
            !value0.getClass().isArray() ||
            !(value0.getClass().getComponentType() == String.class) ||
            !(value1 instanceof String))
        {
            return null;
        }

        final String[] strings = (String[])value0;
        final String   joiner  = (String)  value1;

        if (strings.length == 0) {
            return "";
        }
        else if (strings.length == 1) {
            return strings[0];
        }
        else {
            final StringBuilder sb = new StringBuilder();
            for (int i=0; i < strings.length; i++) {
                if (i > 0) {
                    sb.append(joiner);
                }
                sb.append(strings[i]);
            }
            return sb.toString();
        }
    }
}
