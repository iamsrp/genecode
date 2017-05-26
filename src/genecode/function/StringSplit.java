package genecode.function;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * A function which splits one string using a second string as the
 * delimiter.
 */
public class StringSplit
    extends Function
{
    private static final long serialVersionUID = 34878763246767523L;

    /**
     * CTOR.
     */
    public StringSplit()
    {
        super(Arrays.asList(String.class, String.class),
              new String[0].getClass());
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

        final String splittee = (String)value0;
        final String splitter = (String)value1;

        final List<String> result = new ArrayList<>();

        // If we're splitting on the empty string then it's a simple
        // case
        if (splitter.isEmpty()) {
            // Each character becomes a string
            for (int i=0; i < splittee.length(); i++) {
                result.add(Character.toString(splittee.charAt(i)));
            }
        }
        else {
            // Splitting on a non-empty string
            int from = 0;
            while (from < splittee.length()) {
                // Figure out where we're going from, and to
                int index = splittee.indexOf(splitter, from);

                // Going to here
                final int to = (index < 0) ? splittee.length() : index;

                // Up to, but not including, the start of the splitter string
                result.add(splittee.substring(from, to));

                // Move past the splitter string
                from = to + splitter.length();
            }
        }

        return result.toArray(new String[result.size()]);
    }
}
