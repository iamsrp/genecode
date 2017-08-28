package genecode.function;

import java.lang.reflect.Array;

import java.util.Arrays;

/**
 * A function which replaces elements in a list, or substrings in a
 * string.
 */
public class Replace
    extends Function
{
    private static final long serialVersionUID = 5693849736559386L;

    /**
     * CTOR.
     *
     * @param argType The array or String type we take.
     */
    public Replace(final Class<?> listType)
    {
        super(
            Arrays.asList(
                listType,
                listType.isArray() ? listType.getComponentType() : String.class,
                listType.isArray() ? listType.getComponentType() : String.class,
                Integer.class
            ),
            listType
        );

        if (!listType.isArray() && listType != String.class) {
            throw new IllegalArgumentException(
                listType + " is not an array type or a String"
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object safeCall(final Object[] args)
    {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                "Bad arguments: " + Arrays.toString(args)
            );
        }
        for (Object arg : args) {
            if (arg == null) {
                throw new IllegalArgumentException(
                    "Bad arguments: " + Arrays.toString(args)
                );
            }
        }

        // Max replace count
        if (!(args[3] instanceof Number)) {
            throw new IllegalArgumentException(
                "args[3] was not a Number: " + Arrays.toString(args)
            );
        }
        final int max = ((Number)args[3]).intValue();

        // What we are replacing, and with what

        if (args[0].getClass().isArray()) {
            // Create the new one and copy in the contents, skipping over
            // the element which we want to remove
            final Object array = args[0];
            final Object what = args[1];
            final Object with = args[2];

            // Array info
            final int length = Array.getLength(array);
            final Object result =
                Array.newInstance(array.getClass().getComponentType(), length);

            // Do the replace
            for (int i=0, c=0; i < length; i++) {
                Object element = Array.get(array, i);
                if (c < max && what.equals(element)) {
                    c++;
                    element = with;
                }
                Array.set(result, i, element);
            }
            return result;
        }
        else if (args[0].getClass() == String.class) {
            if (!(args[1] instanceof String &&
                  args[2] instanceof String))
            {
                throw new IllegalArgumentException(
                    "args[1] and args[2] were not Strings: " + Arrays.toString(args)
                );
            }

            // What we're doing
            final String string = (String)args[0];
            final String what   = (String)args[1];
            final String with   = (String)args[2];

            // Build this up
            final StringBuilder sb = new StringBuilder();

            // Do the replace
            for (int i=0, c=0; i < string.length(); /*nothing*/) {
                if (c < max && string.regionMatches(i, what, 0, what.length())) {
                    sb.append(with);
                    i += what.length();
                    c++;
                }
                else {
                    sb.append(string.charAt(i++));
                }
            }

            // And done
            return sb.toString();
        }
        else {
            throw new IllegalArgumentException(
                "args[0] was not a String or array: " + args[0]
            );
        }
    }
}
