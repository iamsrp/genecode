package example;

import java.util.HashMap;
import java.util.Map;

/**
 * Attempt to derive a simple run-length-encoding algorithm.
 *
 * <p>This isn't really expected to be solved by the genecode stuff
 * but, if it is, then I will be pretty impressed.
 *
 * <p>FWIW, this is how I would solve it:<pre>
 *
 *    # Stitch all the arrays into a single string
 *    Reduce[StringConcat](
 *        # Stitch the arrays together into one long one
 *        Reduce[Concat](
 *            # Generate an array of arrays of repeated chars
 *            Map[Repeat](
 *                # The odd characters of the string (the char)
 *                Select(
 *                    StringSplit(string, ""),
 *                    Map[EQ](
 *                        Map[Mod](
 *                            Range (0, Length(string)),
 *                            Repeat(2, Length(string))
 *                        ),
 *                        Repeat(1, Length(string))
 *                    )
 *                ),
 *                # The even characters of the string (the count), as a long
 *                Map[ParseLong](
 *                    Select(
 *                        StringSplit(string, ""),
 *                        Map[EQ](
 *                            Map[Mod](
 *                                Range (0, Length(string)),
 *                                Repeat(2, Length(string))
 *                            ),
 *                            Repeat(0, Length(string))
 *                         )
 *                    )
 *                )
 *            )
 *        )
 *    )
 *
 * </pre>
 */
public class RLESolver
    extends StringSolver
{
    private static String compress(final String string)
    {
        // Null case
        if (string.isEmpty()) {
            return "";
        }

        final StringBuilder result = new StringBuilder();

        // Keep track of contiguous chars
        char last  = string.charAt(0);
        int  count = 1;

        // Walk the string, accumulating the chars
        for (int i=1; i < string.length(); i++) {
            // See if this char differs from the last, or we are about
            // to overflow our single-digit counter
            final char c = string.charAt(i);
            if (count == 9 || c != last) {
                result.append(count).append(last);
                count = 0;
            }
            last = c;
            count++;
        }

        // The remaining values
        result.append(count).append(last);

        return result.toString();
    }

    /**
     * Entry point.
     */
    public static void main(String... args)
    {
        final Map<String,String> mappings = new HashMap<>();
        for (
            String string :
                new String[] {
                    "abcd",
                    "xxxyyyyzzzz",
                    "gghhiijjjjkkkk",
                    "tttttttttttsuuu",
                    "qu",
                    "ppkk",
                    "dddfff",
                    "cvvbbbnnnn",
                    "yyyytttrre",
                }
        ) {
            mappings.put(compress(string), string);
        }

        new RLESolver().solve(mappings);
    }
}
